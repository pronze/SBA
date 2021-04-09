package pronze.hypixelify.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.Scoreboard;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.ScoreboardManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.statistics.PlayerStatisticManager;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.game.Arena;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.DateUtils;
import pronze.lib.core.utils.Logger;

import java.util.*;

public class GameScoreboardManagerImpl implements pronze.hypixelify.api.manager.ScoreboardManager {
    private final Game game;
    private final Arena arena;
    private final Map<UUID, Scoreboard> scoreboardMap = new HashMap<>();
    private final List<String> scoreboard_lines = new ArrayList<>();
    protected BukkitTask updateTask;

    public GameScoreboardManagerImpl(Arena arena) {
        this.arena = arena;
        game = (Game) Main.getInstance().getGameManager().getGame(arena.getGame().getName()).orElseThrow();

        var mDef = LanguageService
                .getInstance()
                .get(MessageKeys.SCOREBOARD_LINES_DEFAULT)
                .toStringList();

        var m5 = LanguageService
                .getInstance()
                .get(MessageKeys.SCOREBOARD_LINES_5)
                .toStringList();

        if (game.countAvailableTeams() >= 5) {
            scoreboard_lines.addAll(m5);
        } else {
            scoreboard_lines.addAll(mDef);
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
        final var statistic = PlayerStatisticManager
                .getInstance()
                .getStatistic(PlayerMapper.wrapPlayer(player));

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
                                .replace("%tier%", arena.getGameTask().getTier()
                                .replace("-", " ") + " in §a" + arena.getGameTask().getFormattedTimeLeft());
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
                    .map(Player::getName)
                    .filter(playerName -> !scoreboardTeam.hasEntry(playerName))
                    .forEach(scoreboardTeam::addEntry);
        });
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

        String formattedTeam = org.screamingsandals.bedwars.game.TeamColor
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

                    final var formattedTeam = org.screamingsandals.bedwars.game.TeamColor
                            .valueOf(team.getColor().name()).chatColor.toString()
                            + team.getName().charAt(0);

                    return destroyed
                            .replace("%color%", formattedTeam)
                            .replace("%team%", ChatColor.WHITE
                                    + team.getName() + ":");
                });
    }
}