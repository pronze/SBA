package org.pronze.hypixelify.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.pronze.hypixelify.SBAHypixelify;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;

import java.text.DecimalFormat;
import java.util.*;

public class ScoreboardUtil {
    private static final Map<Player, Scoreboard> scoreboards = new HashMap<>();
    private static final Map<Player, Map<Player, Integer>> player_health = new HashMap<>();

    public static final String GAME_OBJECTIVE_NAME = "bwa-game";
    public static final String LOBBY_OBJECTIVE_NAME = "bwa-lobby";
    public static final String TAG_OBJECTIVE_NAME = "bwa-tag";
    public static final String TAB_OBJECTIVE_NAME = "bwa-tab";

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##");

    public static final String BOARD_DISPLAY_NAME = "§e§lBED WARS";

    public static final String PLAYERTAG_PREFIX = "{color}{team} ";


    private static String[] resizeContent(String[] content) {
        String[] elements = Arrays.copyOf(content, 16);

        if(elements[0] == null){
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

    public static Map<Player, Scoreboard> getScoreboards() {
        return scoreboards;
    }

    public static void removePlayer(Player player) {
        scoreboards.remove(player);
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

            for (RunningTeam t : game.getRunningTeams()) {
                Team team = scoreboard.getTeam(t.getName());
                if (team == null)
                    team = scoreboard.registerNewTeam(t.getName());
                team.setAllowFriendlyFire(false);
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                for (Player pl : t.getGame().getConnectedPlayers()) {
                    if (!team.hasEntry(Objects.requireNonNull(pl.getName())))
                        team.addEntry(pl.getName());
                }
            }

            if (p.getScoreboard() == null || !p.getScoreboard().equals(scoreboard))
                p.setScoreboard(scoreboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void setGameScoreboard(Player p, String[] elements, Game game) {
        boolean exist = scoreboards.containsKey(p);
        if (!exist)
            scoreboards.put(p, Bukkit.getScoreboardManager().getNewScoreboard());
        elements = resizeContent(elements);
        Scoreboard scoreboard = scoreboards.get(p);
        try {
            Objective obj = scoreboard.getObjective(GAME_OBJECTIVE_NAME);
            if (obj == null) {
                obj = scoreboard.registerNewObjective(GAME_OBJECTIVE_NAME, "dummy");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                obj.setDisplayName(BOARD_DISPLAY_NAME);
            }
            if ((p.getScoreboard() == null || !p.getScoreboard().equals(scoreboard)) && !exist) {
                game.getRunningTeams().forEach(t->{
                    Team team = scoreboard.getTeam(t.getName());
                    if (team == null)
                        team = scoreboard.registerNewTeam(t.getName());
                    team.setAllowFriendlyFire(false);
                    team.setPrefix(org.screamingsandals.bedwars.game.TeamColor.valueOf(t.getColor().name()).chatColor.toString());

                    for (Player pl : t.getConnectedPlayers()) {
                        if (!team.hasEntry(pl.getName()))
                            team.addEntry(pl.getName());
                    }
                });

                if (SBAHypixelify.isProtocolLib()) {
                    ProtocolManager m = ProtocolLibrary.getProtocolManager();
                    //TAB HEALTH
                    try {
                        PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
                        packet.getIntegers().write(0, 0);
                        packet.getStrings().write(0, TAG_OBJECTIVE_NAME);
                        if(Main.isLegacy())
                            packet.getStrings().write(1, TAG_OBJECTIVE_NAME);
                        m.sendServerPacket(p, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
                        packet.getIntegers().write(0, 0);
                        packet.getStrings().write(0, TAB_OBJECTIVE_NAME);
                        m.sendServerPacket(p, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //TAG HEALTH
                    try {
                        PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
                        packet.getIntegers().write(0, 0);
                        packet.getStrings().write(0, TAB_OBJECTIVE_NAME);
                        if(Main.isLegacy())
                            packet.getStrings().write(1, "§c♥");
                        else
                            packet.getChatComponents().write(0, WrappedChatComponent.fromText("§c♥"));
                        m.sendServerPacket(p, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
                        packet.getIntegers().write(0, 2);
                        packet.getStrings().write(0, TAB_OBJECTIVE_NAME);
                        m.sendServerPacket(p, packet);
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

                //Increase performance by updating if only the team isn't registered, seems logical for now
                //TODO: test this
                //TODO: Hide spectators (Configurable Option)
                if (scoreboardTeam == null) {
                    scoreboardTeam = scoreboard.registerNewTeam(team.getName());
                    final String cl = org.screamingsandals.bedwars.game.TeamColor.valueOf(team.getColor().name()).chatColor.toString();
                    scoreboardTeam.setPrefix(PLAYERTAG_PREFIX.replace("{color}", cl).replace("{team}", ChatColor.BOLD +
                            String.valueOf(team.getName().charAt(0))));
                    scoreboardTeam.setAllowFriendlyFire(false);
                    scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

                    if (!Main.isLegacy())
                        scoreboardTeam.setColor(ChatColor.valueOf(cl));

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

        final ProtocolManager m = ProtocolLibrary.getProtocolManager();

        if (!player_health.containsKey(p))
            player_health.put(p, new HashMap<>());

        final Map<Player, Integer> map = player_health.get(p);

        game.getConnectedPlayers().forEach(pl-> {
            int j = Integer.parseInt(DECIMAL_FORMAT.format(pl.getHealth()));
            if (map.getOrDefault(pl, 0) != j) {
                try {
                    PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
                    packet.getIntegers().write(0, j);
                    packet.getStrings().write(0, pl.getName());
                    packet.getStrings().write(1, TAG_OBJECTIVE_NAME);
                    m.sendServerPacket(p, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
                    packet.getIntegers().write(0, j);
                    packet.getStrings().write(0, pl.getName());
                    packet.getStrings().write(1, TAB_OBJECTIVE_NAME);
                    m.sendServerPacket(p, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                map.put(pl, j);
            }
        });
    }

    public static void updateValues(String[] elements, Scoreboard scoreboard, Objective obj){
        /*
            Checks if elements are from scores 15-1 on scoreboard, if not it adds it and resets scores.
         */

        for (int i = 1; i < elements.length; i++) {
            if(elements[i] == null){
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

        for (String entry : scoreboard.getEntries()) {
            boolean toErase = true;
            int b;
            int j;

            for (j = elements.length, b = 0; b < j; b++) {
                String element = elements[b];
                if (element != null && element.equals(entry)
                        && obj.getScore(entry).getScore() == 16 - Arrays.asList(elements).indexOf(element)) {
                    toErase = false;
                    break;
                }
            }
            if (toErase)
                scoreboard.resetScores(entry);
        }
    }


}
