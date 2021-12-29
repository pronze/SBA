package io.github.pronze.sba.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.service.GameManagerImpl;
import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.lang.LangKeys;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.skin.NPCSkin;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskBase;
import org.screamingsandals.lib.tasker.task.TaskState;
import org.screamingsandals.lib.tasker.task.TaskerTask;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BedWarsListener implements Listener {
    private final Map<UUID, TaskerTask> taskCache = new HashMap<>();
    private final SBA plugin;
    private final GameManagerImpl gameManager;
    private final SBAConfig config;
    private final LoggerWrapper logger;

    @OnPostEnable
    public void onPostEnable() {
        plugin.registerListener(this);
    }

    @EventHandler
    public void onBedWarsGameStartedEvent(BedwarsGameStartedEvent event) {
        final var game = event.getGame();
        final var maybeWrapper = gameManager.getWrappedGame(game);
        if (maybeWrapper.isEmpty()) {
            return;
        }

        final var gameWrapper = maybeWrapper.get();
        gameWrapper.start();

        Message.of(LangKeys.GAME_START_MESSAGE)
                .send(game
                        .getConnectedPlayers()
                        .stream()
                        .map(PlayerMapper::wrapPlayer)
                        .collect(Collectors.toList()));
    }

    @EventHandler
    public void onBWPlayerJoinedEvent(BedwarsPlayerJoinedEvent event) {
        final var gamePlayer = PlayerMapper
                .wrapPlayer(event.getPlayer())
                .as(GamePlayer.class);

        var task = taskCache.get(gamePlayer.getUniqueId());
        if (task != null
                && task.getState() != TaskState.CANCELLED) {
            task.cancel();
        }

        final var game = event.getGame();
        if (config.isGameBlacklisted(game)) {
            return;
        }

        switch (game.getStatus()) {
            case WAITING:
                task = Tasker.build(taskBase -> new CountdownUpdaterTask(gamePlayer, taskBase, game, taskCache))
                        .delay(3L, TaskerTime.TICKS)
                        .repeat(1L, TaskerTime.SECONDS)
                        .start();
                taskCache.put(gamePlayer.getUniqueId(), task);
                break;
            case RUNNING:
                final var gameWrapper = gameManager
                        .getWrappedGame(game)
                        .orElseThrow();
                gameWrapper.spectatorJoin(gamePlayer);
        }
    }

    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent event) {
        final var gamePlayer = PlayerMapper
                .wrapPlayer(event.getPlayer())
                .as(GamePlayer.class);

        taskCache.computeIfPresent(gamePlayer.getUniqueId(), ((uuid, taskerTask) -> {
            if (taskerTask.getState() != TaskState.CANCELLED) {
                taskerTask.cancel();
            }
            return null;
        }));

        final var maybeGame = gamePlayer.getGame();
        if (maybeGame.isEmpty()) {
            return;
        }

        final var game = maybeGame.get();
        game.leaveFromGame(gamePlayer);
    }

    // fix for listener handles during BedWars reload.
    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final var pluginName = event.getPlugin().getName();

        if (pluginName.equalsIgnoreCase(Main.getInstance().getName())
                && plugin.as(JavaPlugin.class).isEnabled()) {
            logger.trace("Re-registering listeners!");
            final var listeners = plugin.getRegisteredListeners();
            listeners.forEach(plugin::unregisterListener);
            listeners.forEach(plugin::registerListener);
            logger.trace("Registration complete!");
        }
    }

    @RequiredArgsConstructor
    private static class CountdownUpdaterTask implements Runnable {
        private final GamePlayer gamePlayer;
        private final TaskBase taskBase;
        private final Game game;
        private final Map<UUID, TaskerTask> taskCache;
        private int buffer;

        @Override
        public void run() {
            if (!gamePlayer.isOnline()
                    || game.getStatus() != GameStatus.WAITING) {
                taskBase.cancel();
                taskCache.remove(gamePlayer.getUniqueId());
                return;
            }

            if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                final var time = ((org.screamingsandals.bedwars.game.Game) game).getFormattedTimeLeft();
                if (time.contains("0-1")) {
                    // bug wars problem
                    return;
                }

                String[] units = time.split(":");
                int seconds = (Integer.parseInt(units[0]) * 60) + Integer.parseInt(units[1]) + 1;
                if (buffer == seconds) {
                    // make sure it does not repeatedly glitch on non-countdown moments.
                    return;
                }

                // update buffer
                buffer = seconds;

                if (seconds <= 10) {
                    final var message = Message.of(LangKeys.GAME_STARTS_IN_MESSAGE)
                            .placeholder("seconds", String.valueOf(seconds))
                            .asComponent();

                    gamePlayer.sendMessage(message);

                    final var title = Title.title(
                            Component.text("§c" + seconds),
                            Component.empty(),
                            Title.Times.of(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(0)
                            ));
                    gamePlayer.showTitle(title);
                }
            }
        }
    }
}

