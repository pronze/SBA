package io.github.pronze.sba.utils;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;
import org.bukkit.Bukkit;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.serialize.SerializationException;

@Service
public class FirstStartConfigReplacer {

    @OnPostEnable
    public void onPostEnable() {
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
