package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.mock.MockArena;
import io.github.pronze.sba.mock.MockArenaManagerImpl;
import io.github.pronze.sba.mock.MockGame;
import io.github.pronze.sba.mock.MockPlayer;
import io.github.pronze.sba.utils.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArenaTest {
    private final MockArenaManagerImpl manager;
    private final MockGame game;
    private final MockPlayer mockPlayer;
    private final MockArena arena;

    public ArenaTest() {
        mockPlayer = new MockPlayer();
        manager = new MockArenaManagerImpl();
        game = new MockGame("mockGame");
        game.joinToGame(mockPlayer);
        arena = manager.createArena(game);
    }

    @BeforeAll
    static void enableMockMode() {
        Logger.mockMode();
    }

    @BeforeEach
    public void addHiddenPlayer() {
        arena.addHiddenPlayer(mockPlayer);
        assertTrue(arena.isPlayerHidden(mockPlayer));
    }

    @Test
    public void removeHiddenPlayer() {
        arena.removeHiddenPlayer(mockPlayer);
        assertFalse(arena.isPlayerHidden(mockPlayer));
    }

    @BeforeEach
    public void registerPlayerData() {
        arena.registerPlayerData(mockPlayer.getUniqueId(), GamePlayerData.of(mockPlayer));
        assertTrue(arena.getPlayerData(mockPlayer.getUniqueId()).isPresent());
    }

    @Test
    public void unregisterPlayerData() {
        arena.unregisterPlayerData(mockPlayer.getUniqueId());
        assertTrue(arena.getPlayerData(mockPlayer.getUniqueId()).isEmpty());
    }


}
