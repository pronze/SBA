package io.github.pronze.sba.game;
import io.github.pronze.sba.mock.MockArenaManager;
import io.github.pronze.sba.mock.MockGame;
import io.github.pronze.sba.utils.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArenaManagerTest {
    private final MockArenaManager manager;
    private final MockGame game;

    public ArenaManagerTest() {
        manager = new MockArenaManager();
        game = new MockGame("mockGame");
    }

    @BeforeAll
    static void enableMockMode() {
        Logger.mockMode();
    }

    @BeforeEach
    public void registerArena() {
        manager.createArena(game);
        assertTrue(manager.getRegisteredArenas()
                .stream()
                .anyMatch(arena -> arena.getGame().getName().equalsIgnoreCase(game.getName())));
    }

    @Test
    public void removeArena() {
        manager.removeArena(game);
        assertFalse(manager.getRegisteredArenas()
                .stream()
                .anyMatch(arena -> arena.getGame().getName().equalsIgnoreCase(game.getName())));
    }
}
