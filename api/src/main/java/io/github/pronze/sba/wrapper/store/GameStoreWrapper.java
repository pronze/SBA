package io.github.pronze.sba.wrapper.store;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.entity.EntityBasic;
import org.screamingsandals.lib.entity.EntityMapper;
import org.screamingsandals.lib.utils.BasicWrapper;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.LocationMapper;

import java.util.Optional;

public class GameStoreWrapper extends BasicWrapper<GameStore> {

    public static GameStoreWrapper of(GameStore gameStore) {
        return new GameStoreWrapper(gameStore);
    }

    protected GameStoreWrapper(GameStore wrappedObject) {
        super(wrappedObject);
    }

    public Optional<EntityBasic> kill() {
        final var entity = wrappedObject.kill();
        if (entity == null) {
            return Optional.empty();
        }
        return EntityMapper.wrapEntity(entity);
    }

    public String getShopFile() {
        return wrappedObject.getShopFile();
    }

    public void setEntity(EntityBasic entity) {
        Reflect.setField(wrappedObject, "entity", entity.as(Entity.class));
    }

    @NotNull
    public LocationHolder getStoreLocation() {
        return LocationMapper.wrapLocation(wrappedObject.getStoreLocation());
    }
}
