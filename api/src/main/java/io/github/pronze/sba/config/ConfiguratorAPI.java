package io.github.pronze.sba.config;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.spongepowered.configurate.ConfigurationNode;

public interface ConfiguratorAPI {

    ConfigurationNode node(Object... keys);

    void save();

    boolean isGameBlacklisted(@NotNull Game game);
}
