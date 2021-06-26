package io.github.pronze.sba.game;

import io.github.pronze.sba.utils.ShopUtil;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
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
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.SBAUtil;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.material.builder.ItemFactory;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.Pair;
import org.screamingsandals.lib.world.LocationMapper;

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

    private BukkitTask hologramTask;
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
                .node("floating-generator", "holo-height").getDouble(0.5);

        hologram = HologramManager
                .hologram(LocationMapper.wrapLocation(location.clone().add(0, holoHeight, 0)));
        hologram
                .item(
                        ItemFactory
                                .build(stack)
                                .orElseThrow()
                )
                .itemPosition(Hologram.ItemPosition.BELOW)
                .rotationMode(Hologram.RotationMode.X)
                .rotationTime(Pair.of(2, TaskerTime.TICKS));

        scheduleTasks();
    }

    @Override
    public void addViewer(Player player) {
        hologram.addViewer(PlayerMapper.wrapPlayer(player));
    }

    @Override
    public void removeViewer(Player player) {
        hologram.removeViewer(PlayerMapper.wrapPlayer(player));
    }

    protected void scheduleTasks() {
        // cancel tasks if pending
        SBAUtil.cancelTask(hologramTask);


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
            hologram.newLine(i, Component.text(newLines.get(i)));
        }
        this.lines = new ArrayList<>(newLines);
    }

    public void destroy() {
        SBAUtil.cancelTask(hologramTask);
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
