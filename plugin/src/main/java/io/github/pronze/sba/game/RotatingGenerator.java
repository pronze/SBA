package io.github.pronze.sba.game;

import io.github.pronze.sba.utils.ShopUtil;
import lombok.Getter;
import lombok.Setter;
import org.screamingsandals.lib.item.builder.ItemStackFactory;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.spectator.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.ItemSpawner;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RotatingGenerator implements IRotatingGenerator {
    private Location location;
    private List<String> lines;
    private int time;
    @Getter
    @Setter
    private int tierLevel = 1;
    @Getter
    private final ItemSpawner itemSpawner;
    @Getter
    private final ItemStack stack;

    private BukkitTask hologramTask;
    private Hologram hologram;

    public RotatingGenerator(ItemSpawner itemSpawner, ItemStack stack, Location location) {
        this.itemSpawner = itemSpawner;
        this.stack = stack;
        this.location = location;
        this.time = itemSpawner.getItemSpawnerType().getInterval() + 1;
        this.lines = LanguageService
                .getInstance()
                .get(MessageKeys.ROTATING_GENERATOR_FORMAT)
                .toStringList();
    }

    @Override
    public void spawn(@NotNull List<Player> viewers) {
        Logger.trace("RotatingGenerator::spawn ({},{})", this, viewers);

        final var holoHeight = SBAConfig.getInstance()
                .node("floating-generator", "height").getDouble(2.0);

        hologram = HologramManager.hologram(Objects.requireNonNull(org.screamingsandals.lib.world.Location.fromPlatform(location.clone().add(0, holoHeight, 0))));
        hologram.item(ItemStackFactory.build(stack))
                .itemPosition(Hologram.ItemPosition.BELOW)
                .rotationMode(Hologram.RotationMode.Y)
                .rotationTime(Pair.of(1, TaskerTime.TICKS));

        hologram.show();
        viewers.forEach(player -> hologram.addViewer(Players.wrapPlayer(player)));
        scheduleTasks();
    }

    @Override
    public void addViewer(@NotNull Player player) {
        if (hologram != null)
            hologram.addViewer(Players.wrapPlayer(player));
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        hologram.removeViewer(Players.wrapPlayer(player));
    }

    protected void scheduleTasks() {
        // cancel tasks if pending
        SBAUtil.cancelTask(hologramTask);

        hologramTask = new BukkitRunnable() {
            @Override
            public void run() {
                hologram.show();
                // Logger.trace("RotatingGenerator::hologramTask ({},{})", this,hologramTask);

                boolean full;
                if (SBA.sbw_0_2_30) {
                    // SBW changed the maxSpawnedResources logic and introduced getSpawnedItemsCount() method,
                    // we should use it here to prevent hologram synchronization issues
                    full = itemSpawner.getMaxSpawnedResources() <= itemSpawner.getSpawnedItemsCount();
                } else {
                    full = itemSpawner.getMaxSpawnedResources() <= itemSpawner.spawnedItems.size();
                }
                if (!full) {
                    time--;
                }
                final var format = !full ? LanguageService
                        .getInstance()
                        .get(MessageKeys.ROTATING_GENERATOR_FORMAT)
                        .toStringList() :

                        LanguageService
                                .getInstance()
                                .get(MessageKeys.ROTATING_GENERATOR_FULL_TEXT_FORMAT)
                                .toStringList();

                final var newLines = new ArrayList<String>();
                final var matName = itemSpawner.getItemSpawnerType().getMaterial() == Material.EMERALD
                        ? "§a" + LanguageService
                                .getInstance()
                                .get(MessageKeys.EMERALD)
                                .toString()
                        : "§b" + LanguageService
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

                if (time <= 0 || full) {
                    if (SBA.sbw_0_2_30) {
                        // SBW now allows to dynamically change the spawner interval during the game by changing this property,
                        // we should use it for holograms to prevent synchronization issues
                        time = itemSpawner.currentCycle;
                    } else {
                        time = itemSpawner.getItemSpawnerType().getInterval();
                    }
                }
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 20L);
    }

    @Override
    public void update(@NotNull List<String> newLines) {
        if (newLines.equals(lines)) {
            return;
        }
        for (int i = 0; i < newLines.size(); i++) {
            hologram.replaceLine(i, Component.text(newLines.get(i)));
        }
        this.lines = new ArrayList<>(newLines);
    }

    public void destroy() {
        Logger.trace("RotatingGenerator::destroy ({})", this);

        SBAUtil.cancelTask(hologramTask);
        if (hologram != null) {
            hologram.destroy();
            hologram = null;
        }
    }

    @Override
    public void setLocation(@NotNull Location location) {
        this.location = location;
    }
}
