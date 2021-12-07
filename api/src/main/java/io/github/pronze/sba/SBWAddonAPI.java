package io.github.pronze.sba;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * The SBWAddonAPI for external developmental purposes.
 */
public interface SBWAddonAPI {

    @NotNull
    static SBWAddonAPI getInstance() {
        final var registration = Bukkit.getServicesManager().getRegistration(SBWAddonAPI.class);
        if (registration == null) {
            throw new UnsupportedOperationException("Addon has not yet been initialized! (wrong/missing plugin?)");
        }
        return registration.getProvider();
    }

    boolean isSnapshot();
}
