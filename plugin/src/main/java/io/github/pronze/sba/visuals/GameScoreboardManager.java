package io.github.pronze.sba.visuals;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.tasks.GeneratorTask;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.utils.DateUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GameScoreboardManager {
    private final static Map<Arena, Sidebar> sidebarMap = new HashMap<>();

    public static GameScoreboardManager getInstance() {
        return ServiceManager.get(GameScoreboardManager.class);
    }

    public static void of(Arena arena) {
        final var game = (Game) arena.getGame();
        if (sidebarMap.containsKey(arena)) {
            throw new UnsupportedOperationException("Game: " + game.getName() + " has already been registered into GameScoreboardManager!");
        }

        final var sidebar = Sidebar.of();
        final var animatedTitle = Message.of(LangKeys.ANIMATED_BEDWARS_TITLE).getForAnyone();

        sidebar.title(animatedTitle.get(0));
        // animated title.
        new BukkitRunnable() {
            int anim_title_pos = 0;

            @Override
            public void run() {
                if (game.getStatus() != GameStatus.RUNNING) {
                    this.cancel();
                    return;
                }
                anim_title_pos += 1;
                if (anim_title_pos >= animatedTitle.size()) {
                    anim_title_pos = 0;
                }
                sidebar.title(animatedTitle.get(anim_title_pos));
            }
        }.runTaskTimerAsynchronously(SBA.getPluginInstance(), 0L, 2L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getStatus() != GameStatus.RUNNING) {
                    this.cancel();
                    return;
                }

                game.getRunningTeams().forEach(team -> {
                    if (sidebar.getTeam(team.getName()).isEmpty()) {
                        sidebar.team(team.getName())
                                .friendlyFire(false)
                                .color(NamedTextColor.NAMES.value(TeamColor.fromApiColor(team.getColor()).chatColor.name().toLowerCase()));
                    }

                    var sidebarTeam = sidebar.getTeam(team.getName()).orElseThrow();

                    List.copyOf(sidebarTeam.players())
                            .forEach(teamPlayer -> {
                                if (team.getConnectedPlayers().stream().noneMatch(bedWarsPlayer -> bedWarsPlayer.equals(teamPlayer.as(Player.class)))) {
                                    sidebarTeam.removePlayer(teamPlayer);
                                }
                            });

                    team.getConnectedPlayers()
                            .stream()
                            .map(PlayerMapper::wrapPlayer)
                            .filter(teamPlayer -> !sidebarTeam.players().contains(teamPlayer))
                            .forEach(sidebarTeam::player);
                });
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 20L);


        Message scoreboardLines;
        if (game.countAvailableTeams() > 5) {
            scoreboardLines = Message.of(LangKeys.SCOREBOARD_LINES_5);
        } else {
            scoreboardLines = Message.of(LangKeys.SCOREBOARD_LINES_DEFAULT);
        }

        final var generatorTask = arena.getTask(GeneratorTask.class).orElseThrow();
        scoreboardLines
                .placeholder("beds", wrapper -> {
                    final var data = arena.getPlayerData(wrapper.as(PlayerWrapper.class).getUuid())
                            .orElseThrow();
                    return AdventureHelper.toComponent(String.valueOf(data.getBedDestroys()));
                })
                .placeholder("dies", wrapper -> {
                    final var data = arena.getPlayerData(wrapper.as(PlayerWrapper.class).getUuid())
                            .orElseThrow();
                    return AdventureHelper.toComponent(String.valueOf(data.getFinalKills()));
                })
                .placeholder("finalkills", wrapper -> {
                    final var data = arena.getPlayerData(wrapper.as(PlayerWrapper.class).getUuid())
                            .orElseThrow();
                    return AdventureHelper.toComponent(String.valueOf(data.getFinalKills()));
                })
                .placeholder("kills", wrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(wrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getKills()));
                })
                .placeholder("time", () -> AdventureHelper.toComponent(game.getFormattedTimeLeft()))
                .placeholder("game", game.getName())
                .placeholder("date", DateUtils.getFormattedDate())
                .placeholder("team_bed_status", wrapper -> {
                    final var player = wrapper.as(Player.class);
                    final var playerTeam = game.getTeamOfPlayer(player);
                    final var teamStatus = playerTeam != null ? getTeamBedStatus(playerTeam) : "";
                    return AdventureHelper.toComponent(teamStatus);
                })
                .placeholder("tier", generatorTask.getNextTierName().replace("-", " ") + " in §a" + generatorTask.getTimeLeftForNextEvent())
                .placeholder("sba_version", SBA.getInstance().getVersion())
                .placeholder("team_status", wrapper -> {
                    final var player = wrapper.as(Player.class);
                    final var playerTeam = game.getTeamOfPlayer(player);

                    var componentAtomicReference = new AtomicReference<Component>(Component.empty());

                    game.getAvailableTeams().forEach(t -> {
                        String you = "";
                        if (playerTeam != null) {
                            if (playerTeam.getName().equalsIgnoreCase(t.getName())) {
                                you = AdventureHelper.toLegacy(Message.of(LangKeys.SCOREBOARD_YOU_MESSAGE).asComponent());
                            }
                        }
                        componentAtomicReference.set(componentAtomicReference.get().append(AdventureHelper.toComponent(getTeamStatusFormat(game, t).replace("%you%", you))));
                    });
                    return componentAtomicReference.get();
                });

        sidebar.bottomLine(scoreboardLines);
    }

    private static String getTeamBedStatus(RunningTeam team) {
        return team.isDead() ?
                SBAConfig.getInstance().node("team-status", "target-destroyed").getString("§c\u2717") :
                SBAConfig.getInstance().node("team-status", "target-exists").getString("§a\u2713");
    }

    private static String getTeamStatusFormat(Game game, RunningTeam team) {
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

    private static String getTeamStatusFormat(Game game, Team team) {
        return game
                .getRunningTeams()
                .stream()
                .filter(t -> t.getName().equalsIgnoreCase(team.getName()))
                .map(runningTeam -> getTeamStatusFormat(game, runningTeam))
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

    @OnPreDisable
    public void destroy() {
        sidebarMap.values()
                .forEach(Sidebar::destroy);
        sidebarMap.clear();
    }

    public Optional<Sidebar> getSidebar(Arena query) {
        return Optional.ofNullable(sidebarMap.get(query));
    }

    public void destroy(Arena query) {
        var iterator = sidebarMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final var entry = iterator.next();
            final var arena = entry.getKey();
            if (arena == query) {
                final var sidebar = entry.getValue();
                sidebar.destroy();
                iterator.remove();
            }
        }
    }

    public void addViewer(Player player) {
        final var playerGame = Main.getInstance().getGameOfPlayer(player);
        if (playerGame == null) {
            return;
        }

        final var wrapper = PlayerMapper.wrapPlayer(player);
        for (var entry : sidebarMap.entrySet()) {
            final var arena = entry.getKey();
            if (arena.getGame() != playerGame) {
                continue;
            }

            final var sidebar = entry.getValue();
            if (!sidebar.getViewers().contains(wrapper)) {
                sidebar.addViewer(wrapper);
            }
        }
    }

    public void removeViewer(Player player) {
        final var wrapper = PlayerMapper.wrapPlayer(player);
        sidebarMap.values()
                .stream()
                .filter(sidebar -> sidebar.getViewers().contains(wrapper))
                .forEach(sidebar -> sidebar.removeViewer(wrapper));
    }
}