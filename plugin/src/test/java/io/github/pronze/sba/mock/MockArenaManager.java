package io.github.pronze.sba.mock;

import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

public class MockArenaManager extends ArenaManager {

    @Override
    public void createArena(@NotNull Game game) {
        final var gameName = game.getName();
        if (getArenaMap().containsKey(gameName)) {
            throw new UnsupportedOperationException("Arena: " + gameName + " already exists!");
        }
        Logger.trace("Creating arena for game: {}", gameName);
        getArenaMap().put(gameName, new MockArena(game));
    }

}
