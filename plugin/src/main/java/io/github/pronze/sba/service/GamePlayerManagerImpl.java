package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.game.InvisiblePlayer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.Server;
import org.screamingsandals.lib.event.EventPriority;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerJoinEvent;
import org.screamingsandals.lib.event.player.SPlayerLeaveEvent;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public final class GamePlayerManagerImpl implements GamePlayerManager {
    private final LoggerWrapper logger;
    private final Map<UUID, GamePlayer> wrappedPlayers = new HashMap<>();

    @OnEnable
    public void onEnable() {
        registerMappings();
        Server.getConnectedPlayers().forEach(this::registerPlayer);
    }

    private void registerMappings() {
        PlayerMapper.UNSAFE_getPlayerConverter()
                .registerW2P(GamePlayer.class, playerWrapper -> {
                   if (playerWrapper instanceof GamePlayer) {
                       // prevent infinite wrapping recursion
                       return (GamePlayer) playerWrapper;
                   }

                   if (wrappedPlayers.containsKey(playerWrapper.getUuid())) {
                       return wrappedPlayers.get(playerWrapper.getUuid());
                   }

                   final var maybeWrapper = registerPlayer(playerWrapper);
                   if (maybeWrapper.isEmpty()) {
                       logger.trace("Failed to register player: [name: {}, uuid: {}]", playerWrapper.getName(), playerWrapper.getUniqueId());
                       return null;
                   }

                   return maybeWrapper.get();
                });

        // mappings for invisible players.
        PlayerMapper.UNSAFE_getPlayerConverter()
                .registerW2P(InvisiblePlayer.class, playerWrapper -> {
                   if (playerWrapper instanceof InvisiblePlayer) {
                       return (InvisiblePlayer) playerWrapper;
                   }

                   // TODO: query arenas.

                    return null;
                });
    }

    @NotNull
    public Optional<GamePlayer> registerPlayer(@NotNull PlayerWrapper player) {
        final var uuid = player.getUuid();
        if (wrappedPlayers.containsKey(uuid)) {
            return Optional.empty();
        }

        final var wrappedPlayer = new GamePlayer(player);
        wrappedPlayers.put(uuid, wrappedPlayer);
        logger.trace("Registered player wrapper: [name: {}, uuid: {}]", player.getName(), player.getUniqueId());
        return Optional.of(wrappedPlayer);
    }

    @Override
    public void unregisterPlayer(@NotNull PlayerWrapper player) {
        final var uuid = player.getUuid();
        if (!wrappedPlayers.containsKey(uuid)) {
            return;
        }

        final var wrappedPlayer = wrappedPlayers.get(player.getUuid());
        wrappedPlayer.destroy();
        wrappedPlayers.remove(uuid);
        logger.trace("Removed player wrapper: [name: {}, uuid: {}]", wrappedPlayer.getName(), wrappedPlayer.getUniqueId());
    }

    @OnEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(SPlayerLeaveEvent event) {
        final var player = event.getPlayer();
        unregisterPlayer(player);
    }

    @OnEvent(priority = EventPriority.LOWEST)
    public void onPlayerJoin(SPlayerJoinEvent event) {
        final var player = event.getPlayer();
        registerPlayer(player);
    }
}
