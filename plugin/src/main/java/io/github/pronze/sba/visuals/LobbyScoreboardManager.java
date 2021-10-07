package io.github.pronze.sba.visuals;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.utils.DateUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LobbyScoreboardManager implements Listener {

    public static LobbyScoreboardManager getInstance() {
        return ServiceManager.get(LobbyScoreboardManager.class);
    }

    public static void of(Game game) {
        if (getInstance().sidebarMap.containsKey(game)) {
            throw new UnsupportedOperationException("Game: " + game.getName() + " has already been registered into LobbyScoreboardManager!");
        }

        final var sidebar = Sidebar.of();
        final var animatedTitle = Message.of(LangKeys.ANIMATED_BEDWARS_TITLE).getForAnyone();
        sidebar.title(animatedTitle.get(0));
        // animated title.
        new BukkitRunnable() {
            int anim_title_pos = 0;

            @Override
            public void run() {
                if (game.getStatus() != GameStatus.WAITING) {
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
                if (game.getStatus() != GameStatus.WAITING) {
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

        Message.of(LangKeys.LOBBY_SCOREBOARD_LINES)
                .placeholder("sba_version", SBA.getInstance().getVersion())
                .placeholder("date", DateUtils.getFormattedDate())
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
                    int size = SBAConfig.game_size.getOrDefault(game.getName(), 4);
                    Component mode;
                    switch (size) {
                        case 1:
                            mode = Message.of(LangKeys.LOBBY_SCOREBOARD_SOLO_PREFIX).asComponent();
                            break;
                        case 2:
                            mode = Message.of(LangKeys.LOBBY_SCOREBOARD_DOUBLES_PREFIX).asComponent();
                            break;
                        case 3:
                            mode = Message.of(LangKeys.LOBBY_SCOREBOARD_TRIPLES_PREFIX).asComponent();
                            break;
                        case 4:
                            mode = Message.of(LangKeys.LOBBY_SCOREBOARD_SQUADS_PREFIX).asComponent();
                            break;
                        default:
                            mode = Component.text(size + "v" + size + "v" + size + "v" + size);
                    }
                    return mode;
                }).getForAnyone()
                .forEach(sidebar::bottomLine);

        game.getConnectedPlayers()
                .stream()
                .map(PlayerMapper::wrapPlayer)
                .forEach(sidebar::addViewer);
        sidebar.show();

        getInstance().sidebarMap.put(game, Sidebar.of());
    }

    private final Map<Game, Sidebar> sidebarMap = new HashMap<>();

    @OnPostEnable
    public void onPostEnable() {
        if (!SBAConfig.getInstance().node("lobby-scoreboard", "enabled").getBoolean(true)) {
            return;
        }
        SBA.getInstance().registerListener(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(BedwarsPlayerJoinedEvent event) {
        final var player = event.getPlayer();
        if (event.getGame().getStatus() == GameStatus.WAITING) {
            addViewer(player);
        }
    }

    private void addViewer(Player player) {
        final var playerGame = Main.getInstance().getGameOfPlayer(player);
        if (playerGame == null) {
            return;
        }

        final var wrapper = PlayerMapper.wrapPlayer(player);
        for (var entry : sidebarMap.entrySet()) {
            final var game = entry.getKey();
            if (game != playerGame) {
                continue;
            }

            final var sidebar = entry.getValue();
            if (!sidebar.getViewers().contains(wrapper)) {
                sidebar.addViewer(wrapper);
            }
        }
    }

    private void remove(Player player) {
        final var wrapper = PlayerMapper.wrapPlayer(player);
        sidebarMap.values()
                .stream()
                .filter(sidebar -> sidebar.getViewers().contains(wrapper))
                .forEach(sidebar -> sidebar.removeViewer(wrapper));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(BedwarsPlayerLeaveEvent e) {
        remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    @OnPreDisable
    public void destroy() {
        sidebarMap.values()
                .forEach(Sidebar::destroy);
        sidebarMap.clear();
    }
}
