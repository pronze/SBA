package io.github.pronze.sba.config;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public interface ConfiguratorAPI {

    static List<ConfiguratorAPI> getAllConfigurations() {
        return ServiceManager.getAll(ConfiguratorAPI.class);
    }

    ConfigurationNode node(Object... keys);

    void save();

    boolean isGameBlacklisted(@NotNull Game game);
}
