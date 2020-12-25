package io.pronze.hypixelify.listener;

import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.scoreboard.ScoreBoard;
import io.pronze.hypixelify.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import io.pronze.hypixelify.game.Arena;
import io.pronze.hypixelify.utils.SBAUtil;
import io.pronze.hypixelify.utils.ShopUtil;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.GameStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class BedwarsListener implements Listener {

    private final Map<UUID, BukkitTask> runnableCache = new HashMap<>();

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        final var game = e.getGame();
        final var arena = new Arena(game);
        SBAHypixelify.addArena(arena);
        Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), ()-> arena.getScoreboard().updateScoreboard(), 2L);
        arena.onGameStarted(e);
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final var plugin = event.getPlugin().getName();
        final var pluginManager = Bukkit.getServer().getPluginManager();

        if (plugin.equalsIgnoreCase(Main.getInstance().getName())) {
            pluginManager.disablePlugin(SBAHypixelify.getInstance());
            pluginManager.enablePlugin(SBAHypixelify.getInstance());
        }
    }


    @EventHandler
    public void onPreRebuild(BedwarsPreRebuildingEvent e){
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if(arena != null){
            arena.onPreRebuildingEvent();
        }
    }



    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            arena.onTargetBlockDestroyed(e);

            Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> {
                final ScoreBoard board = arena.getScoreboard();
                if (board != null) {
                    board.updateScoreboard();
                }
            }, 1L);
        }

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostRebuildingEvent(BedwarsPostRebuildingEvent e) {
        final var game = e.getGame();
        SBAHypixelify.removeArena(game.getName());
    }


    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if (arena != null)
            arena.onOver(e);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        final var game = Main.getGame(e.getGame().getName());

        final var task = runnableCache.get(player.getUniqueId());


        if (task != null) {
            if (!task.isCancelled()) {
                try {
                    task.cancel();
                } catch (Throwable ignored) {}
            }
        }

        runnableCache.put(player.getUniqueId(), new BukkitRunnable() {
            int buffer = 1; //fixes the bug where it constantly shows will start in 1 second

            public void run() {
                if ( player.isOnline() &&
                        game.getConnectedPlayers().contains(player) &&
                        game.getStatus() == GameStatus.WAITING) {

                    if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                        String time = game.getFormattedTimeLeft();

                        if (!time.contains("0-1")) {
                            String[] units = time.split(":");
                            int seconds = Integer.parseInt(units[1]) + 1;
                            if (buffer == seconds) return;
                            buffer = seconds;
                            if (seconds <= 10) {
                                String message = ShopUtil
                                        .translateColors(SBAHypixelify.getConfigurator().getString("message.game-starts-in")
                                                .replace("{seconds}", String.valueOf(seconds)));

                                message = seconds == 1 ? message.replace("seconds", "second") : message;
                                player.sendMessage(message);
                                sendTitle(player, ShopUtil
                                        .translateColors("&c" + seconds), "", 0, 20, 0);
                            }
                        }
                    }
                } else {
                    this.cancel();
                    runnableCache.remove(player.getUniqueId());
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 3L, 20L));
    }


    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());


        if (task != null) {
            if (Bukkit.getScheduler().isQueued(task.getTaskId()) ||
                    !task.isCancelled()) {
                try {
                    task.cancel();
                } catch (Throwable ignored) {}
            }
        }
        runnableCache.remove(player.getUniqueId());

        ScoreboardUtil.removePlayer(player);
        SBAUtil.removeScoreboardObjective(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        final var game = e.getGame();

        if (game.getStatus() == GameStatus.RUNNING) {
            final var arena = SBAHypixelify.getArena(game.getName());
            if (arena != null) {
                arena.getScoreboard().updateScoreboard();
            }
        }
    }

    @EventHandler
    public void onBedWarsPlayerKilledEvent(BedwarsPlayerKilledEvent e){
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());

        if (arena != null) {
            arena.onBedWarsPlayerKilled(e);
        }
    }

}
