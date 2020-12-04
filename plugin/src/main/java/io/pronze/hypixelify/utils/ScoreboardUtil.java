package io.pronze.hypixelify.utils;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.packets.WrapperPlayServerScoreboardDisplayObjective;
import io.pronze.hypixelify.packets.WrapperPlayServerScoreboardObjective;
import io.pronze.hypixelify.packets.WrapperPlayServerScoreboardScore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;

import java.text.DecimalFormat;
import java.util.*;

public class ScoreboardUtil {


    public static final String GAME_OBJECTIVE_NAME = "bwa-game";
    public static final String LOBBY_OBJECTIVE_NAME = "bwa-lobby";
    public static final String TAG_OBJECTIVE_NAME = "bwa-tag";
    public static final String TAB_OBJECTIVE_NAME = "bwa-tab";
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##");
    public static final String BOARD_DISPLAY_NAME = "§e§lBED WARS";
    public static final String PLAYERTAG_PREFIX = "{color}{team} ";

    private static final Map<Player, Map<Player, Integer>> player_health = new HashMap<>();

    private static String[] resizeContent(String[] content) {
        String[] elements = Arrays.copyOf(content, 16);

        if (elements[0] == null) {
            elements[0] = BOARD_DISPLAY_NAME;
        }

        if (elements[0].length() > 32)
            elements[0] = elements[0].substring(0, 32);

        for (int i = 1; i < elements.length; i++) {
            final String element = elements[i];
            if (element != null && element.length() > 40)
                elements[i] = element.substring(0, 40);
        }
        return elements;
    }

    public static void removePlayer(Player player) {
        player_health.remove(player);
    }


    public static void setLobbyScoreboard(Player p, String[] elements, Game game) {
        elements = resizeContent(elements);
        Scoreboard scoreboard = p.getScoreboard();
        try {
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard() ||
                    scoreboard.getObjectives().size() != 1) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                scoreboard = p.getScoreboard();
            }

            Objective obj = scoreboard.getObjective(LOBBY_OBJECTIVE_NAME);
            if (obj == null) {
                obj = scoreboard.registerNewObjective(LOBBY_OBJECTIVE_NAME, "dummy");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            }
            obj.setDisplayName(elements[0]);

            updateValues(elements, scoreboard, obj);
            RunningTeam playerteam = game.getTeamOfPlayer(p);

            for (RunningTeam t : game.getRunningTeams()) {
                Team team = scoreboard.getTeam(t.getName());
                if (team == null)
                    team = scoreboard.registerNewTeam(t.getName());
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                ChatColor cl;

                try {
                    cl = TeamColor.fromApiColor(t.getColor()).chatColor;
                    team.setPrefix(PLAYERTAG_PREFIX
                            .replace("{color}", cl.toString()).replace("{team}", ChatColor.BOLD +
                                    String.valueOf(team.getName().charAt(0))));
                    if (!Main.isLegacy())
                        team.setColor(ChatColor.valueOf(cl.name()));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                for(String playerEntry : new HashSet<>(team.getEntries())) {
                    final Player player = Bukkit.getPlayerExact(playerEntry);
                    if (player == null) {
                        continue;
                    }

                    if (!t.getConnectedPlayers().contains(player)) {
                        team.removeEntry(playerEntry);
                    }
                }

                for (Player pl : t.getConnectedPlayers()) {
                    if (!team.hasEntry(pl.getName())) {
                        if (playerteam != null && playerteam.getConnectedPlayers().contains(pl)) {
                            team.addEntry(pl.getName());
                            continue;
                        }
                        String listName = pl.getPlayerListName();
                        if (listName.equals(pl.getName())) {
                            String prefix = team.getPrefix();
                            String suffix = team.getSuffix();
                            String name = prefix + pl.getName() + suffix;
                            if (!name.equals(listName))
                                pl.setPlayerListName(prefix + pl.getName() + suffix);
                        }

                    }
                }
            }

            if (p.getScoreboard() == null || !p.getScoreboard().equals(scoreboard))
                p.setScoreboard(scoreboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setGameScoreboard(Player p, String[] elements, Game game) {
        elements = resizeContent(elements);
        Scoreboard scoreboard = p.getScoreboard();

        boolean exist = true;
        if (scoreboard == null || scoreboard.getObjective(GAME_OBJECTIVE_NAME) == null) {
            exist = false;
        }

        try {
            Objective obj = scoreboard.getObjective(GAME_OBJECTIVE_NAME);
            if (obj == null) {
                obj = scoreboard.registerNewObjective(GAME_OBJECTIVE_NAME, "dummy");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                obj.setDisplayName(BOARD_DISPLAY_NAME);
            }

            if ((p.getScoreboard() == null || !p.getScoreboard().equals(scoreboard)) && !exist) {

                game.getRunningTeams().forEach(t -> {
                    Team team = scoreboard.getTeam(t.getName());
                    if (team == null)
                        team = scoreboard.registerNewTeam(t.getName());
                    team.setAllowFriendlyFire(false);
                    team.setPrefix(org.screamingsandals.bedwars.game.TeamColor
                            .valueOf(t.getColor().name()).chatColor.toString());

                    for (Player pl : t.getConnectedPlayers()) {
                        if (!team.hasEntry(pl.getName()))
                            team.addEntry(pl.getName());
                    }
                });

                if (SBAHypixelify.isProtocolLib()) {
                    //TAB HEALTH
                    try {
                        WrapperPlayServerScoreboardObjective scoreboardObjective =
                                new WrapperPlayServerScoreboardObjective();
                        scoreboardObjective
                                .setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);
                        scoreboardObjective.setName(TAB_OBJECTIVE_NAME);
                        scoreboardObjective.sendPacket(p);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        WrapperPlayServerScoreboardDisplayObjective displayObjective
                                = new WrapperPlayServerScoreboardDisplayObjective();

                        displayObjective.setPosition(0);
                        displayObjective.setScoreName(TAB_OBJECTIVE_NAME);
                        displayObjective.sendPacket(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //TAG HEALTH
                    try {
                        WrapperPlayServerScoreboardObjective scoreboardObjective =
                                new WrapperPlayServerScoreboardObjective();
                        scoreboardObjective.setMode(0);
                        scoreboardObjective.setName(TAG_OBJECTIVE_NAME);
                        scoreboardObjective.setDisplayName(WrappedChatComponent.fromText("§c♥"));
                        scoreboardObjective.sendPacket(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        WrapperPlayServerScoreboardDisplayObjective displayObjective
                                = new WrapperPlayServerScoreboardDisplayObjective();

                        displayObjective.setPosition(2);
                        displayObjective.setScoreName(TAG_OBJECTIVE_NAME);
                        displayObjective.sendPacket(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                updateCustomObjective(p, game);
            }

            updateValues(elements, scoreboard, obj);

            final RunningTeam playerTeam = game.getTeamOfPlayer(p);
            for (RunningTeam team : game.getRunningTeams()) {
                Team scoreboardTeam = scoreboard.getTeam(team.getName());
                ChatColor cl = null;

                try {
                    cl = TeamColor.fromApiColor(team.getColor()).chatColor;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                if (scoreboardTeam == null) {
                    scoreboardTeam = scoreboard.registerNewTeam(team.getName());
                    scoreboardTeam.setPrefix(PLAYERTAG_PREFIX
                            .replace("{color}", cl.toString()).replace("{team}", ChatColor.BOLD +
                                    String.valueOf(team.getName().charAt(0))));
                    scoreboardTeam.setAllowFriendlyFire(false);
                    scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

                    if (!Main.isLegacy())
                        scoreboardTeam.setColor(ChatColor.valueOf(cl.name()));

                    for (Player pl : team.getConnectedPlayers()) {
                        if (!scoreboardTeam.hasEntry(pl.getName())) {
                            if (playerTeam != null && playerTeam.getConnectedPlayers().contains(pl)) {
                                scoreboardTeam.addEntry(pl.getName());
                                continue;
                            }
                            final String listName = pl.getPlayerListName();
                            if (listName.equals(pl.getName())) {
                                final String prefix = scoreboardTeam.getPrefix();
                                final String suffix = scoreboardTeam.getSuffix();
                                String name = prefix + pl.getName() + suffix;
                                if (!name.equals(listName))
                                    pl.setPlayerListName(prefix + pl.getName() + suffix);
                            }
                        }
                    }
                }


            }


            if (p.getScoreboard() == null || !p.getScoreboard().equals(scoreboard))
                p.setScoreboard(scoreboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> makeElementsUnique(List<String> lines) {
        final ArrayList<String> sbLines = new ArrayList<>();
        lines.forEach(ls -> {
            if (ls == null) return;

            final StringBuilder builder = new StringBuilder(ls);

            while (sbLines.contains(builder.toString())) {
                builder.append("§r");
            }

            sbLines.add(builder.toString());
        });
        return sbLines;
    }

    public static String getUniqueString(List<String> lines, String line) {
        if (lines == null || line == null)
            return null;

        final StringBuilder builder = new StringBuilder(line);
        while (lines.contains(builder.toString())) {
            builder.append("§r");
        }

        return builder.toString();
    }

    public static void updateCustomObjective(Player p, Game game) {
        if (!SBAHypixelify.isProtocolLib()) return;

        if (!player_health.containsKey(p))
            player_health.put(p, new HashMap<>());

        final Map<Player, Integer> map = player_health.get(p);

        game.getConnectedPlayers().forEach(pl -> {
            int playerHealth = Integer.parseInt(DECIMAL_FORMAT.format(pl.getHealth()));
            if (map.getOrDefault(pl, 0) != playerHealth) {
                try {
                    WrapperPlayServerScoreboardScore packet = new WrapperPlayServerScoreboardScore();
                    packet.setValue(playerHealth);
                    packet.setScoreName(pl.getName());
                    packet.setObjectiveName(TAG_OBJECTIVE_NAME);
                    packet.sendPacket(pl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    WrapperPlayServerScoreboardScore packet = new WrapperPlayServerScoreboardScore();
                    packet.setValue(playerHealth);
                    packet.setScoreName(pl.getName());
                    packet.setObjectiveName(TAB_OBJECTIVE_NAME);
                    packet.sendPacket(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                map.put(pl, playerHealth);
            }
        });
    }

    public static void updateValues(String[] elements, final Scoreboard scoreboard, Objective obj) {
        /*
            Checks if elements are from scores 15-1 on scoreboard, if not it adds it and resets scores.
         */

        for (int i = 1; i < elements.length; i++) {
            if (elements[i] == null) {
                continue;
            }
            Score score = obj.getScore(elements[i]);
            if (score.getScore() != 16 - i) {
                score.setScore(16 - i);
                for (String string : scoreboard.getEntries()) {
                    if (obj.getScore(string).getScore() == 16 - i && !string.equals(elements[i]))
                        scoreboard.resetScores(string);
                }
            }
        }
    }


}
