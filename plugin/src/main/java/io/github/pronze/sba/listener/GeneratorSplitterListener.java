package io.github.pronze.sba.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import lombok.RequiredArgsConstructor;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.APIUtils;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.lib.nms.utils.Version;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public final class GeneratorSplitterListener implements Listener {
    private static final String SPLITTER_HASH = "SPLITTABLE";

    private final LoggerWrapper logger;
    private final SBA plugin;
    private final SBAConfig config;
    private final List<Material> allowedMaterials = new ArrayList<>();

    @SneakyThrows
    @OnPostEnable
    public void onPostEnable() {
        if (!config.node("generator-splitter", "enabled").getBoolean(true)) {
            return;
        }

        if (Version.isVersion(1, 12)) {
            plugin.registerListener(new GeneratorSplitterListener112(this));
        } else {
            plugin.registerListener(new GeneratorSplitterListenerBefore112(this));
        }

        Objects.requireNonNull(
                        config.node("generator-splitter", "allowed-materials").getList(String.class)
                )
                .stream()
                .map(matName -> Material.valueOf(matName.toUpperCase().trim()))
                .forEach(allowedMaterials::add);
        plugin.registerListener(this);
    }

    public void onPickup(Player player, Item item) {
        if (!Main.isPlayerInGame(player)) {
            return;
        }

        final var game = Main.getInstance().getGameOfPlayer(player);
        final var playerTeam = game.getTeamOfPlayer(player);

        if (!APIUtils.unhashFromInvisibleString(item.getItemStack(), SPLITTER_HASH)) {
            return;
        }

        if (!allowedMaterials.contains(item.getItemStack().getType())) {
            return;
        }

        removeHash(item.getItemStack(), SPLITTER_HASH);
        logger.trace("Detected spawned item, trying to split..");
        player.getWorld().getNearbyEntities(player.getLocation(), 3, 0, 3)
                .stream()
                .filter(entity -> !entity.equals(player))
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(Main::isPlayerInGame)
                .forEach(nearbyPlayer -> {
                    final var nearbyPlayerTeam = game.getTeamOfPlayer(nearbyPlayer);
                    if (nearbyPlayerTeam == playerTeam) {
                        nearbyPlayer.getInventory().addItem(item.getItemStack().clone());

                        if (config.node("generator-splitter", "sounds-enabled").getBoolean(true)) {
                            Sounds.playSound(nearbyPlayer, nearbyPlayer.getLocation(),
                                    "ENTITY_ITEM_PICKUP",
                                    Sounds.ENTITY_ITEM_PICKUP, 1, 1);
                        }
                    }
                });
    }

    private static void removeHash(@NotNull ItemStack stack, @NotNull String hash) {
        final var meta = stack.getItemMeta();
        try {
            final var key = new NamespacedKey((Plugin) BedwarsAPI.getInstance(), APIUtils.BEDWARS_NAMESPACED_KEY);
            var container = meta.getPersistentDataContainer();
            container.remove(key);
        } catch (Throwable ignored) {
            // Use the Lore API instead
            var lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
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

    @RequiredArgsConstructor
    private static class GeneratorSplitterListener112 implements Listener {
        private final GeneratorSplitterListener listener;

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPickup(EntityPickupItemEvent event) {
            if (!((event.getEntity()) instanceof Player)) {
                return;
            }
            listener.onPickup((Player) event.getEntity(), event.getItem());
        }
    }

    @RequiredArgsConstructor
    private static class GeneratorSplitterListenerBefore112 implements Listener {
        private final GeneratorSplitterListener listener;

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPickup(PlayerPickupItemEvent event) {
            listener.onPickup(event.getPlayer(), event.getItem());
        }
    }
}
