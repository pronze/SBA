package pronze.hypixelify.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.Scoreboard;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.ScoreboardManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.statistics.PlayerStatisticManager;
import pronze.hypixelify.Configurator;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.game.ArenaImpl;
import pronze.hypixelify.packets.WrapperPlayServerScoreboardDisplayObjective;
import pronze.hypixelify.packets.WrapperPlayServerScoreboardObjective;
import pronze.hypixelify.utils.ScoreboardUtil;
import pronze.lib.core.utils.Logger;

import java.util.*;

public class GameScoreboardManagerImpl implements pronze.hypixelify.api.manager.ScoreboardManager {
    private final Game game;
    private final ArenaImpl arena;
    private final Map<UUID, Scoreboard> scoreboardMap = new HashMap<>();
    private final List<String> scoreboard_lines = new ArrayList<>();

    protected BukkitTask updateTask;

    public GameScoreboardManagerImpl(ArenaImpl arena) {
        this.arena = arena;
        game = (Game) Main.getInstance().getGameManager().getGame(arena.getGame().getName()).orElseThrow();

        if (game.countAvailableTeams() >= 5 && Configurator.Scoreboard_Lines.containsKey("5")) {
            scoreboard_lines.addAll(Configurator.Scoreboard_Lines.get("5"));
        } else {
            scoreboard_lines.addAll(Configurator.Scoreboard_Lines.get("default"));
        }
        game.getConnectedPlayers().forEach(this::createBoard);

        new BukkitRunnable() {
            public void run() {
                if (game.getStatus() == GameStatus.RUNNING) {
                    updateCustomObj();
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 5L);
    }

    public void createBoard(Player player) {
        Logger.trace("Creating board for player: {}", player.getName());

        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);

        final var scoreboard = Scoreboard.builder()
                .animate(true)
                .player(player)
                .displayObjective(ScoreboardUtil.GAME_OBJECTIVE_NAME)
                .updateInterval(20L)
                .animationInterval(2L)
                .animatedTitle(SBAHypixelify
                        .getConfigurator()
                        .getStringList("lobby-scoreboard.title"))
                .updateCallback(board -> {
                    board.setLines(process(player, board));
                    return true;
                })
                .build();
        createCustomObjective(scoreboard);
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

    public void createCustomObjective(Scoreboard scoreboard) {
        final var player = scoreboard.getHolder().getPlayer();

        if (!SBAHypixelify.getInstance().getServer().getPluginManager().isPluginEnabled("ProtocolLib") || Main.isLegacy()) {
            return;
        }
        try {
            if (SBAHypixelify.getConfigurator().config.getBoolean("game.tab-health", true)) {
                final var tab_objective = new WrapperPlayServerScoreboardObjective();
                tab_objective.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);
                tab_objective.setName(ScoreboardUtil.TAB_OBJECTIVE_NAME);
                tab_objective.sendPacket(player);

                final var tab_displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
                tab_displayObjective.setPosition(0);
                tab_displayObjective.setScoreName(ScoreboardUtil.TAB_OBJECTIVE_NAME);
                tab_displayObjective.sendPacket(player);
            }

            if (SBAHypixelify.getConfigurator().config.getBoolean("game.tag-health", true)) {
                final var tag_objective = new WrapperPlayServerScoreboardObjective();
                tag_objective.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);
                tag_objective.setName(ScoreboardUtil.TAG_OBJECTIVE_NAME);
                tag_objective.setDisplayName("§c♥");
                tag_objective.sendPacket(player);

                final var tag_displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
                tag_displayObjective.setPosition(2);
                tag_displayObjective.setScoreName(ScoreboardUtil.TAG_OBJECTIVE_NAME);
                tag_displayObjective.sendPacket(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCustomObj() {
        game.getConnectedPlayers().forEach(p -> ScoreboardUtil.updateCustomObjective(p, game));
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
                    if (line.contains("{team_status}")) {
                        String finalLine = line;
                        game.getAvailableTeams().forEach(t -> {
                            String you = "";
                            if (playerTeam != null) {
                                if (playerTeam.getName().equalsIgnoreCase(t.getName())) {
                                    you = SBAHypixelify.getConfigurator()
                                            .getString("scoreboard.you", "§7YOU");
                                }
                            }
                            lines.add(finalLine.replace("{team_status}",
                                    getTeamStatusFormat(t).replace("{you}", you)));
                        });
                        return;
                    }
                    line = line
                            .replace("{team}", teamName)
                            .replace("{beds}", currentBedDestroys)
                            .replace("{dies}", currentDeaths)
                            .replace("{totalkills}", totalKills)
                            .replace("{finalkills}", finalKills)
                            .replace("{kills}", currentKills)
                            .replace("{time}", game.getFormattedTimeLeft())
                            .replace("{formattime}", game.getFormattedTimeLeft())
                            .replace("{game}", game.getName())
                            .replace("{date}", SBAHypixelify.getInstance().getFormattedDate())
                            .replace("{team_bed_status}", teamStatus == null ? "" : teamStatus);

                    if (arena.getGameTask() != null) {
                        line = line.replace("{tier}", arena.getGameTask().getTier()
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
        Optional<RunningTeam> rt = game.getRunningTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(team.getName()))
                .findAny();

        if (rt.isPresent()) {
            return getTeamStatusFormat(rt.get());
        }

        final var destroyed = "{color} {team} §c\u2718 {you}";
        final var formattedTeam = org.screamingsandals.bedwars.game.TeamColor
                .valueOf(team.getColor().name()).chatColor.toString()
                + team.getName().charAt(0);

        return destroyed.replace("{bed_status}", "§c\u2718")
                .replace("{color}", formattedTeam)
                .replace("{team}", ChatColor.WHITE.toString()
                        + team.getName() + ":");
    }
}