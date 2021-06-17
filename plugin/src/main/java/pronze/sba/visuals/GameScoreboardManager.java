package pronze.sba.visuals;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;
import pronze.sba.MessageKeys;
import pronze.sba.config.SBAConfig;
import pronze.sba.game.Arena;
import pronze.sba.lib.lang.LanguageService;
import pronze.sba.utils.DateUtils;
import pronze.sba.utils.Logger;
import pronze.lib.scoreboards.Scoreboard;
import pronze.lib.scoreboards.ScoreboardManager;

import java.util.*;

public class GameScoreboardManager implements pronze.sba.manager.ScoreboardManager {
    private final Game game;
    private final Arena arena;
    private final Map<UUID, Scoreboard> scoreboardMap = new HashMap<>();
    private final List<String> scoreboard_lines = new ArrayList<>();
    protected BukkitTask updateTask;

    public GameScoreboardManager(Arena arena) {
        this.arena = arena;
        game = (Game) Main.getInstance().getGameByName(arena.getGame().getName());

        if (game.countAvailableTeams() >= 5) {
            scoreboard_lines.addAll(LanguageService
                    .getInstance()
                    .get(MessageKeys.SCOREBOARD_LINES_5)
                    .toStringList());
        } else {
            scoreboard_lines.addAll(LanguageService
                    .getInstance()
                    .get(MessageKeys.SCOREBOARD_LINES_DEFAULT)
                    .toStringList());
        }
        game.getConnectedPlayers().forEach(this::createBoard);
    }

    public void createBoard(Player player) {
        Logger.trace("Creating board for player: {}", player.getName());

        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);
        final var title = LanguageService
                .getInstance()
                .get(MessageKeys.ANIMATED_BEDWARS_TITLE)
                .toStringList();

        final var scoreboard = Scoreboard.builder()
                .animate(true)
                .player(player)
                .displayObjective("bwa-game")
                .updateInterval(20L)
                .animationInterval(2L)
                .animatedTitle(title)
                .updateCallback(board -> {
                    board.setLines(process(player, board));
                    return true;
                })
                .build();
        scoreboardMap.put(player.getUniqueId(), scoreboard);
    }

    public void removeBoard(Player player) {
        if (scoreboardMap.containsKey(player.getUniqueId())) {
            final var scoreboard = scoreboardMap.get(player.getUniqueId());
            if (scoreboard != null) {
                scoreboard.destroy();
                Logger.trace("Destroyed board of player: {}", player.getName());
            }
            scoreboardMap.remove(player.getUniqueId());
        }
    }

    public void destroy() {
        scoreboardMap.values().forEach(Scoreboard::destroy);
        scoreboardMap.clear();
        Logger.trace("Destroyed scoreboard for all players of arena: {}", arena.getGame().getName());
        if (updateTask != null) {
            if (Bukkit.getScheduler().isCurrentlyRunning(updateTask.getTaskId()) || Bukkit.getScheduler().isQueued(updateTask.getTaskId())) {
                updateTask.cancel();
            }
        }
    }

    public List<String> process(Player player, Scoreboard board) {
        final var lines = new ArrayList<String>();
        final var optionalPlayerData = arena.getPlayerData(player.getUniqueId());

        if (optionalPlayerData.isEmpty()) return List.of();
        final var playerData = optionalPlayerData.get();
        final var playerTeam = game.getTeamOfPlayer(player);
        final var statistic = Main.getPlayerStatisticsManager().getStatistic(player);

        if (statistic == null)
            return List.of();

        final var totalKills = String.valueOf(statistic.getKills());
        final var currentKills = String.valueOf(playerData.getKills());
        final var finalKills = String.valueOf(statistic.getKills());
        final var currentDeaths = String.valueOf(playerData.getDeaths());
        final var currentBedDestroys = String.valueOf(playerData.getBedDestroys());
        final var teamName = playerTeam == null ? "" : playerTeam.getName();
        final var teamStatus = playerTeam != null ? getTeamBedStatus(playerTeam) : null;

        scoreboard_lines.stream()
                .filter(Objects::nonNull)
                .forEach(line -> {
                    if (line.contains("%team_status%")) {
                        String finalLine = line;
                        game.getAvailableTeams().forEach(t -> {
                            String you = "";
                            if (playerTeam != null) {
                                if (playerTeam.getName().equalsIgnoreCase(t.getName())) {
                                    you = LanguageService
                                    .getInstance()
                                    .get(MessageKeys.SCOREBOARD_YOU_MESSAGE)
                                    .toString();
                                }
                            }
                            lines.add(finalLine.replace("%team_status%",
                                    getTeamStatusFormat(t).replace("%you%", you)));
                        });
                        return;
                    }
                    line = line
                            .replace("%team%", teamName)
                            .replace("%beds%", currentBedDestroys)
                            .replace("%dies%", currentDeaths)
                            .replace("%totalkills%", totalKills)
                            .replace("%finalkills%", finalKills)
                            .replace("%kills%", currentKills)
                            .replace("%time%", game.getFormattedTimeLeft())
                            .replace("%formattime%", game.getFormattedTimeLeft())
                            .replace("%game%", game.getName())
                            .replace("%date%", DateUtils.getFormattedDate())
                            .replace("%team_bed_status%", teamStatus == null ? "" : teamStatus);

                    if (arena.getGameTask() != null) {
                        line = line
                                .replace("%tier%", arena.getGameTask().getNextTierName()
                                .replace("-", " ") + " in §a" + arena.getGameTask().getTimeLeftForNextEvent());
                    }
                    lines.add(line);
                });

        final var holder = board.getHolder();
        game.getRunningTeams().forEach(team -> {
            if (!holder.hasTeamEntry(team.getName())) {
                holder.addTeam(team.getName(), TeamColor.fromApiColor(team.getColor()).chatColor);
            }
            final var scoreboardTeam = holder.getTeamOrRegister(team.getName());

            scoreboardTeam.getEntries()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Bukkit::getPlayerExact)
                    .filter(Objects::nonNull)
                    .forEach(teamPlayer -> {
                        if (!team.getConnectedPlayers().contains(teamPlayer)) {
                            scoreboardTeam.removeEntry(teamPlayer.getName());
                        }
                    });

            team.getConnectedPlayers()
                    .stream()
                    .filter(player1 -> !arena.isPlayerHidden(player1))
                    .map(Player::getName)
                    .filter(playerName -> !scoreboardTeam.hasEntry(playerName))
                    .forEach(scoreboardTeam::addEntry);
        });

        if (!arena.getInvisiblePlayers().isEmpty()) {
            arena.getInvisiblePlayers().forEach(invisiblePlayer -> {
                final var team = game.getTeamOfPlayer(invisiblePlayer);
                if (team != null && playerTeam != team) {
                    board.getHolder().getTeamEntry(teamName).ifPresent(teamEntry -> {
                        if (teamEntry.hasEntry(invisiblePlayer.getName())) {
                            teamEntry.removeEntry(invisiblePlayer.getName());
                        }
                    });
                }
            });
        }
        return lines;
    }

    private String getTeamBedStatus(RunningTeam team) {
        return team.isDead() ?
                SBAConfig.getInstance().node("team-status", "target-destroyed").getString("§c\u2717") :
                SBAConfig.getInstance().node("team-status", "target-exists").getString("§a\u2713");
    }

    private String getTeamStatusFormat(RunningTeam team) {
        String alive = SBAConfig
                .getInstance()
                .node("team-status", "alive")
                .getString("%color% %team% §a\u2713 §8%you%");

        String destroyed = SBAConfig
                .getInstance()
                .node("team-status", "destroyed")
                .getString("%color% %team% §a§f%players%§8 %you%");

        String status = team.isTargetBlockExists() ? alive : destroyed;

        String formattedTeam = TeamColor
                .valueOf(team.getColor().name())
                .chatColor
                .toString()
                + team.getName().charAt(0);

        return status
                .replace("%bed_status%", getTeamBedStatus(team))
                .replace("%color%", formattedTeam)
                .replace("%team%", ChatColor.WHITE + team.getName() + ":")
                .replace("%players%", ChatColor.GREEN.toString() + team.getConnectedPlayers().size());
    }

    private String getTeamStatusFormat(Team team) {
        return game
                .getRunningTeams()
                .stream()
                .filter(t -> t.getName().equalsIgnoreCase(team.getName()))
                .map(this::getTeamStatusFormat)
                .findAny()
                .orElseGet(() -> {
                    final var destroyed = SBAConfig
                            .getInstance()
                            .node("team-status", "eliminated")
                            .getString("%color% %team% §c\u2718 %you%");

                    final var formattedTeam = TeamColor
                            .valueOf(team.getColor().name()).chatColor.toString()
                            + team.getName().charAt(0);

                    return destroyed
                            .replace("%color%", formattedTeam)
                            .replace("%team%", ChatColor.WHITE
                                    + team.getName() + ":");
                });
    }
}