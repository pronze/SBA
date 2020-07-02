package org.pronze.hypixelify.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.*;

public class ScoreboardUtil {
    private static Map<Player, Scoreboard> scoreboards = new HashMap<>();

    private static String[] cutUnranked(String[] content) {
        String[] elements = Arrays.copyOf(content, 16);
        if (elements[0] == null)
            elements[0] = "BedWars";
        if (elements[0].length() > 32)
            elements[0] = elements[0].substring(0, 32);
        for (int i = 1; i < elements.length; i++) {
            if (elements[i] != null && elements[i].length() > 40)
                elements[i] = elements[i].substring(0, 40);
        }
        return elements;
    }

    public static Map<Player, Scoreboard> getScoreboards() {
        return scoreboards;
    }

    public static void removePlayer(Player player) {
        if(scoreboards.containsKey(player))
            scoreboards.remove(player);
    }

    public static void setLobbyScoreboard(Player p, String[] elements, Game game) {
        elements = cutUnranked(elements);
        Scoreboard scoreboard = p.getScoreboard();
        try {
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard() ||
                    scoreboard.getObjectives().size() != 1) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                scoreboard = p.getScoreboard();
            }
            if (scoreboard.getObjective("bwa-lobby") == null) {
                scoreboard.registerNewObjective("bwa-lobby", "dummy", "Test");
                scoreboard.getObjective("bwa-lobby").setDisplaySlot(DisplaySlot.SIDEBAR);
            }
            scoreboard.getObjective(DisplaySlot.SIDEBAR).setDisplayName(elements[0]);
            for (int i = 1; i < elements.length; i++) {
                if (elements[i] != null &&
                        Objects.requireNonNull(scoreboard.getObjective(DisplaySlot.SIDEBAR)).getScore(elements[i]).getScore() != 16 - i) {
                    Objects.requireNonNull(scoreboard.getObjective(DisplaySlot.SIDEBAR)).getScore(elements[i]).setScore(16 - i);
                    for (String string : scoreboard.getEntries()) {
                        if (Objects.requireNonNull(scoreboard.getObjective("bwa-lobby")).getScore(string).getScore() == 16 - i &&
                                !string.equals(elements[i]))
                            scoreboard.resetScores(string);
                    }
                }
            }
            for (String entry : scoreboard.getEntries()) {
                boolean toErase = true;
                byte b;
                int j;
                String[] arrayOfString;
                for (j = (arrayOfString = elements).length, b = 0; b < j; ) {
                    String element = arrayOfString[b];
                    if (element != null && element.equals(entry) && Objects.requireNonNull(scoreboard.getObjective("bwa-lobby"))
                            .getScore(entry).getScore() == 16 - Arrays.asList(elements).indexOf(element)) {
                        toErase = false;
                        break;
                    }
                    b++;
                }
                if (toErase)
                    scoreboard.resetScores(entry);
            }
            for (org.screamingsandals.bedwars.api.RunningTeam t : game.getRunningTeams()) {
                Team team = scoreboard.getTeam(t.getName());
                if (team == null)
                    team = scoreboard.registerNewTeam(  t.getName());
                team.setAllowFriendlyFire(false);
                team.setPrefix(t.getColor().toString());
                for (OfflinePlayer pl : t.getGame().getConnectedPlayers()) {
                    if (!team.hasEntry(Objects.requireNonNull(pl.getName())))
                        team.addEntry(pl.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setGameScoreboard(Player p, String[] elements, Game game) {
        boolean exist = scoreboards.containsKey(p);
        if (!exist)
            scoreboards.put(p, Bukkit.getScoreboardManager().getNewScoreboard());
        elements = cutUnranked(elements);
        Scoreboard scoreboard = scoreboards.get(p);
        try {
            if (scoreboard.getObjective("bwa-game") == null) {
                scoreboard.registerNewObjective("bwa-game", "dummy", "test");
                Objects.requireNonNull(scoreboard.getObjective("bwa-game")).setDisplaySlot(DisplaySlot.SIDEBAR);
            }
            ProtocolManager m = ProtocolLibrary.getProtocolManager();
            if ((p.getScoreboard() == null || !p.getScoreboard().equals(scoreboard)) && !exist) {
             // if (Hypixelify.getConfigurator().getBoolean("tag_health", true)) {
             //     try {
             //         PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
             //         packet.getIntegers().write(0, 0);
             //         packet.getStrings().write(0, "bwa-game-list");
             //        // packet.getStrings().write(0, "bwa-game-list");
             //         m.sendServerPacket(p, packet);
             //     } catch (Exception e) {
             //         e.printStackTrace();
             //     }
             //     try {
             //         PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
             //         packet.getIntegers().write(0, 0);
             //         packet.getStrings().write(0, "bwa-game-list");
             //         m.sendServerPacket(p, packet);
             //     } catch (Exception e) {
             //         e.printStackTrace();
             //     }
             // }
             // if (Hypixelify.getConfigurator().getBoolean("tag_health", true)) {
             //     try {
             //         PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
             //         packet.getIntegers().write(0, 0);
             //         packet.getStrings().write(0, "bwa-game-name");
             //        // packet.getStrings().write(0, "bwa-game-name");
             //         m.sendServerPacket(p, packet);
             //     } catch (Exception e) {
             //         e.printStackTrace();
             //     }
           //         try {
           //             PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
          //             packet.getIntegers().write(0, 2);
           //             packet.getStrings().write(0, "bwa-game-name");
            //            m.sendServerPacket(p, packet);
            //       } catch (Exception e) {
            //            e.printStackTrace();
           //         }
           //         try {
           //             PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
          //              packet.getIntegers().write(0, 2);
          //              packet.getStrings().write(0, "bwa-game-name");
          //           //   packet.getStrings().write(0, "§c♥");
           //             m.sendServerPacket(p, packet);
          //          } catch (Exception e) {
          //              e.printStackTrace();
          //          }
          //      }
            }
            Objects.requireNonNull(scoreboard.getObjective(DisplaySlot.SIDEBAR)).setDisplayName(elements[0]);
            for (int i = 1; i < elements.length; i++) {
                if (elements[i] != null &&
                        Objects.requireNonNull(scoreboard.getObjective(DisplaySlot.SIDEBAR)).getScore(elements[i]).getScore() != 16 - i) {
                    Objects.requireNonNull(scoreboard.getObjective(DisplaySlot.SIDEBAR)).getScore(elements[i]).setScore(16 - i);
                    for (String string : scoreboard.getEntries()) {
                        if (Objects.requireNonNull(scoreboard.getObjective("bwa-game")).getScore(string).getScore() == 16 - i &&
                                !string.equals(elements[i]))
                            scoreboard.resetScores(string);
                    }
                }
            }
            for (String entry : scoreboard.getEntries()) {
                boolean toErase = true;
                byte b;
                int j;
                String[] arrayOfString;
                for (j = (arrayOfString = elements).length, b = 0; b < j; ) {
                    String element = arrayOfString[b];
                    if (element != null && element.equals(entry) && Objects.requireNonNull(scoreboard.getObjective("bwa-game"))
                            .getScore(entry).getScore() == 16 - Arrays.asList(elements).indexOf(element)) {
                        toErase = false;
                        break;
                    }
                    b++;
                }
                if (toErase)
                    scoreboard.resetScores(entry);
            }
        //    if (!player_health.containsKey(p))
       //         player_health.put(p, new HashMap<>());
         //   Map<Player, Integer> map = player_health.get(p);
          //  for (Player pl : game.getConnectedPlayers()) {
           //     DecimalFormat format = new DecimalFormat("##");
           //     int j = Integer.parseInt(format.format(pl.getHealth()));
            // if (map.getOrDefault(pl, 0) != j) {
            //     if (Hypixelify.getConfigurator().getBoolean("tab_health", true))
            //         try {
            //             PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
            //             packet.getIntegers().write(0, j);
            //             packet.getStrings().write(0, pl.getName());
            //             packet.getStrings().write(0, "bwa-game-list");
            //             m.sendServerPacket(p, packet);
            //         } catch (Exception e) {
            //             e.printStackTrace();
            //         }
            //     if (Hypixelify.getConfigurator().getBoolean("tag_health", true))
            //         try {
            //             PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_SCORE);
            //             packet.getIntegers().write(0, j);
            //             packet.getStrings().write(0, pl.getName());
            //             packet.getStrings().write(0, "bwa-game-name");
            //             m.sendServerPacket(p, packet);
            //         } catch (Exception e) {
            //             e.printStackTrace();
            //         }
            //     map.put(pl, j);
            // }
         //   }
            String playertag_prefix = "{color}{team} &f| {color}";
            String playertag_suffix = "";
            RunningTeam playerteam = game.getTeamOfPlayer(p);
            for (org.screamingsandals.bedwars.api.RunningTeam t : game.getRunningTeams()) {
                Team team = scoreboard.getTeam( t.getName());
                if (team == null)
                    team = scoreboard.registerNewTeam( t.getName());
                if (!playertag_prefix.equals(""))
                    team.setPrefix(playertag_prefix.replace("{color}", t.getColor().toString()).replace("{team}",
                            t.getName()));
                if (!playertag_suffix.equals(""))
                    team.setSuffix(playertag_suffix.replace("{color}", t.getColor().toString()).replace("{team}",
                            t.getName()));
                team.setAllowFriendlyFire(false);
                for (Player pl : t.getConnectedPlayers()) {
                    if (!team.hasEntry(pl.getName())) {
                        if (playerteam != null && playerteam.getConnectedPlayers().contains(pl)) {
                            team.addEntry(pl.getName());
                            continue;
                        }
                        String listName = pl.getPlayerListName();
                        if (listName == null || listName.equals(pl.getName())) {
                            String prefix = team.getPrefix();
                            String suffix = team.getSuffix();
                            prefix = (prefix == null) ? "" : prefix;
                            suffix = (suffix == null) ? "" : suffix;
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


}
