package io.github.pronze.sba.service;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.item.ItemTypeHolder;
import org.screamingsandals.lib.item.ItemTypeMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import java.util.List;
import java.util.Map;

@Service
public class SBWConfigModifier {

    @OnPostEnable
    public void onPostEnable() {
        updateConfig("join-randomly-after-lobby-timeout", true);
        updateConfig("spawner-holograms", false);
        updateConfig("game-start-items", true);
        updateConfig("gived-game-start-items", List.of("WOODEN_SWORD", "LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS"));
        updateConfig("destroy-placed-blocks-by-explosion-except", "GLASS");
        updateConfig("allowed-commands", List.of("/shout", "/party"));
        updateConfig("scoreboard.enable", false);
        updateConfig("lobby-scoreboard.enabled", false);
        updateConfig("chat.override", false);
        updateConfig("title.enabled", false);
        updateConfig("items.leavegame", "RED_BED");
        updateConfig("player-drops", false);
        updateConfig("compass-enabled", false);
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

    protected void updateConfig(@NotNull String path, Object value) {
        if (Main.getConfigurator().config.isSet(path)) {
            Main.getConfigurator().config.set(path, null);
        }
        if (value instanceof Map) {
            Main.getConfigurator().config.createSection(path, (Map<?, ?>) value);
        } else {
            Main.getConfigurator().config.set(path, value);
        }
    }

    // TODO: Use this for BedWars as 0.2.x is not using SLib.
    protected String getVersionBasedMaterial(@NotNull String material) {
        return ItemTypeMapper.resolve(material)
                .map(ItemTypeHolder::platformName)
                .orElseThrow();
    }
}
