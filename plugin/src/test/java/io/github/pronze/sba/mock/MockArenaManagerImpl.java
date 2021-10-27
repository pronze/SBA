package io.github.pronze.sba.mock;

import io.github.pronze.sba.game.GameWrapperManagerImpl;
import io.github.pronze.sba.game.GameStorageImpl;
import io.github.pronze.sba.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

public class MockArenaManagerImpl extends GameWrapperManagerImpl {

    @Override
    public MockArena createArena(@NotNull Game game) {
        final var gameName = game.getName();
        if (getArenaMap().containsKey(gameName)) {
            throw new UnsupportedOperationException("Arena: " + gameName + " already exists!");
        }
        final var arena = new MockArena(game, new GameStorageImpl(game));
        Logger.trace("Creating arena for game: {}", gameName);
        getArenaMap().put(gameName, arena);
        return arena;
    }

}
