package pronze.hypixelify.game;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.lib.nms.entity.ArmorStandNMS;
import org.screamingsandals.bedwars.lib.nms.holograms.Hologram;
import org.screamingsandals.bedwars.lib.ext.paperlib.PaperLib;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.screamingsandals.bedwars.lib.nms.utils.ClassStorage.NMS.PacketPlayOutEntityDestroy;

@Data
public class RotatingGenerators implements pronze.hypixelify.api.game.RotatingGenerators {

    public static final String entityName = "sba_rot_entity";
    public static List<RotatingGenerators> cache = new ArrayList<>();
    private final ItemSpawner itemSpawner;
    protected BukkitTask rotatingTask;
    private ArmorStand armorStand;
    private Location location;
    private ItemStack itemStack;
    private int time;
    private int tierLevel;
    private Game game;

    public RotatingGenerators(ItemSpawner spawner,
                              ItemStack itemStack,
                              Game game) {
        this.game = game;
        this.location = spawner.getLocation();
        this.itemStack = itemStack;
        this.itemSpawner = spawner;
        cache.add(this);
    }

    public static void destroy(List<RotatingGenerators> rotatingGenerators) {
        if (rotatingGenerators == null || rotatingGenerators.isEmpty()) {
            return;
        }
        rotatingGenerators.stream()
                .filter(Objects::nonNull)
                .forEach(pronze.hypixelify.api.game.RotatingGenerators::destroy);
        cache.removeAll(rotatingGenerators);
    }

    public static boolean canBeUsed(ItemSpawner spawner) {
        final var type = spawner.getItemSpawnerType().getMaterial();
        return type == Material.DIAMOND || type == Material.EMERALD;
    }

    public void scheduleTask() {
        rotatingTask = new BukkitRunnable() {
            @Override
            public void run() {
                location.setYaw(location.getYaw() + 10f);
                armorStand.teleport(location);
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 2L);
    }

    public ArmorStand getArmorStandEntity() {
        return armorStand;
    }

    @SuppressWarnings("unchecked")
    public RotatingGenerators spawn(List<Player> players) {
        destroy();
        if (!itemSpawner.getHologramEnabled()) {
            return null;
        }

        var holoHeight = SBAHypixelify.getConfigurator()
                .config.getDouble("floating-generator.holo-height", 2.0);
        try {
            final var countdownHologramField = game.getClass()
                    .getDeclaredField("countdownHolograms");
            countdownHologramField.setAccessible(true);

            final var countdownHologram = (Hologram) ((Map<?, ?>) countdownHologramField.get(game))
                    .get(itemSpawner);

            final var entities = ((List<ArmorStandNMS>) countdownHologram
                    .getClass()
                    .getDeclaredField("entities")
                    .get(holoHeight));

            final var updateField = countdownHologram
                    .getClass()
                    .getDeclaredMethod("update",
                            Player.class,
                            List.class,
                            boolean.class);

            final var destroyPacketField = countdownHologram
                    .getClass()
                    .getDeclaredMethod("getFullDestroyPacket");

            destroyPacketField.setAccessible(true);

            updateField.setAccessible(true);

            //destroy holograms here

            final var destroyPacket = PacketPlayOutEntityDestroy
                    .getConstructor(int[].class)
                    .newInstance(
                            (Object) entities
                                    .stream()
                                    .map(ArmorStandNMS::getId)
                                    .mapToInt(i -> i)
                                    .toArray()
                    );

            //let's update positions first
            final var locationField = countdownHologram
                    .getClass()
                    .getDeclaredField("location");
            locationField.setAccessible(true);
            locationField.set(countdownHologram, location.clone()
                    .subtract(
                            0,
                            Main.getConfigurator().config.getDouble("spawner-holo-height", 0.25),
                            0
                    ).add(0, holoHeight, 0));

            // destroy holograms so it will be recreated on the new position
            countdownHologram.getViewers().forEach(player -> {
                try {
                    updateField.invoke(
                            countdownHologram,
                            player,
                            List.of(destroyPacket),
                            true
                    );
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });

        } catch (Throwable t) {
            Logger.trace(t.getMessage());
        }

        final var itemHeight = SBAHypixelify.getConfigurator()
                .config.getDouble("floating-generator.item-height", 0.25);


        PaperLib.getChunkAtAsync(location)
                .thenAccept(chunk -> {
                    armorStand = (ArmorStand) location.getWorld().
                            spawnEntity(location.clone().add(0, itemHeight, 0), EntityType.ARMOR_STAND);
                    armorStand.setCustomName(entityName);
                    armorStand.setVisible(false);
                    armorStand.setHelmet(itemStack);
                    armorStand.setGravity(false);

                    //make sure there aren't any other rotating generator entities in the same area
                    armorStand.getLocation().getWorld().getNearbyEntities(armorStand.getLocation()
                            , 1, 1, 1)
                            .stream()
                            .filter(entity -> entity.getLocation().getBlock().equals(armorStand.getLocation().getBlock()) && !entity.equals(armorStand))
                            .forEach(Entity::remove);
                    scheduleTask();
                });
        return this;
    }

    public void destroy() {
        if (armorStand != null)
            armorStand.remove();
        cancelTask();
    }

    public void cancelTask() {
        SBAUtil.cancelTask(rotatingTask);
    }
}
