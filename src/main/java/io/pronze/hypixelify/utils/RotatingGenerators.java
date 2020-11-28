package io.pronze.hypixelify.utils;

import io.pronze.hypixelify.SBAHypixelify;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.bedwars.lib.nms.holograms.Hologram;

import java.util.*;

public class RotatingGenerators {

    public static List<RotatingGenerators> cache = new ArrayList<>();
    private final List<String> lines;
    private Game game;
    private ArmorStand armorStand;
    private Location location;
    private ItemStack itemStack;
    private Hologram hologram;
    private ItemSpawner itemSpawner;
    private int time;

    public RotatingGenerators(ItemSpawner spawner,
                              ItemStack itemStack,
                              List<String> lines,
                              Game game) {
        this.location = spawner.getLocation();
        this.itemStack = itemStack;
        this.lines = lines;
        this.game = game;
        this.itemSpawner = spawner;
        time = spawner.getItemSpawnerType().getInterval() + 1;
        cache.add( this);
    }

    public static void scheduleTask() {
        Scheduler.runTimerTask(() -> {
            cache.stream().filter(Objects::nonNull).forEach(generator -> {
                final Location loc = generator.location;
                if (loc == null || generator.armorStand == null)
                    return;

                loc.setYaw(loc.getYaw() + 10f);
                generator.armorStand.teleport(loc);
                generator.location = loc;
            });
        }, 0L, 2L);

        Scheduler.runTimerTask(()->{
            cache.stream().filter(Objects::nonNull).forEach(generator -> {
                if( generator.hologram == null){
                    return;
                }
                generator.time--;


                generator.hologram.setLine(2, "§eSpawns in §c{seconds} §eseconds".replace("{seconds}",
                        String.valueOf(generator.time)));

                if(generator.time <= 0){
                    generator.time = generator.itemSpawner.getItemSpawnerType().getInterval();
                }
            });
        }, 0L, 20L);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public RotatingGenerators spawn(List<Player> players) {
        float holoHeight = (float) SBAHypixelify.getConfigurator()
                .config.getDouble("floating-generator.holo-height", 2.0);

        float itemHeight = (float) SBAHypixelify.getConfigurator()
                .config.getDouble("floating-generator.item-height", 0.25);


        hologram = Main.getHologramManager()
                .spawnHologram(players, location.clone().add(0, holoHeight, 0), lines.toArray(new String[0]));

        armorStand = (ArmorStand) location.getWorld().
                spawnEntity(location.clone().add(0, itemHeight, 0), EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        armorStand.setHelmet(itemStack);
        armorStand.setGravity(false);


        return this;
    }

    public void update(List<String> lines) {
        if (lines == null || lines.size() < 1) {
            return;
        }

        if (this.lines.equals(lines)) {
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            hologram.setLine(i, lines.get(i));
        }

    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemSpawner getItemSpawner() {
        return itemSpawner;
    }

    public void setLine(int index, String line){
        hologram.setLine(index, line);
    }

    public void destroy() {
        if (armorStand != null)
            armorStand.remove();
        if (hologram != null)
            hologram.destroy();

        cache.remove(this);
    }


}
