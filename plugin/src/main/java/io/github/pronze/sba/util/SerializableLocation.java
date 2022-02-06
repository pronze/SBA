package io.github.pronze.sba.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.screamingsandals.lib.utils.Wrapper;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.WorldMapper;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

// from bw 0.3.x

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigSerializable
public class SerializableLocation implements Wrapper {
    private String world;
    private double x;
    private double y;
    private double z;
    private double yaw = 0;
    private double pitch = 0;

    public SerializableLocation(LocationHolder location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public boolean isWorldLoaded() {
        return WorldMapper.getWorld(world).isPresent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T as(Class<T> type) {
        if (type == LocationHolder.class) {
            var holder = new LocationHolder(x, y, z);
            holder.setYaw((float) this.yaw);
            holder.setPitch((float) this.pitch);
            holder.setWorld(WorldMapper.getWorld(world).orElseThrow());
            return (T) holder;
        }
        throw new UnsupportedOperationException("Unsupported type!");
    }
}
