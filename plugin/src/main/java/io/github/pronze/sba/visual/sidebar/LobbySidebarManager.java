package io.github.pronze.sba.visual.sidebar;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.util.LocationUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.lib.Server;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerJoinEvent;
import org.screamingsandals.lib.event.player.SPlayerLeaveEvent;
import org.screamingsandals.lib.event.player.SPlayerWorldChangeEvent;
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
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.WorldHolder;

@RequiredArgsConstructor
@Service
public final class LobbySidebarManager implements Listener {
    private final SBA plugin;
    private final SBAConfig config;
    private final LoggerWrapper logger;
    private final Sidebar sidebar = Sidebar.of();

    @Getter
    @Setter
    private LocationHolder location = null;

    @OnPostEnable
    public void onPostEnable() {
        if (!config.node("main-lobby", "scoreboard", "enabled")
                .getBoolean(false)) {
            return;
        }

        final var title = Message.of(LangKeys.MAIN_LOBBY_SCOREBOARD_TITLE)
                .asComponent();
        sidebar.title(title);

        final var lines = Message.of(LangKeys.MAIN_LOBBY_SCOREBOARD_LINES)
                .placeholder("sba_version", plugin.getPluginDescription().getVersion())
                .placeholder("kills", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getKills()));
                })
                .placeholder("beddestroys", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getDestroyedBeds()));
                })
                .placeholder("deaths", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getDeaths()));
                })
                .placeholder("level", playerWrapper -> {
                    final var sbaWrapper = playerWrapper.as(GamePlayer.class);
                    return AdventureHelper.toComponent("§7" + sbaWrapper.getLevel() + "✫");
                })
                .placeholder("progress", playerWrapper -> {
                    final var sbaWrapper = playerWrapper.as(GamePlayer.class);
                    return AdventureHelper.toComponent(sbaWrapper.getFormattedProgress());
                })
                .placeholder("bar", playerWrapper -> {
                    final var sbaWrapper = playerWrapper.as(GamePlayer.class);
                    return AdventureHelper.toComponent(sbaWrapper.getCompletedBoxes());
                })
                .placeholder("wins", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getWins()));
                })
                .placeholder("kdr", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getKD()));
                });

        sidebar.bottomLine(lines);
        sidebar.show();

        LocationUtils.locationFromNode(config.node("main-lobby")).ifPresentOrElse(location -> {
            plugin.registerListener(this);
            setLocation(location);
            Tasker.build(() ->
                            Server
                            .getConnectedPlayers()
                            .forEach(this::addViewer))
                    .delay(3L, TaskerTime.TICKS)
                    .start();
        }, () -> {
            Bukkit.getServer().getLogger().warning("Could not find lobby world!");
        });
    }

    @OnPreDisable
    public void onPreDisable() {
        logger.trace("Destroying Lobby sidebar for viewers: {}", sidebar.getViewers().toString());
        sidebar.destroy();
    }

    private void addViewer(@NotNull PlayerWrapper playerWrapper) {
        logger.trace("Adding viewer: {} to lobby scoreboard!", playerWrapper.getName());
        sidebar.addViewer(playerWrapper);
    }

    private void removeViewer(@NotNull PlayerWrapper playerWrapper) {
        logger.trace("Removing viewer: {} from lobby scoreboard!", playerWrapper.getName());
        sidebar.removeViewer(playerWrapper);
    }

    @OnEvent
    public void onPlayerJoin(SPlayerJoinEvent event) {
        final var gamePlayer = event.getPlayer().as(GamePlayer.class);
        Tasker.build(() -> {
             if (isInWorld(gamePlayer.getLocation())
                    && !Main.isPlayerInGame(gamePlayer.as(Player.class))
                    && gamePlayer.isOnline()) {
                addViewer(gamePlayer);
            }
        }).delay(1L, TaskerTime.SECONDS).start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(SPlayerWorldChangeEvent event) {
        final var playerWrapper = event.getPlayer();
        if (playerWrapper.isOnline() && isInWorld(playerWrapper.getLocation())) {
            addViewer(playerWrapper);
        } else {
            removeViewer(playerWrapper);
        }
    }

    @OnEvent
    public void onPlayerLeave(SPlayerLeaveEvent event) {
        removeViewer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerJoin(BedwarsPlayerJoinedEvent event) {
        final var player = event.getPlayer();
        final var wrappedPlayer = PlayerMapper
                .wrapPlayer(player)
                .as(GamePlayer.class);

        removeViewer(wrappedPlayer);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedWarsPlayerLeaveEvent(BedwarsPlayerLeaveEvent event) {
        final var player = event.getPlayer();
        final var wrappedPlayer = PlayerMapper
                .wrapPlayer(player)
                .as(GamePlayer.class);

        Tasker.build(() -> {
            if (isInWorld(wrappedPlayer.getLocation())
                    && wrappedPlayer.isOnline()) {
                logger.trace("Player: {} is in lobby world, displaying sidebar!", player.getName());
                addViewer(wrappedPlayer);
            }
        }).afterOneTick().start();
    }

    private boolean isInWorld(@NotNull WorldHolder worldHolder) {
        return worldHolder.equals(location.getWorld());
    }

    private boolean isInWorld(@NotNull LocationHolder locationHolder) {
        return isInWorld(locationHolder.getWorld());
    }
}

