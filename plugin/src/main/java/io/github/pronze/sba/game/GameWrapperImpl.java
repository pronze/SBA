package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GamePlayerData;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.lib.utils.BasicWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GameWrapperImpl extends BasicWrapper<Game> implements GameWrapper {
    private final Map<UUID, GamePlayerData> playerData = new HashMap<>();

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
        playerData.clear();
    }

    @Override
    public org.screamingsandals.bedwars.api.game.@NotNull Game getGame() {
        return wrappedObject;
    }

    @Override
    public @NotNull Optional<GamePlayerData> getPlayerData(@NotNull UUID uuid) {
        return Optional.ofNullable(playerData.get(uuid));
    }
}
