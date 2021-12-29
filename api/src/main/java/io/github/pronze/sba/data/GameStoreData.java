package io.github.pronze.sba.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.LocationMapper;
import org.spongepowered.configurate.ConfigurationNode;

@Data
public class GameStoreData {

    public static GameStoreData of(@NotNull ConfigurationNode node, @NotNull GameStore gameStore) {
        return new GameStoreData(node, LocationMapper.wrapLocation(gameStore.getStoreLocation()));
    }

    private final ConfigurationNode node;
    private final LocationHolder location;
}