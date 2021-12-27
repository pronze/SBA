package io.github.pronze.sba.visual.generator;

import io.github.pronze.sba.SBWAddonAPI;
import io.github.pronze.sba.lang.LangKeys;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskState;
import org.screamingsandals.lib.tasker.task.TaskerTask;
import org.screamingsandals.lib.utils.Pair;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.lib.world.LocationHolder;

import java.util.List;

@Data
public final class RotatingGeneratorImpl implements RotatingGenerator {
    @Setter(AccessLevel.PRIVATE)
    private Hologram hologram;
    private final LocationHolder location;
    private int time;
    private int tierLevel = 1;
    private final ItemSpawner itemSpawner;
    private final ItemStack stack;

    @Setter(AccessLevel.PRIVATE)
    private TaskerTask hologramTask;
    private final List<Item> spawnedItems;

    @SuppressWarnings("unchecked")
    public RotatingGeneratorImpl(@NotNull ItemSpawner itemSpawner,
                                 @NotNull ItemStack stack,
                                 @NotNull LocationHolder location) {
        this.itemSpawner = itemSpawner;
        this.stack = stack;
        this.location = location;
        this.time = itemSpawner.getItemSpawnerType().getInterval() + 1;
        this.spawnedItems = (List<Item>) Reflect.getField(itemSpawner, "spawnedItems");
    }

    @Override
    public void updateLines(@NotNull List<Component> newLines) {
        hologram.setLines(newLines);
    }

    @Override
    public void destroy() {
        if (hologramTask != null
                && hologramTask.getState() != TaskState.CANCELLED) {
            hologramTask.cancel();
            hologramTask = null;
        }

        if (!hologram.isDestroyed()) {
            hologram.destroy();
        }
    }

    @Override
    public void spawn() {
        final var holoHeight = SBWAddonAPI
                .getInstance()
                .getConfigurator()
                .node("floating-generator", "height")
                .getDouble(2.0);

        hologram = HologramManager.hologram(location.add(0, holoHeight, 0));
        hologram.setItem(ItemFactory.build(stack).orElseThrow())
                .setItemPosition(Hologram.ItemPosition.BELOW)
                .setRotationMode(Hologram.RotationMode.Y)
                .setRotationTime(Pair.of(1, TaskerTime.TICKS));
        hologram.show();

        startTasks();
    }

    private void startTasks() {
        if (hologramTask != null) {
            hologramTask.cancel();
        }

        hologramTask = Tasker.build(() -> {
            if (isFull()) {
                final var message = Message.of(LangKeys.ROTATING_GENERATOR_FULL_TEXT_FORMAT);
                formatMessage(message);
                updateLines(message.getForAnyone());
                return;
            }

            final var message = Message.of(LangKeys.ROTATING_GENERATOR_FULL_TEXT_FORMAT);
            formatMessage(message);

            if (time <= 0 || isFull()) {
                time = itemSpawner.getItemSpawnerType().getInterval();
            }

        }).repeat(1L, TaskerTime.SECONDS).start();
    }

    @Override
    public void addViewer(@NotNull PlayerWrapper player) {
        hologram.addViewer(player);
    }

    @Override
    public void removeViewer(@NotNull PlayerWrapper player) {
        hologram.removeViewer(player);
    }

    @Override
    public boolean isFull() {
        return itemSpawner.getMaxSpawnedResources() <= spawnedItems.size();
    }

    private void formatMessage(@NotNull Message message) {
      //format
      //        .placeholder("tier", ShopUtil.romanNumerals.get(tierLevel))
      //        .placeholder("material", matName + "&6")
      //        .placeholder("seconds", String.valueOf(time));
    }
}
