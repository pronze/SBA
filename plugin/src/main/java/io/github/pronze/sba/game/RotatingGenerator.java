package io.github.pronze.sba.game;

import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import io.github.pronze.sba.utils.ShopUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.ItemSpawner;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.SBAUtil;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.Pair;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.lib.world.LocationMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RotatingGenerator implements IRotatingGenerator {
    private Location location;
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
    private List<Item> spawnedItems;

    @SuppressWarnings("unchecked")
    public RotatingGenerator(ItemSpawner itemSpawner, ItemStack stack, Location location) {
        this.itemSpawner = itemSpawner;
        this.stack = stack;
        this.location = location;
        this.time = itemSpawner.getItemSpawnerType().getInterval() + 1;
        this.spawnedItems = (List<Item>) Reflect.getField(itemSpawner, "spawnedItems");
    }

    @Override
    public void spawn(@NotNull List<Player> viewers) {
        final var holoHeight = SBAConfig.getInstance()
                .node("floating-generator", "height").getDouble(2.0);

        hologram = HologramManager.hologram(LocationMapper.wrapLocation(location.clone().add(0, holoHeight, 0)));
        hologram.item(ItemFactory.build(stack).orElseThrow())
                .itemPosition(Hologram.ItemPosition.BELOW)
                .rotationMode(Hologram.RotationMode.Y)
                .rotationTime(Pair.of(1, TaskerTime.TICKS));

        hologram.show();
        viewers.forEach(player -> hologram.addViewer(PlayerMapper.wrapPlayer(player)));
        scheduleTasks();
    }

    @Override
    public void addViewer(@NotNull Player player) {
        hologram.addViewer(PlayerMapper.wrapPlayer(player));
    }

    @Override
    public void removeViewer(@NotNull Player player) {
        hologram.removeViewer(PlayerMapper.wrapPlayer(player));
    }

    @SuppressWarnings("unchecked")
    protected void scheduleTasks() {
        // cancel tasks if pending
        SBAUtil.cancelTask(hologramTask);

        hologramTask = new BukkitRunnable() {
            @Override
            public void run() {
                boolean full = itemSpawner.getMaxSpawnedResources() <= spawnedItems.size();
                if (!full) {
                    time--;
                }

                final var format = !full ? Message.of(LangKeys.ROTATING_GENERATOR_FORMAT) :
                        Message.of(LangKeys.ROTATING_GENERATOR_FULL_TEXT_FORMAT);


                final var matName = itemSpawner.getItemSpawnerType().getMaterial() ==
                        Material.EMERALD ? "§a" + AdventureHelper.toLegacy(Message.of(LangKeys.EMERALD).asComponent()) :
                        "§b" + AdventureHelper.toLegacy(Message.of(LangKeys.DIAMOND).asComponent());

                format
                        .placeholder("tier", ShopUtil.romanNumerals.get(tierLevel))
                        .placeholder("material", matName + "&6")
                        .placeholder("seconds", String.valueOf(time));

                update(format.getForAnyone());

                if (time <= 0 || full) {
                    time = itemSpawner.getItemSpawnerType().getInterval();
                }
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 20L);
    }

    @Override
    public void update(@NotNull List<Component> newLines) {
        for (int i = 0; i < newLines.size(); i++) {
            hologram.replaceLine(i, newLines.get(i));
        }
    }

    public void destroy() {
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
