package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GamePlayerData;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Optional;
import java.util.UUID;

public interface GameWrapper {

    void forceStop();

    void stop();

    @NotNull
    Game getGame();

    @NotNull
    Optional<GamePlayerData> getPlayerData(@NotNull UUID uuid);
}
