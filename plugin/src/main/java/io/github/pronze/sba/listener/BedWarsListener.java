package io.github.pronze.sba.listener;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBAFinalKillEvent;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BedWarsListener implements Listener {
    private final Map<UUID, BukkitTask> runnableCache = new HashMap<>();

    @OnPostEnable
    public void registerListener() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
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
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena)arena).onTargetBlockDestroyed(e));
    }

    @EventHandler
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
                .ifPresent(arena -> ((Arena)arena).onOver(e));
    }

    @EventHandler
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        final var wrappedPlayer = PlayerMapper
                .wrapPlayer(player)
                .as(SBAPlayerWrapper.class);
        final var task = runnableCache.get(player.getUniqueId());
        final var game = (Game) e.getGame();
        if (task != null) {
            SBAUtil.cancelTask(task);
        }
        SBA.getInstance()
                .getPartyManager()
                .getPartyOf(wrappedPlayer)
                .ifPresent(party -> {
                    if (!wrappedPlayer.equals(party.getPartyLeader())) {
                        Message.of(LangKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                .send(wrappedPlayer);
                        return;
                    }
                    if (party.getMembers().size() == 1) {
                        Message.of(LangKeys.PARTY_MESSAGE_NO_PLAYERS_TO_WARP)
                                .send(wrappedPlayer);
                        return;
                    }

                    Message.of(LangKeys.PARTY_MESSAGE_WARP)
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
                                            Message.of(LangKeys.PARTY_MESSAGE_WARP)
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
                                        Main.getInstance().getGameOfPlayer(member.getInstance()).leaveFromGame(member.getInstance());
                                    }
                                    PlayerUtils.teleportPlayer(member.getInstance(), leaderLocation);
                                    Message.of(LangKeys.PARTY_MESSAGE_LEADER_JOIN_LEAVE)
                                            .send(PlayerMapper.wrapPlayer(member.getInstance()));
                                });
                    }
                });

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
                                        var message = Message.of(LangKeys.GAME_STARTS_IN_MESSAGE)
                                                .placeholder("seconds", String.valueOf(seconds))
                                                .asComponent();
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
                }.runTaskTimer(SBA.getPluginInstance(), 3L, 20L);
                runnableCache.put(player.getUniqueId(), bukkitTask);
                break;
            case RUNNING:
                final var arena = ArenaManager
                        .getInstance()
                        .get(game.getName())
                        .orElseThrow();

                GameScoreboardManager.getInstance().addViewer(player);
                arena.getRotatingGenerators().forEach(generator -> generator.addViewer(player));
                break;
        }
    }

    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());
        final var game = e.getGame();
        GameScoreboardManager.getInstance().removeViewer(player);
        if (task != null) {
            SBAUtil.cancelTask(task);
        }
        runnableCache.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerKilledEvent(PlayerDeathEvent e) {
        final var victim = e.getEntity();
        if (!Main.isPlayerInGame(victim)) {
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(victim);
        // query arena instance for access to Victim/Killer data
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    // player has died, increment death counter
                    arena.getPlayerData(victim.getUniqueId())
                            .ifPresent(victimData -> victimData.setDeaths(victimData.getDeaths() + 1));

                    final var killer = victim.getKiller();
                    //killer is present
                    if (killer != null) {
                        Logger.trace("Killer: {} has killed Player: {}", killer.getName(), victim.getName());
                        // get victim game profile
                        final var gVictim = Main.getPlayerGameProfile(victim);

                        if (gVictim == null || gVictim.isSpectator) return;

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
                                            Bukkit.getPluginManager().callEvent(new SBAFinalKillEvent(game, victim, killer));
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
