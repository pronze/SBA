package io.github.pronze.sba.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.bukkit.utils.nms.Version;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GeneratorSplitterListener {

    @OnPostEnable
    public void onPostEnable() {
        if (Version.isVersion(1, 12)) {
            SBA.getInstance().registerListener(new GeneratorSplitterListener112());
        } else {
            SBA.getInstance().registerListener(new GeneratorSplitterListenerBefore112());
        }
    }

    @SneakyThrows
    public static void onPickup(Player player, Item item) {
        if (Main.isPlayerInGame(player)) {
            final var game = Main.getInstance().getGameOfPlayer(player);
            final var playerTeam = game.getTeamOfPlayer(player);

            final var isSpawnedItem = game.getItemSpawners()
                    .stream()
                    .map(itemSpawner -> (List<Item>) Reflect.getField(itemSpawner, "spawnedItems"))
                    .anyMatch(items -> items.contains(item));

            if (!isSpawnedItem) {
                return;
            }

            final var allowedItems = Objects.requireNonNull(SBAConfig.getInstance().node("generator-splitter", "allowed-materials").
                    getList(String.class))
                    .stream()
                    .map(matName -> Material.valueOf(matName.toUpperCase()))
                    .collect(Collectors.toList());

            if (!allowedItems.contains(item.getItemStack().getType())) {
                return;
            }

            player.getWorld().getNearbyEntities(player.getLocation(), 1.4, 1.4, 1.4)
                    .stream()
                    .filter(entity -> !entity.equals(player))
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .filter(Main::isPlayerInGame)
                    .forEach(nearbyPlayer -> {
                        final var nearbyPlayerTeam = game.getTeamOfPlayer(nearbyPlayer);
                        if (nearbyPlayerTeam == playerTeam) {
                            nearbyPlayer.getInventory().addItem(item.getItemStack().clone());
                        }
                    });
        }
    }

    private static class GeneratorSplitterListener112 implements Listener {

        @SneakyThrows
        @SuppressWarnings("unchecked")
        @EventHandler
        public void onPickup(EntityPickupItemEvent event) {
            if (!((event.getEntity()) instanceof Player)) {
                return;
            }
            GeneratorSplitterListener.onPickup((Player) event.getEntity(), event.getItem());
        }
    }

    private static class GeneratorSplitterListenerBefore112 implements Listener {
        @EventHandler
        public void onPickup(PlayerPickupItemEvent event) {
            GeneratorSplitterListener.onPickup(event.getPlayer(), event.getItem());
        }
    }
}
