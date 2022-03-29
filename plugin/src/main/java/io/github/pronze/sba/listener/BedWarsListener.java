package io.github.pronze.sba.listener;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBAFinalKillEvent;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.lib.pronzelib.scoreboards.Scoreboard;
import io.github.pronze.lib.pronzelib.scoreboards.ScoreboardManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BedWarsListener implements Listener {
    private final Map<UUID, BukkitTask> runnableCache = new HashMap<>();

    @OnPostEnable
    public void registerListener() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        Logger.trace("SBA onStarted{}", e);

        final var game = e.getGame();
        final var arena = ArenaManager
                .getInstance()
                .createArena(game);

        ((Arena) arena).onGameStarted();
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final var pluginName = event.getPlugin().getName();
        if (pluginName.equalsIgnoreCase(Main.getInstance().getName())) {
            if (!SBA.getPluginInstance().isEnabled()) {
                return;
            }
            Logger.trace("Re registering listeners!");
            final var listeners = SBA.getInstance().getRegisteredListeners();

            listeners.forEach(SBA.getInstance()::unregisterListener);
            listeners.forEach(SBA.getInstance()::registerListener);

            Logger.trace("Registration complete!");
        }
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        Logger.trace("SBA onTargetBlockDestroyed{}", e);

        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena) arena).onTargetBlockDestroyed(e));
    }

    @EventHandler
    public void onPostRebuildingEvent(BedwarsPostRebuildingEvent e) {
        Logger.trace("SBA onPostRebuildingEvent{}", e);

        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena) arena).onOver(e));
        ArenaManager
                .getInstance()
                .removeArena(game);
    }

    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        Logger.trace("SBA onOver{}", e);

        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena) arena).onOver(e));
    }

    public io.github.pronze.sba.party.PartySetting.GameMode gamemodeOf(Player connectedPlayer) {
        AtomicReference<io.github.pronze.sba.party.PartySetting.GameMode> ref = new AtomicReference<>(
                io.github.pronze.sba.party.PartySetting.GameMode.PUBLIC);
        SBA.getInstance().getPartyManager().getPartyOf(SBA.getInstance().getPlayerWrapper(connectedPlayer))
                .ifPresent(party -> {
                    ref.set(party.getSettings().getGamemode());
                });
        return ref.get();
    }

    @EventHandler
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        Logger.trace("SBA onBWLobbyJoin{}", e);

        final var player = e.getPlayer();
        final var wrappedPlayer = SBA.getInstance().getPlayerWrapper((player));
        final var task = runnableCache.get(player.getUniqueId());
        final var game = (Game) e.getGame();
        if (task != null) {
            SBAUtil.cancelTask(task);
        }

        SBA
                .getInstance()
                .getPartyManager()
                .getPartyOf(wrappedPlayer)
                .ifPresentOrElse(party -> {
                    if (party.getSettings().getGamemode() == io.github.pronze.sba.party.PartySetting.GameMode.PRIVATE) {
                        game.getConnectedPlayers().forEach(connectedPlayer -> {
                            if (!party.getMembers().contains(SBA.getInstance().getPlayerWrapper(connectedPlayer))) {
                                game.leaveFromGame(player);
                                // Prevent joining
                                return;
                            }
                        });
                    }
                    if (!wrappedPlayer.equals(party.getPartyLeader())) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                .send(wrappedPlayer);
                        return;
                    }
                    if (party.getMembers().size() == 1) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_NO_PLAYERS_TO_WARP)
                                .send(wrappedPlayer);
                        return;
                    }

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_WARP)
                            .send(wrappedPlayer);

                    if (Main.getInstance().isPlayerPlayingAnyGame(player)) {

                        party.getMembers()
                                .stream().filter(member -> !wrappedPlayer.equals(member))
                                .forEach(member -> {
                                    final var memberGame = Main.getInstance().getGameOfPlayer(member.getInstance());

                                    Bukkit.getScheduler().runTask(SBA.getPluginInstance(), () -> {
                                        if (game != memberGame) {
                                            if (memberGame != null)
                                                memberGame.leaveFromGame(member.getInstance());
                                            game.joinToGame(member.getInstance());
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_WARP)
                                                    .send(member);
                                        }
                                    });
                                });
                    } else {
                        final var leaderLocation = wrappedPlayer.getInstance().getLocation();
                        party.getMembers()
                                .stream()
                                .filter(member -> !member.equals(player))
                                .forEach(member -> {
                                    if (Main.getInstance().isPlayerPlayingAnyGame(member.getInstance())) {
                                        Main.getInstance().getGameOfPlayer(member.getInstance())
                                                .leaveFromGame(member.getInstance());
                                    }
                                    PlayerUtils.teleportPlayer(member.getInstance(), leaderLocation);
                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_LEADER_JOIN_LEAVE)
                                            .send(PlayerMapper.wrapPlayer(member.getInstance()));
                                });
                    }
                }, () -> {
                    game.getConnectedPlayers().forEach(connectedPlayer -> {
                        if (gamemodeOf(connectedPlayer) == io.github.pronze.sba.party.PartySetting.GameMode.PRIVATE) {
                            game.leaveFromGame(player);
                        }
                    });
                });

        switch (game.getStatus()) {
            case WAITING:
                var bukkitTask = new BukkitRunnable() {
                    int buffer = 1; // fixes the bug where it constantly shows will start in 1 second

                    @Override
                    public void run() {
                        if (game.getStatus() == GameStatus.WAITING) {
                            if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                                String time = game.getFormattedTimeLeft();

                                if (!time.contains("0-1")) {
                                    String[] units = time.split(":");
                                    int seconds = Integer.parseInt(units[1]) + 1;
                                    if (buffer == seconds)
                                        return;
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
                                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(player),
                                                ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                                    }
                                }
                            }
                        } else {
                            this.cancel();
                            runnableCache.remove(player.getUniqueId());
                        }
                    }
                }.runTaskTimer(SBA.getPluginInstance(), 3L, 20L);
                runnableCache.put(player.getUniqueId(), bukkitTask);
                break;
            case RUNNING:
                final var arena = ArenaManager
                        .getInstance()
                        .get(game.getName())
                        .orElseThrow();
                if (SBAConfig.getInstance().getBoolean("game-scoreboard.enabled", true))
                    arena.getScoreboardManager().createScoreboard(player);
                ((Arena) arena).getRotatingGenerators().forEach(generator -> {
                    generator.addViewer(player);
                });
                break;
        }
    }

    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        Logger.trace("SBA EonBedWarsPlayerLeave{}", e);
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    arena.removeVisualsForPlayer(player);
                    arena.removePlayerFromGame(player);
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerRespawnEvent(PlayerRespawnEvent e) {
        final var victim = e.getPlayer();

        if (!Main.isPlayerInGame(victim)) {
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(victim);
        // query arena instance for access to Victim/Killer data
        Tasker.build(() -> {
            ArenaManager
                    .getInstance()
                    .get(game.getName())
                    .ifPresent(arena -> {
                        arena.addVisualsForPlayer(victim);
                    });
        }).afterOneTick().start();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedwarsPlayerPlay(PlayerGameModeChangeEvent e) {
        final var player = e.getPlayer();

        if (!Main.isPlayerInGame(player)) {
            return;
        }
        Logger.trace("Bedwars player {} changed gamemode from {} to {} ", player.getName(), player.getGameMode(),
                e.getNewGameMode());
        if (player.getGameMode() == GameMode.SURVIVAL) {
            Logger.trace("Ignoring gamemode change as they were already playing", player);
            return;
        }
        if (e.getNewGameMode() != GameMode.SURVIVAL) {
            Logger.trace("Ignoring gamemode change as they did not respawn", player);
            return;
        }
        Logger.trace("Player {} started playing", player);
        Tasker.build(() -> {
            final var game = Main.getInstance().getGameOfPlayer(player);
            ShopUtil.applyTeamUpgrades(player, game);
        }).delay(2, TaskerTime.TICKS).start();

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerKilledEvent(PlayerDeathEvent e) {
        final var victim = e.getEntity();

        if (!Main.isPlayerInGame(victim)) {
            return;
        }
        Logger.trace("SBA ENTITY DIED :: {}", victim.getEntityId());
        final var game = Main.getInstance().getGameOfPlayer(victim);
        // query arena instance for access to Victim/Killer data
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    arena.removeVisualsForPlayer(victim);
                    // player has died, increment death counter
                    arena.getPlayerData(victim.getUniqueId())
                            .ifPresent(victimData -> victimData.setDeaths(victimData.getDeaths() + 1));

                    final var killer = victim.getKiller();
                    // killer is present
                    if (killer != null) {
                        Logger.trace("Killer: {} has killed Player: {}", killer.getName(), victim.getName());
                        // get victim game profile
                        final var gVictim = Main.getPlayerGameProfile(victim);

                        if (gVictim == null || gVictim.isSpectator)
                            return;

                        // get victim team to check if it was a final kill or not
                        final var victimTeam = game.getTeamOfPlayer(victim);
                        if (victimTeam != null) {
                            arena.getPlayerData(killer.getUniqueId())
                                    .ifPresent(killerData -> {
                                        Logger.trace("Incrementing killer kills to: {}", killerData.getKills() + 1);
                                        // increment kill counter for killer
                                        killerData.setKills(killerData.getKills() + 1);
                                        if (!victimTeam.isAlive()) {
                                            // increment final kill counter for killer
                                            killerData.setFinalKills(killerData.getFinalKills() + 1);
                                            Bukkit.getPluginManager()
                                                    .callEvent(new SBAFinalKillEvent(game, victim, killer));
                                            if (SBAConfig.getInstance().node("final-kill-lightning").getBoolean(true)) {
                                                victim.getWorld().strikeLightningEffect(victim.getLocation());
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}
