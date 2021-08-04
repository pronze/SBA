package io.github.pronze.sba.utils;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;
import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
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

    @OnPostEnable
    public void onPostEnable() {
        enableLegacySupport();
        if (SBAConfig.getInstance().node("first_start").getBoolean(false)) {
            Bukkit.getLogger().info("§aDetected first start");
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
