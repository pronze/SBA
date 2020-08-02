package org.pronze.hypixelify.scoreboard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.listener.LobbyScoreboard;
import org.pronze.hypixelify.utils.ScoreboardUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.statistics.PlayerStatistic;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScoreBoard {

    private Game game;

    private Arena arena;

    private int tc = 0;

    private Map<String, String> teamstatus;
    private List<String> m_title;
    private int ticks = 0;

    public ScoreBoard(Arena arena) {
        this.arena = arena;
        game = arena.getGame();
        teamstatus = new HashMap<>();
        m_title = LobbyScoreboard.listcolor(Hypixelify.getConfigurator().config.getStringList("lobby-scoreboard.title"));
        new BukkitRunnable() {

            public void run() {
                    ticks += 2;
                    if (ticks == 15000)
                        ticks = 0;
                    if (game.getStatus()!= GameStatus.WAITING && game.getStatus() == GameStatus.RUNNING) {
                        //update title animation every 2 ticks
                        updateTitle();
                        //update scoreboard every 1 second
                        if(ticks % 20 == 0)
                            updateScoreboard();
                    } else {
                        cancel();
                    }
            }
        }.runTaskTimer(Hypixelify.getInstance(), 0L, 2L);
    }

    public void updateTitle(){
        tc++;
        String Title = "";
        if (tc >= m_title.size())
            tc = 0;
        int tcs = 0;
        for (String title : m_title) {
            if (tc == tcs)
                Title = title.replace("{game}", game.getName()).replace("{time}",
                        Main.getGame(game.getName()).getFormattedTimeLeft());
            tcs++;
        }
        if(game.getConnectedPlayers() == null || game.getConnectedPlayers().isEmpty()) return;
        for(Player p : game.getConnectedPlayers()){
            try{
                if(p.getScoreboard().getObjective("bwa-game") == null) continue;
                Scoreboard scoreboard = p.getScoreboard();
                Objects.requireNonNull(scoreboard.getObjective(DisplaySlot.SIDEBAR)).setDisplayName(Title);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void updateScoreboard() {
        List<String> scoreboard_lines;
        List<String> lines = new ArrayList<>();

        int ats = 0;
        int rts = 0;
        for (RunningTeam team : game.getRunningTeams()) {
            if (!team.isDead())
                ats++;
            if (team.getConnectedPlayers().size() > 0)
                rts++;
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
                Team playerTeam = game.getTeamOfPlayer(player);
                lines.clear();
                String tks;
                String ks;
                String fks;
                String dis;
                String bes;

                PlayerStatistic statistic = Main.getPlayerStatisticsManager().getStatistic(player);
                try {
                    tks = String.valueOf(statistic.getCurrentKills() + statistic.getKills());
                    ks = String.valueOf(statistic.getCurrentKills());
                    fks = String.valueOf(statistic.getKills());
                    dis = String.valueOf(statistic.getCurrentDeaths());
                    bes = String.valueOf(statistic.getCurrentDestroyedBeds());
                } catch (Exception e){
                    String nullscore = "0";
                    tks = nullscore;
                    ks = nullscore;
                    fks = nullscore;
                    dis = nullscore;
                    bes = nullscore;
                }
                String p_t_ps = "";
                String p_t = "";
                String p_t_b_s = "";
                if (game.getTeamOfPlayer(player) != null && game.getTeamOfPlayer(player).countConnectedPlayers() > 0) {
                    chatColor = org.screamingsandals.bedwars.game.TeamColor.valueOf(game.getTeamOfPlayer(player).getColor().name()).chatColor;
                    p_t_ps = String.valueOf(game.getTeamOfPlayer(player).getConnectedPlayers().size());
                    p_t =  playerTeam.getName();
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
                            .replace("{team}", p_t).replace("{beds}", bes).replace("{dies}", dis)
                            .replace("{totalkills}", tks).replace("{finalkills}", fks).replace("{kills}", ks)
                            .replace("{time}", Main.getGame(game.getName()).getFormattedTimeLeft())
                            .replace("{formattime}", Main.getGame(game.getName()).getFormattedTimeLeft())
                            .replace("{game}", game.getName()).replace("{date}", date)
                            .replace("{team_bed_status}", p_t_b_s);

                    if(game.isPlayerInAnyTeam(player)){
                        addline.replace("{team_peoples}", p_t_ps).replace("{player_name}", player.getName())
                               .replace("{teams}", String.valueOf(game.getRunningTeams().size())).replace("{color}", chatColor.toString());
                    }
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
                String title = "";
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
        String alive = color("{color} {team} &a\u2714 &8{you}");
        String destroyed = color("{color} {team} &a&f{players}&8 {you}");
        String status = team.isTargetBlockExists() ? alive : destroyed;
        if (team.isDead() && team.getConnectedPlayers().size() <= 0)
            status = color("{color} {team} &c\u2718 {you}");

       String formattedTeam = org.screamingsandals.bedwars.game.TeamColor.valueOf(team.getColor().name()).chatColor.toString()
                + team.getName().charAt(0);
        return status.replace("{bed_status}", getTeamBedStatus(team))
                .replace("{color}", formattedTeam)
                .replace("{team}", ChatColor.WHITE.toString() + team.getName() + ":")
                .replace("{players}", ChatColor.GREEN.toString() + team.getConnectedPlayers().size());
    }
}
