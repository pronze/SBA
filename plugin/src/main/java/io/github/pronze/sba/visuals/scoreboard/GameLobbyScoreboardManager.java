package io.github.pronze.sba.visuals.scoreboard;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.event.GameWrapperRegistrationEvent;
import io.github.pronze.sba.game.GameManagerImpl;
import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.service.DateProviderService;
import io.github.pronze.sba.visuals.scoreboard.task.SidebarAnimatedTitleTask;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerLeaveEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public final class GameLobbyScoreboardManager implements Listener {
    private final DateProviderService service;
    private final SBA plugin;
    private final LoggerWrapper logger;
    private final GameManagerImpl gameManager;
    private final SBAConfig config;
    private final Map<GameWrapper, Sidebar> sidebarMap = new HashMap<>();

    @OnPostEnable
    public void onPostEnable() {
        if (!config.node("lobby-scoreboard", "enabled").getBoolean(true)) {
            return;
        }
        plugin.registerListener(this);
    }

    private void addViewer(@NotNull PlayerWrapper playerWrapper, @NotNull Game game) {
        logger.trace("Adding viewer: {} to game lobby scoreboard!", playerWrapper.getName());
        final var maybeWrappedGame = gameManager.getWrappedGame(game);
        if (maybeWrappedGame.isEmpty()) {
            // probably blacklisted.
            return;
        }

        final var sidebar = sidebarMap.get(maybeWrappedGame.get());
        sidebar.addViewer(playerWrapper);
    }

    private void removeViewer(@NotNull PlayerWrapper playerWrapper) {
        logger.trace("Removing viewer: {} from game lobby scoreboard!", playerWrapper.getName());
        sidebarMap.values()
                .stream()
                .filter(sidebar -> sidebar.getViewers().contains(playerWrapper))
                .forEach(sidebar -> sidebar.removeViewer(playerWrapper));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsGameStartEvent(BedwarsGameStartEvent event) {
        event.getGame()
                .getConnectedPlayers()
                .stream()
                .map(PlayerMapper::wrapPlayer)
                .forEach(this::removeViewer);
    }

    @EventHandler
    public void onBedWarsPlayerJoin(BedwarsPlayerJoinEvent event) {
        final var game = event.getGame();
        if (game.getStatus() != GameStatus.WAITING) {
            // probably joined as spectator, let's ignore.
            return;
        }

        final var player = event.getPlayer();
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player);
        addViewer(wrappedPlayer, game);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent event) {
        final var player = event.getPlayer();
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player);
        removeViewer(wrappedPlayer);
    }

    @OnEvent
    public void onPlayerLeave(SPlayerLeaveEvent event) {
        removeViewer(event.getPlayer());
    }

    @OnEvent(priority = org.screamingsandals.lib.event.EventPriority.HIGHEST)
    public void onGameRegistered(GameWrapperRegistrationEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final var gameWrapper = event.getGame();
        final var game = gameWrapper.getGame();

        final var sidebar = Sidebar.of();
        final var animatedTitle = Message.of(LangKeys.ANIMATED_BEDWARS_TITLE).getForAnyone();

        Tasker.build(taskBase -> new SidebarAnimatedTitleTask(sidebar, animatedTitle, () -> true, taskBase))
                .async()
                .repeat(2L, TaskerTime.TICKS)
                .start();

        Tasker.build(() -> {
            if (game.getStatus() != GameStatus.WAITING) {
                // we could instead listen to BedWarsGameStatusChangedEvent
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
        }).repeat(1L, TaskerTime.SECONDS).start();

        final var lines = Message.of(LangKeys.LOBBY_SCOREBOARD_LINES)
                .placeholder("sba_version", plugin.getPluginDescription().getVersion())
                .placeholder("date", service.getFormattedDate())
                .placeholder("state", () -> {
                    var state = Message.of(LangKeys.LOBBY_SCOREBOARD_STATE_WAITING).asComponent();
                    if (game.countConnectedPlayers() >= game.getMinPlayers()
                            && game.getStatus() == GameStatus.WAITING) {
                        final var time = ((org.screamingsandals.bedwars.game.Game) Main.getInstance().getGameByName(game.getName())).getFormattedTimeLeft();
                        if (!time.contains("0-1")) {
                            final var units = time.split(":");
                            var seconds = Integer.parseInt(units[1]) + 1;
                            state = Message.of(LangKeys.LOBBY_SCOREBOARD_STATE)
                                    .placeholder("countdown", String.valueOf(seconds))
                                    .asComponent();
                        }
                    }
                    return state;
                })
                .placeholder("game", () -> AdventureHelper.toComponent(game.getName()))
                .placeholder("players", () -> AdventureHelper.toComponent(String.valueOf(game.getConnectedPlayers().size())))
                .placeholder("maxplayers", () -> AdventureHelper.toComponent(String.valueOf(game.getMaxPlayers())))
                .placeholder("minplayers", () -> AdventureHelper.toComponent(String.valueOf(game.getMinPlayers())))
                .placeholder("needplayers", () -> {
                    int needplayers = game.getMinPlayers() - game.getConnectedPlayers().size();
                    needplayers = Math.max(needplayers, 0);
                    return AdventureHelper.toComponent(String.valueOf(needplayers));
                })
                .placeholder("mode", () -> {
                    // TODO: Configurable modes.
                    // int size = SBAConfig.game_size.getOrDefault(game.getName(), 4);
                    // Component mode;
                    // switch (size) {
                    //     case 1:
                    //         mode = Message.of(LangKeys.LOBBY_SCOREBOARD_SOLO_PREFIX).asComponent();
                    //         break;
                    //     case 2:
                    //         mode = Message.of(LangKeys.LOBBY_SCOREBOARD_DOUBLES_PREFIX).asComponent();
                    //         break;
                    //     case 3:
                    //         mode = Message.of(LangKeys.LOBBY_SCOREBOARD_TRIPLES_PREFIX).asComponent();
                    //         break;
                    //     case 4:
                    //         mode = Message.of(LangKeys.LOBBY_SCOREBOARD_SQUADS_PREFIX).asComponent();
                    //         break;
                    //     default:
                    //         mode = Component.text(size + "v" + size + "v" + size + "v" + size);
                    // }
                    // return mode;
                    return null;
                });

        sidebar.bottomLine(lines);
        sidebar.show();
        sidebarMap.put(gameWrapper, sidebar);
    }

    @OnPreDisable
    public void onPreDisable() {
        sidebarMap.values()
                .forEach(Sidebar::destroy);
        sidebarMap.clear();
    }
}
