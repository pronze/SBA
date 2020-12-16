package io.pronze.hypixelify.listener;

import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.message.Messages;
import io.pronze.hypixelify.scoreboard.ScoreBoard;
import io.pronze.hypixelify.utils.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import io.pronze.hypixelify.game.Arena;
import io.pronze.hypixelify.utils.SBAUtil;
import io.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class BedwarsListener implements Listener {

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        final Game game = e.getGame();
        final Arena arena = new Arena(game);
        SBAHypixelify.addArena(arena);
        Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), ()-> arena.getScoreboard().updateScoreboard(), 2L);
        arena.onGameStarted(e);
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final String plugin = event.getPlugin().getName();
        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();

        if (plugin.equalsIgnoreCase("BedWars")) {
            pluginManager.disablePlugin(SBAHypixelify.getInstance());
            pluginManager.enablePlugin(SBAHypixelify.getInstance());
        }
    }


    @EventHandler
    public void onPreRebuild(BedwarsPreRebuildingEvent e){
        final Game game = e.getGame();
        final Arena arena = SBAHypixelify.getArena(game.getName());
        if(arena != null){
            arena.onPreRebuildingEvent();
        }
    }



    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final Game game = e.getGame();
        final Arena arena = SBAHypixelify.getArena(game.getName());
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
        Game game = e.getGame();
        SBAHypixelify.removeArena(game.getName());
    }


    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        final Game game = e.getGame();
        final Arena arena = SBAHypixelify.getArena(game.getName());
        if (arena != null)
            arena.onOver(e);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        final Player player = e.getPlayer();
        final org.screamingsandals.bedwars.game.Game game = Main.getGame(e.getGame().getName());

        new BukkitRunnable() {
            int buffer = 300; //fixes the bug where it constantly shows will start in 1 second

            public void run() {
                if (player.isOnline() &&
                        game.getConnectedPlayers().contains(player) &&
                        game.getStatus() == GameStatus.WAITING) {
                    if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                        String time = game.getFormattedTimeLeft();
                        if (!time.contains("0-1")) {
                            String[] units = time.split(":");
                            int seconds = Integer.parseInt(units[1]) + 1;
                            if (buffer == seconds) return;
                            buffer = seconds;
                            if (seconds < 2) {
                                player.sendMessage(ShopUtil
                                        .translateColors(Messages.message_game_starts_in.replace("{seconds}", String.valueOf(seconds))
                                                .replace("seconds", "second")));
                                sendTitle(player, ShopUtil
                                        .translateColors("&c" + seconds), "", 0, 20, 0);
                            } else if (seconds < 6) {
                                player.sendMessage(ShopUtil
                                        .translateColors(Messages.message_game_starts_in.replace("{seconds}", String.valueOf(seconds))));
                                sendTitle(player, ShopUtil
                                        .translateColors("&c" + seconds), "", 0, 20, 0);
                            }
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 40L, 20L);
    }


    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final Player player = e.getPlayer();
        ScoreboardUtil.removePlayer(player);
        SBAUtil.removeScoreboardObjective(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        final Game game = e.getGame();
        if (game.getStatus() != GameStatus.RUNNING) return;
        final Arena arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            arena.getScoreboard().updateScoreboard();
        }
    }

    @EventHandler
    public void onBedWarsPlayerKilledEvent(BedwarsPlayerKilledEvent e){
        final Game game = e.getGame();

        final Arena arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            arena.onBedWarsPlayerKilled(e);
        }
    }

}
