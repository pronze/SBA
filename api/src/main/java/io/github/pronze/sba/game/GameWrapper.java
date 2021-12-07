package io.github.pronze.sba.game;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

public interface GameWrapper {

    void forceStop();

    void stop();

    @NotNull
    Game getGame();
}
