package io.github.pronze.sba.config;

import io.github.pronze.sba.SBA;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.parameters.ConfigFile;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@RequiredArgsConstructor
@Service
public final class SBAConfig implements MainConfig {

    @ConfigFile("config.yml")
    private final YamlConfigurationLoader loader;

    private ConfigurationNode root;

    @Override
    public ConfigurationNode node(Object... keys) {
        return root.node(keys);
    }

    @OnEnable
    public void onEnable(SBA plugin) {
        plugin.saveResource("config.yml", false);
    }

    @OnPostEnable
    public void postEnable() {
        try {
            root = loader.load();
        } catch (ConfigurateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            loader.save(root);
        } catch (ConfigurateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public boolean isGameBlacklisted(@NotNull Game game) {
        final var blacklistedGames = node("games", "blacklisted")
                .getList(String.class);

        return blacklistedGames != null && blacklistedGames.contains(game.getName());
    }
}
