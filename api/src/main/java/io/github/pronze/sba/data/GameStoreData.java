package io.github.pronze.sba.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.LocationMapper;

@RequiredArgsConstructor
@Getter
public class GameStoreData {

    public static GameStoreData of(GameStore gameStore) {
        return new GameStoreData(gameStore.getShopFile(), gameStore.getShopCustomName(), LocationMapper.wrapLocation(gameStore.getStoreLocation()));
    }

    private final String shopFile;
    private final String displayName;
    private final LocationHolder location;
}

