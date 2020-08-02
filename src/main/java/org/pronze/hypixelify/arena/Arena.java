package org.pronze.hypixelify.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.scoreboard.ScoreBoard;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.nms.title.Title;

public class Arena {
    private Game game;
    private ScoreBoard scoreBoard;

    public Game getGame() {
        return this.game;
    }

    public ScoreBoard getScoreBoard() {
        return this.scoreBoard;
    }

    public Arena(Game game) {
        this.game = game;
        if(!Main.isLegacy())
            scoreBoard = new ScoreBoard(this);
    }

    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(e.getPlayer())) return;
        if(!e.getGame().equals(game)) return;

        for(Player p : e.getTeam().getConnectedPlayers()){
            Title.sendTitle(p,"§c§lBED DESTROYED!", "You will no longer respawn!", 0, 40, 20);
        }

    }




    public void onOver(BedwarsGameEndingEvent e) {
        if (e.getGame().getName().equals(game.getName())) {
            if (e.getWinningTeam() != null) {
                Team winner = e.getWinningTeam();
                Map<String, Integer> dataKills = new HashMap<>();
                for(Player player : e.getGame().getConnectedPlayers()){
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

                for(Player pl : e.getWinningTeam().getConnectedPlayers()){
                    Title.sendTitle(pl, "§6§lVICTORY!", "", 0, 90, 0);
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

    public void onGameStarted(BedwarsGameStartedEvent e){
        if(!e.getGame().equals(game)) return;
        for(Player p : e.getGame().getConnectedPlayers()){
            for(String os : Configurator.gamestart_message){
                p.sendMessage(os);
            }
        }
    }





    }

