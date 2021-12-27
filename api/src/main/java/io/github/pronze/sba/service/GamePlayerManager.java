package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GamePlayer;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerWrapper;

import java.util.Optional;

public interface GamePlayerManager {

    Optional<GamePlayer> registerPlayer(@NotNull PlayerWrapper player);

    void unregisterPlayer(@NotNull PlayerWrapper player);
}
