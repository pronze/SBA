package io.github.pronze.sba.listener;

import com.google.gson.Gson;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.APIUtils;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsResourceSpawnEvent;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.bukkit.utils.nms.Version;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneratorSplitterListener implements Listener {
    private static List<Material> allowedMaterials = new ArrayList<>();
    private static final String SPLITTER_HASH = "SPLITTABLE";

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
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onResourceSpawn(BedwarsResourceSpawnEvent event) {
        final var resource = event.getResource();
        if (allowedMaterials.contains(resource.getType())) {
            APIUtils.hashIntoInvisibleString(resource, SPLITTER_HASH);
        }
    }

    public static void onPickup(Player player, Item item) {
        if (Main.isPlayerInGame(player)) {
            final var game = Main.getInstance().getGameOfPlayer(player);
            final var playerTeam = game.getTeamOfPlayer(player);

            if (!APIUtils.unhashFromInvisibleString(item.getItemStack(), SPLITTER_HASH)) {
                Logger.trace("GSL: Not spawned item!");
                return;
            }

            removeHash(item.getItemStack(), SPLITTER_HASH);
            Logger.trace("GSL: Hit spawned item!");
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

    public static void removeHash(ItemStack stack, String hash) {
        final var meta = stack.getItemMeta();
        try {
            final var key = new NamespacedKey((Plugin) BedwarsAPI.getInstance(), APIUtils.BEDWARS_NAMESPACED_KEY);
            var container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.STRING, new Gson().toJson(new ArrayList<>()));
        } catch (Throwable ignored) {
            // Use the Lore API instead
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.removeIf(convertToInvisibleString(hash)::equals);
            meta.setLore(lore);
        }
        stack.setItemMeta(meta);
    }

    private static String convertToInvisibleString(String s) {
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) {
            hidden.append(ChatColor.COLOR_CHAR + "").append(c);
        }
        return hidden.toString();
    }

    private static class GeneratorSplitterListener112 implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPickup(EntityPickupItemEvent event) {
            if (!((event.getEntity()) instanceof Player)) {
                return;
            }
            GeneratorSplitterListener.onPickup((Player) event.getEntity(), event.getItem());
        }
    }

    private static class GeneratorSplitterListenerBefore112 implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPickup(PlayerPickupItemEvent event) {
            GeneratorSplitterListener.onPickup(event.getPlayer(), event.getItem());
        }
    }
}
