package pronze.hypixelify.scoreboard;
import pronze.hypixelify.Configurator;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pronze.hypixelify.message.Messages;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.statistics.PlayerStatistic;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScoreBoard {

    private final Game game;
    private final Arena arena;
    private final String date;
    private final Map<String, String> teamstatus;
    private final List<String> animatedTitle;
    private int ticks = 0;
    private int currentTitlePos = 0;


    public ScoreBoard(Arena arena) {
        this.arena = arena;
        date = new SimpleDateFormat(Configurator.date).format(new Date());
        animatedTitle = SBAHypixelify.getConfigurator().getStringList("lobby-scoreboard.title");
        game = arena.getGame();
        teamstatus = new HashMap<>();
        new BukkitRunnable() {
            public void run() {
                if (game.getStatus() == GameStatus.RUNNING) {
                    ticks += 5;
                    updateCustomObj();
                    if (ticks % 20 == 0)
                        updateScoreboard();
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 5L);
    }

    public void updateCustomObj() {
        game.getConnectedPlayers().forEach(p-> ScoreboardUtil.updateCustomObjective(p, game));
    }

    public void updateScoreboard() {
        List<String> scoreboard_lines;
        final List<String> lines = new ArrayList<>();

        if (currentTitlePos >= animatedTitle.size())
            currentTitlePos = 0;

        final String currentTitle = animatedTitle.get(currentTitlePos);

        if (game.countAvailableTeams() >= 5 && Configurator.Scoreboard_Lines.containsKey("5")) {
            scoreboard_lines = Configurator.Scoreboard_Lines.get("5");
        } else if (Configurator.Scoreboard_Lines.containsKey("default")) {
            scoreboard_lines = Configurator.Scoreboard_Lines.get("default");
        } else {
            scoreboard_lines = Arrays.asList("", "{team_status}", "");
        }


        for (Player player : game.getConnectedPlayers()) {
            final PlayerData playerData = arena.getPlayerDataMap().get(player.getUniqueId());
            ChatColor chatColor = null;
            final RunningTeam playerTeam = game.getTeamOfPlayer(player);
            lines.clear();
            String totalKills;
            String currentKills;
            String finalKills;
            String currentDeaths;
            String currentBedDestroys;

            final PlayerStatistic statistic = Main.getPlayerStatisticsManager().getStatistic(player);
            try {
                totalKills = String.valueOf(statistic.getKills());
                currentKills = String.valueOf(playerData.getKills());
                finalKills = String.valueOf(statistic.getKills());
                currentDeaths = String.valueOf(playerData.getDeaths());
                currentBedDestroys = String.valueOf(playerData.getBedDestroys());
            } catch (Throwable e) {
                final String nullscore = "0";
                totalKills = nullscore;
                currentKills = nullscore;
                finalKills = nullscore;
                currentDeaths = nullscore;
                currentBedDestroys = nullscore;
            }

            String teamName = "";
            String teamStatus = "";

            if (playerTeam != null && playerTeam.countConnectedPlayers() > 0) {
                chatColor = org.screamingsandals.bedwars.game.TeamColor.valueOf(playerTeam.getColor().name()).chatColor;
                teamName = playerTeam.getName();
                teamStatus = getTeamBedStatus(playerTeam);
            }


            for (String ls : scoreboard_lines) {

                if (ls.contains("{team_status}")) {
                    game.getAvailableTeams().forEach(t -> {
                        String you = "";
                        if (playerTeam != null) {
                            if (playerTeam.getName().equals(t.getName())) {
                                you = Messages.message_you;
                            }
                        }
                        if (teamstatus.containsKey(t.getName())) {
                            lines.add(teamstatus.get(t.getName()).replace("{you}", you));
                            return;
                        }
                        lines.add(ls.replace("{team_status}",
                                getTeamStatusFormat(t).replace("{you}", you)));
                    });
                    continue;
                }

                String addline = ls
                        .replace("{team}", teamName).replace("{beds}", currentBedDestroys).replace("{dies}", currentDeaths)
                        .replace("{totalkills}", totalKills).replace("{finalkills}", finalKills).replace("{kills}", currentKills)
                        .replace("{time}", Main.getGame(game.getName()).getFormattedTimeLeft())
                        .replace("{formattime}", Main.getGame(game.getName()).getFormattedTimeLeft())
                        .replace("{game}", game.getName()).replace("{date}", date)
                        .replace("{team_bed_status}", teamStatus)
                        .replace("{tier}", arena.gameTask.getTier().replace("-", " ")
                                + " in §a" + arena.gameTask.getFormattedTimeLeft());

                if (game.isPlayerInAnyTeam(player) && chatColor != null) {
                    addline = addline.replace("{teams}", String.valueOf(game.getRunningTeams().size()))
                            .replace("{color}", chatColor.toString());
                }

                if (lines.contains(addline)) {
                    lines.add(ScoreboardUtil.getUniqueString(lines, addline));
                    continue;
                }
                lines.add(addline);
            }
            List<String> elements = new ArrayList<>();
            elements.add(currentTitle);
            elements.addAll(lines);
            if (elements.size() < 16) {
                int es = elements.size();
                for (int i = 0; i < 16 - es; i++)
                    elements.add(1, null);
            }
            elements = ScoreboardUtil.makeElementsUnique(elements);
            ScoreboardUtil.setGameScoreboard(player, elements.toArray(new String[0]), this.game);
        }
    }

    private String getTeamBedStatus(RunningTeam team) {
        return team.isDead() ? "§c\u2717" :
                "§a\u2713";
    }

    private String getTeamStatusFormat(RunningTeam team) {
        String alive = "{color} {team} §a\u2713 §8{you}";
        String destroyed = "{color} {team} §a§f{players}§8 {you}";

        String status = team.isTargetBlockExists() ? alive : destroyed;

        if (team.isDead() && team.getConnectedPlayers().size() <= 0)
            status = "{color} {team} §c\u2717 {you}";

        String formattedTeam = org.screamingsandals.bedwars.game.TeamColor.valueOf(team.getColor().name()).chatColor.toString()
                + team.getName().charAt(0);
        return status.replace("{bed_status}", getTeamBedStatus(team))
                .replace("{color}", formattedTeam)
                .replace("{team}", ChatColor.WHITE.toString() + team.getName() + ":")
                .replace("{players}", ChatColor.GREEN.toString() + team.getConnectedPlayers().size());
    }

    private String getTeamStatusFormat(Team team) {
        boolean alive = false;
        RunningTeam rt = null;

        for (RunningTeam t : game.getRunningTeams()) {
            if (t.getName().equalsIgnoreCase(team.getName())) {
                alive = true;
                rt = t;
            }
        }

        if (alive) {
            return getTeamStatusFormat(rt);
        }

        String destroyed = "{color} {team} §c\u2718 {you}";


        String formattedTeam = org.screamingsandals.bedwars.game.TeamColor.valueOf(team.getColor().name()).chatColor.toString()
                + team.getName().charAt(0);

        return destroyed.replace("{bed_status}", "§c\u2718")
                .replace("{color}", formattedTeam)
                .replace("{team}", ChatColor.WHITE.toString() + team.getName() + ":");
    }
}