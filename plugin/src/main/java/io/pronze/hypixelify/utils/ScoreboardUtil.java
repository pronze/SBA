package io.pronze.hypixelify.utils;

import com.comphenix.protocol.wrappers.EnumWrappers;
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
import java.util.concurrent.atomic.AtomicReference;

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
        final var scoreboard = p.getScoreboard() == null
                || p.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()
                || p.getScoreboard().getObjective(LOBBY_OBJECTIVE_NAME) == null ?
                Bukkit.getScoreboardManager().getNewScoreboard()
                : p.getScoreboard();
        try {
            var obj = scoreboard.getObjective(LOBBY_OBJECTIVE_NAME);
            if (obj == null) {
                obj = scoreboard.registerNewObjective(LOBBY_OBJECTIVE_NAME, "dummy");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            }
            obj.setDisplayName(elements[0]);
            updateValues(elements, scoreboard, obj);

            game.getRunningTeams().forEach(runningTeam-> {
                final var scoreboardTeam = scoreboard.getTeam(runningTeam.getName()) == null ?
                        scoreboard.registerNewTeam(runningTeam.getName()) :
                        scoreboard.getTeam(runningTeam.getName());

                final var chatColor = TeamColor.fromApiColor(runningTeam.getColor()).chatColor;
                scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                scoreboardTeam.setPrefix(PLAYERTAG_PREFIX
                        .replace("{color}", chatColor.toString())
                        .replace("{team}", ChatColor.BOLD +
                                String.valueOf(runningTeam.getName().charAt(0))));
                if (!Main.isLegacy()) {
                    scoreboardTeam.setColor(chatColor);
                }

                new HashSet<>(scoreboardTeam.getEntries())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(Bukkit::getPlayerExact)
                        .filter(Objects::nonNull)
                        .forEach(player-> {
                            if (!runningTeam.getConnectedPlayers().contains(player)) {
                                scoreboardTeam.removeEntry(player.getName());
                            }
                        });

                runningTeam.getConnectedPlayers()
                        .stream()
                        .map(Player::getName)
                        .filter(playerName-> !scoreboardTeam.hasEntry(playerName))
                        .forEach(scoreboardTeam::addEntry);
            });

            p.setScoreboard(scoreboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setGameScoreboard(Player p, String[] elements, Game game) {
        elements = resizeContent(elements);
        final var scoreboard = p.getScoreboard() == null
                || p.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()
                || p.getScoreboard().getObjective(GAME_OBJECTIVE_NAME) == null ?
                Bukkit.getScoreboardManager().getNewScoreboard()
                : p.getScoreboard();

        boolean exist = true;
        if (scoreboard.getObjective(GAME_OBJECTIVE_NAME) == null) {
            exist = false;
        }

        final var lobbyObjective = scoreboard.getObjective(LOBBY_OBJECTIVE_NAME);
        if (lobbyObjective != null) {
            try {
                lobbyObjective.unregister();
            } catch (Throwable ignored) {}
        }

        try {
            var obj = scoreboard.getObjective(GAME_OBJECTIVE_NAME);
            if (obj == null) {
                obj = scoreboard.registerNewObjective(GAME_OBJECTIVE_NAME, "dummy");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                obj.setDisplayName(BOARD_DISPLAY_NAME);
            }

            if (!exist) {
                game.getRunningTeams().forEach(runningTeam -> {
                    final var team = scoreboard.getTeam(runningTeam.getName()) == null ?
                            scoreboard.registerNewTeam(runningTeam.getName()) :
                            scoreboard.getTeam(runningTeam.getName());

                    final var teamColor = TeamColor.fromApiColor(runningTeam.getColor()).chatColor;
                    team.setAllowFriendlyFire(false);
                    team.setPrefix(teamColor.toString());
                    if (!Main.isLegacy()) {
                        team.setColor(teamColor);
                    }

                    runningTeam.getConnectedPlayers()
                            .stream()
                            .map(Player::getName)
                            .filter(playerName-> !team.hasEntry(playerName))
                            .forEach(team::addEntry);
                });

                if (SBAHypixelify.isProtocolLib()) {
                    //TAB HEALTH
                    try {
                        final var scoreboardObjective = new WrapperPlayServerScoreboardObjective();
                        scoreboardObjective.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);
                        scoreboardObjective.setName(TAB_OBJECTIVE_NAME);
                        scoreboardObjective.sendPacket(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        final var displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
                        displayObjective.setPosition(0);
                        displayObjective.setScoreName(TAB_OBJECTIVE_NAME);
                        displayObjective.sendPacket(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //TAG HEALTH
                    try {
                        final var scoreboardObjective = new WrapperPlayServerScoreboardObjective();
                        scoreboardObjective.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);
                        scoreboardObjective.setName(TAG_OBJECTIVE_NAME);
                        scoreboardObjective.setDisplayName(WrappedChatComponent.fromText("§c♥"));
                        scoreboardObjective.sendPacket(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        final var displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
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

            game.getRunningTeams().forEach(runningTeam -> {
                var team = scoreboard.getTeam(runningTeam.getName());
                final var chatColor = TeamColor.fromApiColor(runningTeam.getColor()).chatColor;
                if (team == null) {
                    team = scoreboard.registerNewTeam(runningTeam.getName());
                    team.setPrefix(PLAYERTAG_PREFIX
                            .replace("{color}", chatColor.toString())
                            .replace("{team}", ChatColor.BOLD +
                                    String.valueOf(team.getName().charAt(0))));
                    team.setAllowFriendlyFire(false);
                    team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

                    if (!Main.isLegacy())
                        team.setColor(chatColor);

                    final var finalTeam = team;
                    runningTeam.getConnectedPlayers()
                            .stream()
                            .map(Player::getName)
                            .filter(playerName-> !finalTeam.hasEntry(playerName))
                            .forEach(team::addEntry);
                }
                final var teamColor = TeamColor.fromApiColor(runningTeam.getColor()).chatColor;
                team.setAllowFriendlyFire(false);
                team.setPrefix(teamColor.toString());
                if (!Main.isLegacy()) {
                    team.setColor(teamColor);
                }

                final var finalTeam = team;
                new HashSet<>(scoreboard.getEntries())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(Bukkit::getPlayerExact)
                        .filter(Objects::nonNull)
                        .forEach(teamPlayer-> {
                            if (!runningTeam.getConnectedPlayers().contains(teamPlayer)) {
                                finalTeam.removeEntry(teamPlayer.getName());
                            }
                        });

            });

            if (p.getScoreboard() == null || !p.getScoreboard().equals(scoreboard))
                p.setScoreboard(scoreboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> makeElementsUnique(List<String> lines) {
        final var sbLines = new ArrayList<String>();
        lines.stream()
                .filter(Objects::nonNull)
                .forEach(ls -> {
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
        final var builder = new StringBuilder(line);
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

        game.getConnectedPlayers()
                .forEach(pl -> {
            var playerHealth = Integer.parseInt(DECIMAL_FORMAT.format(pl.getHealth()));
            if (map.getOrDefault(pl, 0) != playerHealth) {
                try {
                    final var packet = new WrapperPlayServerScoreboardScore();
                    packet.setValue(playerHealth);
                    packet.setScoreName(pl.getName());
                    packet.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);
                    packet.setObjectiveName(TAG_OBJECTIVE_NAME);
                    packet.sendPacket(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    final var packet = new WrapperPlayServerScoreboardScore();
                    packet.setValue(playerHealth);
                    packet.setScoreName(pl.getName());
                    packet.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);
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
        for (int i = 1; i < elements.length; i++) {
            if (elements[i] == null) {
                continue;
            }
            final var score = obj.getScore(elements[i]);
            final var pos = 16 - i;
            if (score.getScore() != pos) {
                score.setScore(pos);
                int finalI = i;
                scoreboard.getEntries()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(entry-> obj.getScore(entry).getScore() == pos && !entry.equalsIgnoreCase(elements[finalI]))
                        .forEach(scoreboard::resetScores);
            }
        }
    }


}
