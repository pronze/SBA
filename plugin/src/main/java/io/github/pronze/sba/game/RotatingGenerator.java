package io.github.pronze.sba.game;

import io.github.pronze.sba.utils.ShopUtil;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
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
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.bedwars.lib.nms.holograms.Hologram;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.SBAUtil;

import java.util.ArrayList;
import java.util.List;

public class RotatingGenerator implements IRotatingGenerator {
    public static final String entityName = "sba_rot_entity";

    private Location location;
    private List<String> lines;
    private int time;
    @Getter
    @Setter
    private int tierLevel = 1;
    private final ItemSpawner itemSpawner;
    private final ItemStack stack;

    private BukkitTask rotatingTask, hologramTask;
    private ArmorStand entity;
    private Hologram hologram;

    public RotatingGenerator(ItemSpawner itemSpawner, ItemStack stack, Location location) {
        this.itemSpawner = itemSpawner;
        this.stack = stack;
        this.location = location;

        time = itemSpawner.getItemSpawnerType().getInterval() + 1;
        lines = LanguageService
                .getInstance()
                .get(MessageKeys.ROTATING_GENERATOR_FORMAT)
                .toStringList();
    }

    @Override
    public void spawn(List<Player> viewers) {
        final var holoHeight = SBAConfig.getInstance()
                .node("floating-generator", "holo-height").getDouble(2.0);

        final var itemHeight = SBAConfig.getInstance()
                .node("floating-generator", "item-height").getDouble(0.25);

        hologram = Main.getHologramManager()
                .spawnHologram(viewers, location.clone().add(0, holoHeight, 0), lines.toArray(new String[0]));

        PaperLib
                .getChunkAtAsync(location)
                .thenAccept(chunk -> {
                    entity = (ArmorStand) location.getWorld().
                            spawnEntity(location.clone().add(0, itemHeight, 0), EntityType.ARMOR_STAND);
                    entity.setCustomName(entityName);
                    entity.setVisible(false);
                    entity.setHelmet(stack);
                    entity.setGravity(false);

                    //make sure there aren't any other rotating generator entities in the same area
                    entity.getLocation().getWorld().getNearbyEntitiesByType(ArmorStand.class, entity.getLocation()
                            , 1, 1, 1)
                            .stream()
                            .filter(e1->  e1.getLocation().getBlock().equals(entity.getLocation().getBlock()) && !entity.equals(e1))
                            .forEach(Entity::remove);
                });

        scheduleTasks();
    }

    @Override
    public void addViewer(Player player) {
        hologram.addViewer(player);
    }

    @Override
    public void removeViewer(Player player) {
        hologram.removeViewer(player);
    }

    protected void scheduleTasks() {
        // cancel tasks if pending
        SBAUtil.cancelTask(rotatingTask);
        SBAUtil.cancelTask(hologramTask);

        rotatingTask = new BukkitRunnable() {
            @Override
            public void run() {
                location.setYaw(location.getYaw() + 10f);
                entity.teleport(location);
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 2L);

        hologramTask = new BukkitRunnable() {
            @Override
            public void run() {
                time--;

                final var format = LanguageService
                        .getInstance()
                        .get(MessageKeys.ROTATING_GENERATOR_FORMAT)
                        .toStringList();

                final var newLines = new ArrayList<String>();
                final var matName = itemSpawner.getItemSpawnerType().getMaterial() ==
                        Material.EMERALD ? "§a" + LanguageService
                        .getInstance()
                        .get(MessageKeys.EMERALD)
                        .toString() :
                        "§b" + LanguageService
                                .getInstance()
                                .get(MessageKeys.DIAMOND)
                                .toString();

                for (String line : format) {
                    newLines.add(line
                            .replace("%tier%", ShopUtil.romanNumerals.get(tierLevel))
                            .replace("%material%", matName + "§6")
                            .replace("%seconds%", String.valueOf(time)));
                }

                update(newLines);

                if (time <= 0) {
                    time = itemSpawner.getItemSpawnerType().getInterval();
                }
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 20L);
    }

    @Override
    public void update(List<String> newLines) {
        if (newLines == null) {
            return;
        }
        if (newLines.equals(lines)) {
            return;
        }
        for (int i = 0; i < newLines.size(); i++) {
            hologram.setLine(i, newLines.get(i));
        }
        this.lines = new ArrayList<>(newLines);
    }

    public void destroy() {
        SBAUtil.cancelTask(rotatingTask);
        SBAUtil.cancelTask(hologramTask);
        if (entity != null) {
            entity.remove();
            entity = null;
        }
        if (hologram != null) {
            hologram.destroy();
            hologram = null;
        }
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }
}
