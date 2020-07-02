package org.pronze.hypixelify.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.utils.ScoreboardUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScoreBoard {

    private Game game;

    private Arena arena;

    private int tc = 0;

    private Map<String, String> teamstatus;


    public ScoreBoard(Arena arena) {
        this.arena = arena;
        this.game = arena.getGame();
        this.teamstatus = new HashMap<>();

        (new BukkitRunnable() {
            int i = 2;

            public void run() {
                this.i--;
                if (this.i <= 0) {
                    this.i = 2;
                    if (ScoreBoard.this.game.getStatus() != GameStatus.WAITING && ScoreBoard.this.game.getStatus() == GameStatus.RUNNING) {
                        ScoreBoard.this.updateScoreboard();
                    } else {
                        cancel();
                    }
                    return;
                }
            }
        }).runTaskTimer(Hypixelify.getInstance(), 0L, 1L);
    }


    public void updateScoreboard() {
        List<String> scoreboard_lines;
        this.tc++;
        List<String> lines = new ArrayList<>();

        int ats = 0;
        int rts = 0;
        for (RunningTeam team : game.getRunningTeams()) {
            if (!team.isDead())
                ats++;
            if (team.getConnectedPlayers().size() > 0)
                rts++;
        }

        String Title = "";
        if (this.tc >= Hypixelify.getConfigurator().getStringList("lobby-scoreboard.title").size())
            this.tc = 0;
        int tcs = 0;
        for (String title : Hypixelify.getConfigurator().getStringList("lobby-scoreboard.title")) {
            if (this.tc == tcs)
                Title = title.replace("{game}", this.game.getName()).replace("{time}",
                        Main.getGame(game.getName()).getFormattedTimeLeft());
            tcs++;
        }
        String teams = String.valueOf(game.getRunningTeams().size());
        if (Configurator.Scoreboard_Lines.containsKey(teams)) {
            scoreboard_lines = Configurator.Scoreboard_Lines.get(teams);
        } else if (Configurator.Scoreboard_Lines.containsKey("default")) {
            scoreboard_lines = Configurator.Scoreboard_Lines.get("default");
        } else {
            scoreboard_lines = Arrays.asList("", "{team_status}", "");
        }
        int alive_players = 0;
        for (Player p : game.getConnectedPlayers()) {
            if (game.isPlayerInAnyTeam(p))
                alive_players++;
        }
        for (Player player : game.getConnectedPlayers()) {
            ChatColor chatColor = null;
            Team playerteam = game.getTeamOfPlayer(player);
            lines.clear();
            String tks = "0";
            String ks = "0";
            String fks = "0";
            String dis = "0";
            String bes = "0";
            Map<String, Integer> totalkills = this.arena.getPlayerGameStorage().getPlayerTotalKills();
            Map<String, Integer> kills = this.arena.getPlayerGameStorage().getPlayerKills();
            Map<String, Integer> finalkills = this.arena.getPlayerGameStorage().getPlayerFinalKills();
            Map<String, Integer> dies = this.arena.getPlayerGameStorage().getPlayerDies();
            Map<String, Integer> beds = this.arena.getPlayerGameStorage().getPlayerBeds();
            tks = String.valueOf(totalkills.getOrDefault(player.getName(), 0));
            ks = String.valueOf(kills.getOrDefault(player.getName(), 0));
            fks = String.valueOf(finalkills.getOrDefault(player.getName(), 0));
            dis = String.valueOf(dies.getOrDefault(player.getName(), 0));
            bes = String.valueOf(beds.getOrDefault(player.getName(), 0));
            String p_t_ps = "";
            String p_t = "";
            String p_t_b_s = "";
            if (game.getTeamOfPlayer(player) != null && game.getTeamOfPlayer(player).countConnectedPlayers() > 0) {
                chatColor = org.screamingsandals.bedwars.game.TeamColor.valueOf(game.getTeamOfPlayer(player).getColor().name()).chatColor;
                p_t_ps = String.valueOf(game.getTeamOfPlayer(player).getConnectedPlayers().size());
                p_t = game.getTeamOfPlayer(player).getName();
                p_t_b_s = getTeamBedStatus(game.getTeamOfPlayer(player));
            }
            for (String ls : scoreboard_lines) {
                if (ls.contains("{team_status}")) {
                    for (RunningTeam t : game.getRunningTeams()) {
                        String you = "";
                        if (game.getTeamOfPlayer(player) != null)
                            if (game.getTeamOfPlayer(player) == t) {
                                you = color("&7YOU");
                            } else {
                                you = "";
                            }
                        if (this.teamstatus.containsKey(t.getName())) {
                            lines.add(this.teamstatus.get(t.getName()).replace("{you}", you));
                            continue;
                        }
                        lines.add(ls.replace("{team_status}",
                                getTeamStatusFormat(t).replace("{you}", you)));
                    }
                    continue;
                }
                String date = (new SimpleDateFormat("MM/dd/yy")).format(new Date());

                assert chatColor != null;
                String addline = ls
                        .replace("{remain_teams}", String.valueOf(rts)).replace("{alive_teams}", String.valueOf(ats))
                        .replace("{alive_players}", String.valueOf(alive_players))
                        .replace("{teams}", String.valueOf(game.getRunningTeams().size())).replace("{color}", chatColor.toString())
                        .replace("{team_peoples}", p_t_ps).replace("{player_name}", player.getName())
                        .replace("{team}", p_t).replace("{beds}", bes).replace("{dies}", dis)
                        .replace("{totalkills}", tks).replace("{finalkills}", fks).replace("{kills}", ks)
                        .replace("{time}", Main.getGame(game.getName()).getFormattedTimeLeft())
                        .replace("{formattime}", Main.getGame(game.getName()).getFormattedTimeLeft())
                        .replace("{game}", this.game.getName()).replace("{date}", date)
                        .replace("{team_bed_status}", p_t_b_s);
                for (RunningTeam t : game.getRunningTeams()) {
                    if (addline.contains("{team_" + t.getName() + "_status}")) {
                        String stf = getTeamStatusFormat(t);
                        if (game.getTeamOfPlayer(player) == null) {
                            stf = stf.replace("{you}", "");
                        } else if (game.getTeamOfPlayer(player) == t) {
                            stf = stf.replace("{you}", "&7YOU");
                        } else {
                            stf = stf.replace("{you}", "");
                        }
                        addline = addline.replace("{team_" + t.getName() + "_status}", stf);
                    }
                    if (addline.contains("{team_" + t.getName() + "_bed_status}"))
                        addline = addline.replace("{team_" + t.getName() + "_bed_status}",
                                getTeamBedStatus(t));
                    if (addline.contains("{team_" + t.getName() + "_peoples}"))
                        addline = addline.replace("{team_" + t.getName() + "_peoples}", String.valueOf(t.getConnectedPlayers().size()));
                }
                if (lines.contains(addline)) {
                    lines.add(conflict(lines, addline));
                    continue;
                }
                lines.add(addline);
            }
            String title = Title;
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                title = PlaceholderAPI.setPlaceholders(player, title);
            List<String> elements = new ArrayList<>();
            elements.add(title);
            elements.addAll(lines);
            if (elements.size() < 16) {
                int es = elements.size();
                for (int i = 0; i < 16 - es; i++)
                    elements.add(1, null);
            }
            List<String> ncelements = elementsPro(elements);
            String[] scoreboardelements = ncelements.toArray(new String[0]);
            ScoreboardUtil.setGameScoreboard(player, scoreboardelements, this.game);
        }
    }


    private List<String> elementsPro(List<String> lines) {
        ArrayList<String> nclines = new ArrayList<>();
        for (String ls : lines) {
            String l = ls;
            if (l != null) {
                if (nclines.contains(l)) {
                    for (int i = 0; i == 0; ) {
                        l = l + "§r";
                        if (!nclines.contains(l)) {
                            nclines.add(l);
                            break;
                        }
                    }
                    continue;
                }
                nclines.add(l);
                continue;
            }
            nclines.add(l);
        }
        return nclines;
    }

    private String conflict(List<String> lines, String line) {
        String l = line;
        for (int i = 0; i == 0; ) {
            l = l + "§r";
            if (!lines.contains(l))
                return l;
        }
        return l;
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private String getTeamBedStatus(RunningTeam team) {
        return team.isDead() ? color("&c\u2718") :
                color("&a\u2714");
    }

    private String getTeamStatusFormat(RunningTeam team) {
        String alive = color("{color} {team} &a\u2714 &8(&f&l{players}&8) {you})");
        String destroyed = color("{color} {team} &c\u2718 &8(&f&l{players}&8) {you}");
        String status = team.isTargetBlockExists() ? alive : destroyed;
        if (team.isDead() && team.getConnectedPlayers().size() <= 0)
            status = color("{color} {team} &c\u2718 &8(&f&l{players}&8) {you}");

        String temp = team.isAlive() ? String.valueOf(team.getConnectedPlayers().size()) : "0";
        return status.replace("{bed_status}", getTeamBedStatus(team))
                .replace("{color}", org.screamingsandals.bedwars.game.TeamColor.valueOf(team.getColor().name()).chatColor.toString())
                .replace("{team}", team.getName())
                .replace("{players}", (new StringBuilder(temp)));
    }
}
