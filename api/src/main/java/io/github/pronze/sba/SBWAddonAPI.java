package io.github.pronze.sba;

import io.github.pronze.sba.config.ConfiguratorAPI;
import io.github.pronze.sba.game.GameManager;
import io.github.pronze.sba.inventory.GamesInventory;
import io.github.pronze.sba.service.GameTaskManager;
import io.github.pronze.sba.service.GamePlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;
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
    default ConfiguratorAPI getConfigurator() {
        return ServiceManager.get(ConfiguratorAPI.class);
    }

    @NotNull
    default GamePlayerManager getPlayerManager() {
        return ServiceManager.get(GamePlayerManager.class);
    }

    @NotNull
    default GameManager getGameManager() {
        return ServiceManager.get(GameManager.class);
    }

    @NotNull
    default GameTaskManager getGameTaskManager() {
        return ServiceManager.get(GameTaskManager.class);
    }

    @NotNull
    default GamesInventory getGamesInventory() {
        return ServiceManager.get(GamesInventory.class);
    }

    @NotNull
    default JavaPlugin asJavaPlugin() {
        return as(JavaPlugin.class);
    }

    boolean isSnapshot();
}
