package io.github.pronze.sba;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.listener.GeneratorSplitterListener;
import io.github.pronze.sba.service.HealthIndicatorService;
import io.github.pronze.sba.service.SBWConfigModifier;
import io.github.pronze.sba.visual.sidebar.GameLobbySidebarManager;
import io.github.pronze.sba.visual.sidebar.GameSidebarManager;
import io.github.pronze.sba.visual.sidebar.LobbySidebarManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;
import java.util.ArrayList;
import java.util.List;

@Plugin(
        id = "SBA",
        authors = {"pronze"},
        loadTime = Plugin.LoadTime.POSTWORLD,
        version = "2.0-SNAPSHOT"
)
@PluginDependencies(platform = PlatformType.BUKKIT, dependencies = {
        "BedWars"
})
@Init(services = {
        UpdateChecker.class,

        // configuration
        SBAConfig.class,
        SBWConfigModifier.class,

        // listeners
        GeneratorSplitterListener.class,

        // visuals
        GameLobbySidebarManager.class,
        GameSidebarManager.class,
        LobbySidebarManager.class,

        // services
        HealthIndicatorService.class
})
public class SBA extends PluginContainer implements SBWAddonAPI {
    private LoggerWrapper logger;
    private final List<Listener> registeredListeners = new ArrayList<>();

    @OnEnable
    public void onEnable(LoggerWrapper logger) {
        this.logger = logger;
    }

    @OnPostEnable
    public void onPostEnable() {
        if (Bukkit.getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            Bukkit.getLogger().warning("Could not find Screaming-BedWars plugin!, " +
                    "make sure you have the right one installed, and it's enabled properly!");
            Bukkit.getPluginManager().disablePlugin(as(JavaPlugin.class));
            return;
        }

        // init SLib v1 InventoryListener to delegate old SLib actions to new one.
        InventoryListener.init(asJavaPlugin());

        logger.trace("SBA v{} has been enabled!", getPluginDescription().getVersion());
        registerAPI();
    }

    @OnPreDisable
    public void onPreDisable() {
        Tasker.cancelAll();
        EventManager.getDefaultEventManager().unregisterAll();
        EventManager.getDefaultEventManager().destroy();
    }

    public void registerListener(@NotNull Listener listener) {
        if (registeredListeners.contains(listener)) {
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(listener, as(JavaPlugin.class));
        logger.trace("Registered listener: {}", listener.getClass().getSimpleName());
    }

    public void unregisterListener(@NotNull Listener listener) {
        if (!registeredListeners.contains(listener)) {
            return;
        }
        HandlerList.unregisterAll(listener);
        registeredListeners.remove(listener);
        logger.trace("Unregistered listener: {}", listener.getClass().getSimpleName());
    }

    @NotNull
    public List<Listener> getRegisteredListeners() {
        return List.copyOf(registeredListeners);
    }

    private void registerAPI() {
        Bukkit.getServer().getServicesManager().register(SBWAddonAPI.class, this, asJavaPlugin(), ServicePriority.Normal);
        logger.trace("API has been registered!");
    }

    public boolean isSnapshot() {
        return getPluginDescription().getVersion().toLowerCase().contains("version");
    }
}
