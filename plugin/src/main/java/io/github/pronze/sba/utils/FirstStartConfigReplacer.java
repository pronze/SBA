package io.github.pronze.sba.utils;
import io.github.pronze.sba.config.SBAConfig;
import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FirstStartConfigReplacer {

    // legacy replacement map
    private static final Map<Map.Entry<String, String>, String> replacementMap = new HashMap<>() {
        {
            put(Map.entry("items.leavegame", "RED_BED"), "BED");
            put(Map.entry("items.shopcosmetic", "GRAY_STAINED_GLASS_PANE"), "STAINED_GLASS_PANE");
        }
    };

    public void enableLegacySupport() {
        //Do change for legacy support.
        if (Main.isLegacy()) {
            final var doneChanges =  new AtomicBoolean(false);

            replacementMap.forEach((key, value) -> {
                if (Main.getConfigurator().config.getString(key.getKey(), key.getValue()).equalsIgnoreCase(key.getValue())) {
                    Main.getConfigurator().config.set(key.getKey(), value);
                    doneChanges.set(true);
                }
            });

            if (doneChanges.get()) {
                Bukkit.getLogger().info("[SBA]: Making legacy changes");
                Main.getConfigurator().saveConfig();
                SBAUtil.reloadPlugin(Main.getInstance());
            }
        }
    }

    protected void updateConfig(String path, Object value) {
        if (Main.getConfigurator().config.isSet(path)) {
            Main.getConfigurator().config.set(path, null);
        }
        if (value instanceof Map) {
            Main.getConfigurator().config.createSection(path, (Map<?, ?>) value);
        } else {
            Main.getConfigurator().config.set(path, value);
        }
    }

    public void updateBedWarsConfig() {
        updateConfig("join-randomly-after-lobby-timeout", true);
        updateConfig("spawner-holograms", false);
        updateConfig("game-start-items", true);
        updateConfig("gived-game-start-items", List.of("WOODEN_SWORD", "LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS"));
        updateConfig("destroy-placed-blocks-by-explosion-except", "GLASS");
        updateConfig("allowed-commands", List.of("/shout", "/party"));
        updateConfig("scoreboard.enable", false);
        updateConfig("scoreboard.enabled", false);
        updateConfig("lobby-scoreboard.enabled", false);
        updateConfig("chat.override", false);
        updateConfig("title.enabled", false);
        updateConfig("items.leavegame", "RED_BED");
        updateConfig("player-drops", false);
        updateConfig("compass-enabled", true);
        updateConfig("add-wool-to-inventory-on-join", false);
        updateConfig("breakable.enabled", true);
        updateConfig("breakable.blocks", List.of(!Main.isLegacy() ? "GRASS" : "LONG_GRASS", "SNOW"));
        updateConfig("disable-hunger", true);
        updateConfig("specials.auto-igniteable-tnt.explosion-time", 3);
        updateConfig("resources", Map.of(
                "emerald", Map.of(
                        "material", "EMERALD",
                        "color", "GREEN",
                        "name", "Emerald",
                        "interval", 60,
                        "translate", "resource_emerald",
                        "spread", 0.1
                ),
                "diamond", Map.of(
                        "material", "DIAMOND",
                        "color", "BLUE",
                        "name", "Diamond",
                        "interval", 30,
                        "translate", "resource_diamond",
                        "spread", 0.1
                ),
                "iron", Map.of(
                        "material", "IRON_INGOT",
                        "color", "WHITE",
                        "name", "Iron",
                        "interval", 2.5,
                        "translate", "resource_iron",
                        "spread", 0.1
                ),
                "gold", Map.of(
                        "material", "GOLD_INGOT",
                        "color", "GOLD",
                        "name", "Gold",
                        "interval", 8,
                        "translate", "resource_gold",
                        "spread", 0.1
                )
        ));
        Main.getConfigurator().saveConfig();
    }

    @OnPostEnable
    public void onPostEnable() {
        enableLegacySupport();
        if (SBAConfig.getInstance().node("first_start").getBoolean(false)) {
            Bukkit.getLogger().info("§aDetected first start");
            updateBedWarsConfig();
            SBAConfig.getInstance().upgrade();
            try {
                SBAConfig.getInstance().node("first_start").set(false);
                SBAConfig.getInstance().node("autoset-bw-config").set(false);
                SBAConfig.getInstance().saveConfig();
                SBAConfig.getInstance().forceReload();
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }
    }
}
