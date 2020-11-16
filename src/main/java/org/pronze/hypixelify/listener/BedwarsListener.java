package org.pronze.hypixelify.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.scoreboard.ScoreBoard;
import org.pronze.hypixelify.utils.SBAUtil;
import org.pronze.hypixelify.utils.Scheduler;
import org.pronze.hypixelify.utils.ScoreboardUtil;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import java.util.Map;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class BedwarsListener extends AbstractListener {

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        final Game game = e.getGame();
        final Map<Player, Scoreboard> scoreboards = ScoreboardUtil.getScoreboards();
        for (Player player : game.getConnectedPlayers()) {
            if (scoreboards.containsKey(player))
                ScoreboardUtil.removePlayer(player);
        }
        Arena arena = new Arena(game);
        SBAHypixelify.getArenaManager().addArena(game.getName(), arena);
        Scheduler.runTaskLater(() -> arena.getScoreBoard().updateScoreboard(), 2L);
        arena.onGameStarted(e);
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final Game game = e.getGame();
        final Arena arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            arena.onTargetBlockDestroyed(e);

            Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(),
                    () -> {
                        final ScoreBoard board = arena.getScoreBoard();
                        if (board != null) {
                            board.updateScoreboard();
                        }
                    }, 1L);
        }

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnd(BedwarsGameEndEvent e) {
        Game game = e.getGame();
        SBAHypixelify.getArenaManager().removeArena(game.getName());
    }


    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        Game game = e.getGame();
        if (SBAHypixelify.getArenaManager().getArenas().containsKey(game.getName()))
            SBAHypixelify.getArenaManager().getArenas().get(game.getName()).onOver(e);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        Player player = e.getPlayer();
        Game game = e.getGame();
        String message = "&eThe game starts in &c{seconds} &eseconds";

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (player.isOnline() && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player) &&
                        game.getConnectedPlayers().contains(player) &&
                        game.getStatus().equals(GameStatus.WAITING)) {
                    if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                        String time = Main.getGame(game.getName()).getFormattedTimeLeft();
                        if (!time.contains("0-1")) {
                            String[] units = time.split(":");
                            int seconds = Integer.parseInt(units[1]) + 1;
                            if (j == seconds) return;
                            j = seconds;
                            if (seconds < 2) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("{seconds}", String.valueOf(seconds)).replace("seconds", "second")));
                                sendTitle(player, ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                            } else if (seconds < 6) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("{seconds}", String.valueOf(seconds))));
                                sendTitle(player, ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                            } else if (seconds % 10 == 0) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("&c{seconds}", "&6" + seconds)));
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
        final Game game = e.getGame();
        if (game.getStatus() != GameStatus.RUNNING) return;
        final Arena arena = SBAHypixelify.getArena(game.getName());
        if(arena != null){
            arena.getScoreBoard().updateScoreboard();
        }
    }

}
