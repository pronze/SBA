package org.pronze.hypixelify.listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.utils.SBAUtil;
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
        Map<Player, Scoreboard> scoreboards = ScoreboardUtil.getScoreboards();
        for (Player player : game.getConnectedPlayers()) {
            if (scoreboards.containsKey(player))
                ScoreboardUtil.removePlayer(player);
        }
        Arena arena = new Arena(game);
        Hypixelify.getArenaManager().addArena(game.getName(), arena);
        new BukkitRunnable() {
            public void run() {
                if(arena.getScoreBoard() != null)
                    arena.getScoreBoard().updateScoreboard();
            }
        }.runTaskLater(Hypixelify.getInstance(), 2L);

        arena.onGameStarted(e);
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final Game game = e.getGame();
        if (Hypixelify.getArenaManager().getArenas().containsKey(game.getName())) {
            Hypixelify.getArenaManager().getArenas().get(game.getName()).onTargetBlockDestroyed(e);
            new BukkitRunnable() {
                public void run() {
                    if (Hypixelify.getArenaManager().getArenas().containsKey(game.getName())) {
                        if (Hypixelify.getArenaManager().getArenas().get(game.getName()).getScoreBoard() != null) {
                            Hypixelify.getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
                        }
                    }
                }
            }.runTaskLater(Hypixelify.getInstance(), 1L);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnd(BedwarsGameEndEvent e) {
        Game game = e.getGame();
        Hypixelify.getArenaManager().removeArena(game.getName());
    }

    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        Game game = e.getGame();
        if (Hypixelify.getArenaManager().getArenas().containsKey(game.getName()))
            Hypixelify.getArenaManager().getArenas().get(game.getName()).onOver(e);
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
                            if(j == seconds) return;
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
        }.runTaskTimer(Hypixelify.getInstance(), 40L, 20L);
    }


    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        Player player = e.getPlayer();
        ScoreboardUtil.removePlayer(player);
        SBAUtil.removeScoreboardObjective(player);
        Game game = e.getGame();
        if (game.getStatus() != GameStatus.RUNNING) return;
        if (Hypixelify.getArenaManager().getArenas().containsKey(game.getName())) {
            if (Hypixelify.getArenaManager().getArenas().get(game.getName()).getScoreBoard() != null) {
                Hypixelify.getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
            }
        }


    }

}
