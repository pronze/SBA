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
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.bukkit.utils.nms.Version;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GeneratorSplitterListener {
    private static List<Material> allowedMaterials = new ArrayList<>();

    @SneakyThrows
    @OnPostEnable
    public void onPostEnable() {
        if (Version.isVersion(1, 12)) {
            SBA.getInstance().registerListener(new GeneratorSplitterListener112());
        } else {
            SBA.getInstance().registerListener(new GeneratorSplitterListenerBefore112());
        }
        allowedMaterials = Objects.requireNonNull(SBAConfig.getInstance().node("generator-splitter", "allowed-materials").
                getList(String.class))
                .stream()
                .map(matName -> Material.valueOf(matName.toUpperCase().trim()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
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

            if (!allowedMaterials.contains(item.getItemStack().getType())) {
                return;
            }

            player.getWorld().getNearbyEntities(player.getLocation(), 3, 3, 3)
                    .stream()
                    .filter(entity -> !entity.equals(player))
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .filter(Main::isPlayerInGame)
                    .forEach(nearbyPlayer -> {
                        final var nearbyPlayerTeam = game.getTeamOfPlayer(nearbyPlayer);
                        if (nearbyPlayerTeam == playerTeam) {
                            nearbyPlayer.getInventory().addItem(item.getItemStack().clone());
                            Sounds.playSound(nearbyPlayer, nearbyPlayer.getLocation(),
                                    "ENTITY_ITEM_PICKUP",
                                        Sounds.ENTITY_ITEM_PICKUP, 1, 1);
                        }
                    });
        }
    }

    private static class GeneratorSplitterListener112 implements Listener {

        @SneakyThrows
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
