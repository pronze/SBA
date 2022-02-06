package io.github.pronze.sba;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.Wrapper;

/**
 * The SBWAddonAPI for external developmental purposes.
 */
public interface SBWAddonAPI extends Wrapper {

    @NotNull
    static SBWAddonAPI getInstance() {
        final var registration = Bukkit.getServicesManager().getRegistration(SBWAddonAPI.class);
        if (registration == null) {
            throw new UnsupportedOperationException("Addon has not yet been initialized! (wrong/missing plugin?)");
        }
        return registration.getProvider();
    }

    @NotNull
    default JavaPlugin asJavaPlugin() {
        return as(JavaPlugin.class);
    }

    boolean isSnapshot();
}
