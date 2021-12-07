package io.github.pronze.sba.game;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.lib.utils.BasicWrapper;

public class GameWrapperImpl extends BasicWrapper<Game> implements GameWrapper {

    public GameWrapperImpl(@NotNull Game game) {
        super(game);
    }

    @Override
    public void forceStop() {
        stop();
    }

    @Override
    public void stop() {
        wrappedObject.stop();
    }

    @Override
    public org.screamingsandals.bedwars.api.game.@NotNull Game getGame() {
        return wrappedObject;
    }
}
