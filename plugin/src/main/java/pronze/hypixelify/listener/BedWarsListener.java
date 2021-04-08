package pronze.hypixelify.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.GameManager;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.Scoreboard;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.ScoreboardManager;
import org.screamingsandals.bedwars.lib.lang.Message;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.player.PlayerWrapper;
import org.screamingsandals.bedwars.player.PlayerManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.game.ArenaManager;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@AutoInitialize(listener = true)
public class BedWarsListener implements Listener {
    private final Map<UUID, BukkitTask> runnableCache = new HashMap<>();

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .createArena(game);

        LanguageService
                .getInstance()
                .get(MessageKeys.GAME_START_MESSAGE)
                .send(game.getConnectedPlayers().stream().map(PlayerMapper::wrapPlayer).toArray(PlayerWrapper[]::new));
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final var pluginName = event.getPlugin().getName();
        //Register listeners again
        if (pluginName.equalsIgnoreCase(Main.getInstance().as(JavaPlugin.class).getName())) {
            Logger.trace("Re-registering listeners");
            final var listeners = Core.getRegisteredListeners();
            listeners.forEach(Core::unregisterListener);
            listeners.forEach(Core::registerListener);
            Logger.trace("Registration complete");
        }
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> arena.onTargetBlockDestroyed(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostRebuildingEvent(BedwarsPostRebuildingEvent e) {
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .removeArena(game);
    }


    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> arena.onOver(e));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        final var game = (Game) GameManager.getInstance().getGame(e.getGame().getName()).orElseThrow();
        final var task = runnableCache.get(player.getUniqueId());
        if (task != null) {
            SBAUtil.cancelTask(task);
        }

        switch (game.getStatus()) {
            case WAITING:
                var bukkitTask = new BukkitRunnable() {
                    int buffer = 1; //fixes the bug where it constantly shows will start in 1 second
                    @Override
                    public void run() {
                        if (game.getStatus() == GameStatus.WAITING) {
                            if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                                String time = game.getFormattedTimeLeft();

                                if (!time.contains("0-1")) {
                                    String[] units = time.split(":");
                                    int seconds = Integer.parseInt(units[1]) + 1;
                                    if (buffer == seconds) return;
                                    buffer = seconds;
                                    if (seconds <= 10) {
                                        var message = LanguageService
                                                .getInstance()
                                                .get(MessageKeys.GAME_STARTS_IN_MESSAGE)
                                                .replace("%seconds%", String.valueOf(seconds))
                                                .toString();

                                        message = seconds == 1 ? message
                                                .replace("seconds", "second") : message;
                                        player.sendMessage(message);
                                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(player), ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                                    }
                                }
                            }
                        } else {
                            this.cancel();
                            runnableCache.remove(player.getUniqueId());
                        }
                    }
                }.runTaskTimer(SBAHypixelify.getInstance(), 3L, 20L);
                runnableCache.put(player.getUniqueId(), bukkitTask);
                break;
            case RUNNING:
                ArenaManager
                        .getInstance()
                        .get(game.getName())
                        .ifPresent(arena -> arena.getScoreboardManager().createBoard(player));
                break;
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    final var scoreboardManager = arena.getScoreboardManager();
                    if (scoreboardManager != null) {
                        scoreboardManager.removeBoard(player);
                    }
                });

        if (task != null) {
            SBAUtil.cancelTask(task);
        }
        runnableCache.remove(player.getUniqueId());

        ScoreboardManager
                .getInstance()
                .fromCache(player.getUniqueId())
                .ifPresent(Scoreboard::destroy);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @EventHandler
    public void onBedWarsPlayerKilledEvent(BedwarsPlayerKilledEvent e) {
        final var game = e.getGame();
        // query arena instance for access to Victim/Killer data
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    final var victim = e.getPlayer();
                    // player has died, increment death counter
                    arena.getPlayerData(victim.getUniqueId())
                            .ifPresent(victimData -> victimData.setDeaths(victimData.getDeaths() + 1));

                    final var killer = e.getKiller();
                    //killer is present
                    if (killer != null) {
                        // get victim game profile
                        final var gVictim = PlayerManager
                                .getInstance()
                                .getPlayer(victim.getUniqueId())
                                .orElse(null);

                        if (gVictim == null || gVictim.isSpectator) return;

                        // get victim team to check if it was a final kill or not
                        final var victimTeam = game.getTeamOfPlayer(victim);
                        if (victimTeam != null) {
                            arena.getPlayerData(killer.getUniqueId())
                                    .ifPresent(killerData -> {
                                        // increment kill counter for killer
                                        killerData.setKills(killerData.getKills() + 1);
                                        if (!victimTeam.isAlive()) {
                                            // increment final kill counter for killer
                                            killerData.setFinalKills(killerData.getFinalKills() + 1);
                                        }
                                    });
                        }
                    }
                });
    }

}
