package io.github.pronze.sba.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.WorldMapper;
import org.spongepowered.configurate.ConfigurationNode;
import java.util.Optional;

@UtilityClass
public class LocationUtils {

    @NotNull
    public Optional<LocationHolder> locationFromNode(ConfigurationNode locationNode) {
        try {
            final var world = WorldMapper.getWorld(locationNode.node("world").getString()).orElseThrow();
            final var x = locationNode.node("x").getDouble(0.0D);
            final var y = locationNode.node("y").getDouble(0.0D);
            final var z = locationNode.node("z").getDouble(0.0D);
            final var yaw = locationNode.node("yaw").getFloat(0.0F);
            final var pitch= locationNode.node("pitch").getFloat(0.0F);

            final var locationHolder = new LocationHolder(x, y, z, yaw, pitch, world);
            return Optional.of(locationHolder);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }
}
