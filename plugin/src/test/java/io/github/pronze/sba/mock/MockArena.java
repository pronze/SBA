package io.github.pronze.sba.mock;

import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.game.GameStorage;
import io.github.pronze.sba.game.IArena;
import io.github.pronze.sba.manager.ScoreboardManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.ItemSpawner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class MockArena implements IArena {
    private final Game game;

    @Override
    public GameStorage getStorage() {
        return null;
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        return null;
    }

    @Override
    public Optional<GamePlayerData> getPlayerData(UUID playerUUID) {
        return Optional.empty();
    }

    @Override
    public void putPlayerData(UUID uuid, GamePlayerData data) {

    }

    @Override
    public boolean isPlayerHidden(Player player) {
        return false;
    }

    @Override
    public void removeHiddenPlayer(Player player) {

    }

    @Override
    public void addHiddenPlayer(Player player) {

    }

    @Override
    public List<Player> getInvisiblePlayers() {
        return null;
    }

    @Override
    public void createRotatingGenerator(ItemSpawner itemSpawner) {

    }
}
