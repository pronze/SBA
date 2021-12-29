package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GamePlayer;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;

import java.util.Optional;

public interface GamePlayerManager {

    static GamePlayerManager getInstance() {
        return ServiceManager.get(GamePlayerManager.class);
    }

    Optional<GamePlayer> registerPlayer(@NotNull PlayerWrapper player);

    void unregisterPlayer(@NotNull PlayerWrapper player);
}
