package pronze.hypixelify.game;

import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.utils.SBAUtil;
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
import org.screamingsandals.bedwars.lib.nms.holograms.Hologram;
import org.screamingsandals.lib.paperlib.PaperLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class RotatingGenerators implements pronze.hypixelify.api.game.RotatingGenerators {

    public static final String entityName = "sba_rot_entity";
    public static List<RotatingGenerators> cache = new ArrayList<>();
    private List<String> lines;
    public static List<String> format = new ArrayList<>();
    private Hologram hologram;
    private ArmorStand armorStand;
    private Location location;
    private ItemStack itemStack;
    private final ItemSpawner itemSpawner;
    private int time;
    private int tierLevel;

    protected BukkitTask rotatingTask;
    protected BukkitTask hologramTask;

    public RotatingGenerators(ItemSpawner spawner,
                              ItemStack itemStack,
                              List<String> lines) {
        this.location = spawner.getLocation();
        this.itemStack = itemStack;
        this.lines = lines;
        this.itemSpawner = spawner;
        time = spawner.getItemSpawnerType().getInterval() + 1;
        cache.add(this);
    }

    public void scheduleTask() {
        rotatingTask = new BukkitRunnable() {
            @Override
            public void run() {
                location.setYaw(location.getYaw() + 10f);
                armorStand.teleport(location);
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 2L);

        hologramTask = new BukkitRunnable() {
            @Override
            public void run() {
                time--;

                final var newLines = new ArrayList<String>();
                final var matName = getItemSpawner().getItemSpawnerType().getMaterial() ==
                        Material.EMERALD ? "§a" + SBAHypixelify.getConfigurator().config
                        .getString("message.emerald", "Emerald&e") :
                        "§b" + SBAHypixelify.getConfigurator().config.getString("message.diamond");

                for (var line : RotatingGenerators.format) {
                    if (line == null) {
                        continue;
                    }
                    newLines.add(line
                            .replace("{tier}", String.valueOf(tierLevel))
                            .replace("{material}", matName + "§6")
                            .replace("{seconds}", String.valueOf(time)));
                }
                update(newLines);
                if (time <= 0) {
                    time = itemSpawner.getItemSpawnerType().getInterval();
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
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

    public ArmorStand getArmorStandEntity() {
        return armorStand;
    }

    public RotatingGenerators spawn(List<Player> players) {
        destroy();

        final var holoHeight = SBAHypixelify.getConfigurator()
                .config.getDouble("floating-generator.holo-height", 2.0);

        final var itemHeight = SBAHypixelify.getConfigurator()
                .config.getDouble("floating-generator.item-height", 0.25);

        hologram = Main.getHologramManager()
                .spawnHologram(players, location.clone().add(0, holoHeight, 0), lines.toArray(new String[0]));

        PaperLib.getChunkAtAsync(location)
                .thenAccept(chunk-> {
                    armorStand = (ArmorStand) location.getWorld().
                            spawnEntity(location.clone().add(0, itemHeight, 0), EntityType.ARMOR_STAND);
                    armorStand.setCustomName(entityName);
                    armorStand.setVisible(false);
                    armorStand.setHelmet(itemStack);
                    armorStand.setGravity(false);

                    //make sure there aren't any other rotating generator entities in the same area
                    armorStand.getLocation().getWorld().getNearbyEntitiesByType(ArmorStand.class, armorStand.getLocation()
                            , 1, 1, 1)
                            .stream()
                            .filter(entity->  entity.getLocation().getBlock().equals(armorStand.getLocation().getBlock()) && !entity.equals(armorStand))
                            .forEach(Entity::remove);
                    scheduleTask();
                });
        return this;
    }

    public void update(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        if (lines.equals(getLines())) {
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i) == null) {
                continue;
            }
            hologram.setLine(i, lines.get(i));
        }
        this.lines = new ArrayList<>(lines);
    }

    public void setLine(int index, String line) {
        hologram.setLine(index, line);
        if (lines != null) {
            lines.set(index, line);
        }
    }

    public static boolean canBeUsed(ItemSpawner spawner) {
        final var type = spawner.getItemSpawnerType().getMaterial();
        if (type == Material.DIAMOND || type == Material.EMERALD) {
            return true;
        }
        return false;
    }

    public void destroy() {
        if (armorStand != null)
            armorStand.remove();
        if (hologram != null)
            hologram.destroy();
        cancelTask();
    }

    public void cancelTask() {
        SBAUtil.cancelTask(rotatingTask);
        SBAUtil.cancelTask(hologramTask);
    }
}
