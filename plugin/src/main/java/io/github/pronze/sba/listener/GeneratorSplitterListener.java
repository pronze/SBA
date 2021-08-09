package io.github.pronze.sba.listener;

import io.github.pronze.sba.config.SBAConfig;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerPickupItemEvent;
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
        allowedMaterials = Objects.requireNonNull(SBAConfig.getInstance().node("generator-splitter", "allowed-materials").
                getList(String.class))
                .stream()
                .map(matName -> Material.valueOf(matName.toUpperCase().trim()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @OnEvent
    public void onItemPickup(SPlayerPickupItemEvent event) {
        final var bukkitPlayer = event.getPlayer().as(Player.class);
        if (!Main.isPlayerInGame(bukkitPlayer)) {
            return;
        }

        final var game = Main.getInstance().getGameOfPlayer(bukkitPlayer);
        final var playerTeam = game.getTeamOfPlayer(bukkitPlayer);
        final var bukkitItem = event.getItem().as(Item.class);

        final var isSpawnedItem = game.getItemSpawners()
                .stream()
                .map(itemSpawner -> (List<Item>) Reflect.getField(itemSpawner, "spawnedItems"))
                .anyMatch(items -> items.contains(bukkitItem));

        if (!isSpawnedItem) {
            return;
        }

        if (!allowedMaterials.contains(bukkitItem.getItemStack().getType())) {
            return;
        }

        bukkitPlayer.getWorld().getNearbyEntities(bukkitPlayer.getLocation(), 3, 3, 3)
                .stream()
                .filter(entity -> !entity.equals(bukkitPlayer))
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(Main::isPlayerInGame)
                .forEach(nearbyPlayer -> {
                    final var nearbyPlayerTeam = game.getTeamOfPlayer(nearbyPlayer);
                    if (nearbyPlayerTeam == playerTeam) {
                        nearbyPlayer.getInventory().addItem(bukkitItem.getItemStack().clone());
                        Sounds.playSound(nearbyPlayer, nearbyPlayer.getLocation(),
                                "ENTITY_ITEM_PICKUP",
                                Sounds.ENTITY_ITEM_PICKUP, 1, 1);
                    }
                });
    }
}
