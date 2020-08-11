package org.pronze.hypixelify.arena;

import org.bukkit.entity.Player;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.database.GameStorage;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.scoreboard.ScoreBoard;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class Arena {
    public GameTask gameTask;
    private final Game game;
    private final ScoreBoard scoreBoard;
    public double radius;
    private GameStorage storage;

    public GameStorage getStorage(){
        return storage;
    }

    public Arena(Game game) {
        radius = Math.pow(7, 2);
        this.game = game;
        storage = new GameStorage(game);
        scoreBoard = new ScoreBoard(this);
        gameTask = new GameTask(this);
    }


    public Game getGame() {
        return this.game;
    }

    public ScoreBoard getScoreBoard() {
        return this.scoreBoard;
    }

    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        for (Player p : e.getTeam().getConnectedPlayers()) {
            sendTitle(p, Messages.message_bed_destroyed_title, Messages.message_bed_destroyed_subtitle, 0, 40, 20);
        }
    }


    public void onOver(BedwarsGameEndingEvent e) {
        if (e.getGame().getName().equals(game.getName())) {
            scoreBoard.updateScoreboard();
            if (gameTask != null && !gameTask.isCancelled()) {
                gameTask.cancel();
                gameTask = null;
            }
            storage = null;

            if (e.getWinningTeam() != null) {
                Team winner = e.getWinningTeam();
                Map<String, Integer> dataKills = new HashMap<>();
                for (Player player : e.getGame().getConnectedPlayers()) {
                    dataKills.put(player.getDisplayName(),
                            Main.getPlayerStatisticsManager().getStatistic(player).getCurrentKills());
                }
                int kills_1 = 0;
                int kills_2 = 0;
                int kills_3 = 0;
                String kills_1_player = "none";
                String kills_2_player = "none";
                String kills_3_player = "none";
                for (String player : dataKills.keySet()) {
                    int k = dataKills.get(player);
                    if (k > 0 && k > kills_1) {
                        kills_1_player = player;
                        kills_1 = k;
                    }
                }
                for (String player : dataKills.keySet()) {
                    int k = dataKills.get(player);
                    if (k > kills_2 && k <= kills_1 && !player.equals(kills_1_player)) {
                        kills_2_player = player;
                        kills_2 = k;
                    }
                }
                for (String player : dataKills.keySet()) {
                    int k = dataKills.get(player);
                    if (k > kills_3 && k <= kills_2 && !player.equals(kills_1_player) &&
                            !player.equals(kills_2_player)) {
                        kills_3_player = player;
                        kills_3 = k;
                    }
                }

                List<String> WinTeamPlayers = new ArrayList<>();
                for (Player teamplayer : e.getWinningTeam().getConnectedPlayers())
                    WinTeamPlayers.add(teamplayer.getName());

                for (Player pl : e.getWinningTeam().getConnectedPlayers()) {
                    sendTitle(pl, "§6§lVICTORY!", "", 0, 90, 0);
                }
                for (Player player : game.getConnectedPlayers()) {
                    for (String os : Configurator.overstats_message)
                        player.sendMessage(os.replace("{color}", org.screamingsandals.bedwars.game.TeamColor.valueOf(winner.getColor().name()).chatColor.toString())
                                .replace("{win_team}", winner.getName())
                                .replace("{win_team_players}", WinTeamPlayers.toString())
                                .replace("{first_1_kills_player}", kills_1_player)
                                .replace("{first_2_kills_player}", kills_2_player)
                                .replace("{first_3_kills_player}", kills_3_player)
                                .replace("{first_1_kills}", String.valueOf(kills_1))
                                .replace("{first_2_kills}", String.valueOf(kills_2))
                                .replace("{first_3_kills}", String.valueOf(kills_3)));
                }
            }
        }
    }

    public void onGameStarted(BedwarsGameStartedEvent e) {
        if (!e.getGame().equals(game)) return;
        for (Player p : e.getGame().getConnectedPlayers()) {
            for (String os : Configurator.gamestart_message) {
                p.sendMessage(os);
            }
        }

        for (RunningTeam t : game.getRunningTeams()) {
            storage.setTrap(t, false);
        }

    }




}

