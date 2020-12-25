package io.pronze.hypixelify.game;

import io.pronze.hypixelify.SBAHypixelify;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.bedwars.lib.nms.holograms.Hologram;
import org.screamingsandals.lib.paperlib.PaperLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class RotatingGenerators implements io.pronze.hypixelify.api.game.RotatingGenerators {

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

    public static void scheduleTask() {

        Bukkit.getScheduler().runTaskTimer(SBAHypixelify.getInstance(), () -> {
            cache.stream().filter(generator -> generator != null &&
                    generator.location != null &&
                    generator.armorStand != null).forEach(generator -> {
                final Location loc = generator.location;
                loc.setYaw(loc.getYaw() + 10f);
                generator.armorStand.teleport(loc);
                generator.location = loc;
            });
        }, 0L, 2L);

        //Maybe use bedwarsgametickevent for this?
        Bukkit.getScheduler().runTaskTimer(SBAHypixelify.getInstance(), () -> {
            cache.stream().filter(generator -> generator != null && generator.hologram != null)
                    .forEach(generator -> {
                        generator.time--;


                        final var newLines = new ArrayList<String>();

                        final var matName = generator.getItemSpawner().getItemSpawnerType().getMaterial() == Material.EMERALD
                                ? "§a" + SBAHypixelify.getConfigurator().config
                                .getString("message.emerald", "Emerald&e") :
                                "§b" + SBAHypixelify.getConfigurator().config.getString("message.diamond");

                        for (var line : RotatingGenerators.format) {
                            if (line == null) {
                                continue;
                            }
                            newLines.add(line
                                    .replace("{tier}", String.valueOf(generator.getTierLevel()))
                                    .replace("{material}", matName + "§6")
                                    .replace("{seconds}", String.valueOf(generator.time)));
                        }

                        generator.update(newLines);

                        if (generator.time <= 0) {
                            generator.time = generator.itemSpawner.getItemSpawnerType().getInterval();
                        }
                    });
        }, 0L, 20L);
    }

    public static void destroy(List<RotatingGenerators> rotatingGenerators) {
        if (rotatingGenerators == null || rotatingGenerators.isEmpty()) {
            return;
        }

        rotatingGenerators.forEach(generator -> {
            if (generator == null) {
                return;
            }

            generator.destroy();
        });

        cache.removeAll(rotatingGenerators);
    }

    public ArmorStand getArmorStandEntity() {
        return armorStand;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public RotatingGenerators spawn(List<Player> players) {
        final var holoHeight = (float) SBAHypixelify.getConfigurator()
                .config.getDouble("floating-generator.holo-height", 2.0);

        final var itemHeight = (float) SBAHypixelify.getConfigurator()
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

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemSpawner getItemSpawner() {
        return itemSpawner;
    }

    public void setLine(int index, String line) {
        hologram.setLine(index, line);
        if (lines != null) {
            lines.set(index, line);
        }
    }

    public void destroy() {
        if (armorStand != null)
            armorStand.remove();
        if (hologram != null)
            hologram.destroy();
    }


}
