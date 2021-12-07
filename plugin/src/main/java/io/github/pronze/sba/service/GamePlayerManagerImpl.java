package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GamePlayerImpl;
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
public final class GamePlayerManagerImpl {
    private final LoggerWrapper logger;
    private final Map<UUID, GamePlayerImpl> wrappedPlayers = new HashMap<>();

    @OnEnable
    public void onEnable() {
        registerMappings();
        Server.getConnectedPlayers().forEach(this::registerPlayer);
    }

    private void registerMappings() {
        PlayerMapper.UNSAFE_getPlayerConverter()
                .registerW2P(GamePlayerImpl.class, playerWrapper -> {
                   if (playerWrapper instanceof GamePlayerImpl) {
                       // prevent infinite wrapping recursion
                       return (GamePlayerImpl) playerWrapper;
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
    }

    @NotNull
    public Optional<GamePlayerImpl> registerPlayer(@NotNull PlayerWrapper player) {
        final var uuid = player.getUuid();
        if (wrappedPlayers.containsKey(uuid)) {
            return Optional.empty();
        }

        final var wrappedPlayer = new GamePlayerImpl(player);
        wrappedPlayers.put(uuid, wrappedPlayer);
        logger.trace("Registered player wrapper: [name: {}, uuid: {}]", player.getName(), player.getUniqueId());
        return Optional.of(wrappedPlayer);
    }

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
