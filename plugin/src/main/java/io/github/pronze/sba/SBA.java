package io.github.pronze.sba;

import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.IConfigurator;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.game.IGameStorage;
import io.github.pronze.sba.game.tasks.GameTaskManager;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.inventories.SBAStoreInventory;
import io.github.pronze.sba.inventories.SBAUpgradeStoreInventory;
import io.github.pronze.sba.lang.ILanguageService;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.listener.*;
import io.github.pronze.sba.manager.IArenaManager;
import io.github.pronze.sba.manager.IPartyManager;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.placeholderapi.SBAExpansion;
import io.github.pronze.sba.service.*;
import io.github.pronze.sba.specials.listener.BridgeEggListener;
import io.github.pronze.sba.specials.listener.PopupTowerListener;
import io.github.pronze.sba.utils.DateUtils;
import io.github.pronze.sba.utils.FirstStartConfigReplacer;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.visuals.LobbyScoreboardManager;
import io.github.pronze.sba.visuals.MainLobbyVisualsManager;
import io.github.pronze.sba.wrapper.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.healthindicator.HealthIndicatorManager;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.npc.NPCManager;
import org.screamingsandals.lib.packet.PacketMapper;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import pronze.lib.scoreboards.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.pronze.sba.utils.MessageUtils.showErrorMessage;

@Plugin(
        id = "SBA",
        authors = {"pronze"},
        loadTime = Plugin.LoadTime.POSTWORLD,
        version = "1.5.6-SNAPSHOT"
)
@PluginDependencies(platform = PlatformType.BUKKIT, dependencies = {
        "BedWars"
}, softDependencies =
        "PlaceholderAPI"
)
@Init(services = {
        Tasker.class,
        PacketMapper.class,
        HologramManager.class,
        HealthIndicatorManager.class,
        SimpleInventoriesCore.class,
        NPCManager.class,
        //EventManager.class,
        UpdateChecker.class,
        SBAConfig.class,
        Logger.class,
        LanguageService.class,
        CommandManager.class,
        ArenaManager.class,
        PartyManager.class,
        GameTaskManager.class,
        SBAStoreInventory.class,
        SBAUpgradeStoreInventory.class,
        GamesInventory.class,
        PlayerWrapperService.class,
        GamesInventoryService.class,
        HealthIndicatorService.class,
        PlayerInvisibilityMaintainerService.class,
        DateUtils.class,
        BedWarsListener.class,
        GameChatListener.class,
        PartyListener.class,
        PlayerListener.class,
        GeneratorSplitterListener.class,
        ExplosionVelocityControlListener.class,
        LobbyScoreboardManager.class,
        MainLobbyVisualsManager.class,
        DynamicSpawnerLimiterService.class,
        BedwarsCustomMessageModifierListener.class,
        BridgeEggListener.class,
        PopupTowerListener.class,
        NPCStoreService.class,
        FirstStartConfigReplacer.class,
})

public class SBA extends PluginContainer implements AddonAPI {
    private static SBA instance;
    private final List<Listener> registeredListeners = new ArrayList<>();

    public static SBA getInstance() {
        return instance;
    }

    public static JavaPlugin getPluginInstance() {
        return instance.getPluginDescription().as(JavaPlugin.class);
    }

    @Override
    public void load() {
        instance = this;
    }

    @Override
    public void enable() {
        instance = this;
        ScoreboardManager.init(getPluginInstance());
        // register API
        Bukkit.getServer().getServicesManager().register(AddonAPI.class, this, getPluginInstance(), ServicePriority.Normal);
    }

    @OnPostEnable
    public void postEnabled() {
        if (Bukkit.getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9.4, please upgrade!");
            return;
        }

        InventoryListener.init(getPluginInstance());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SBAExpansion().register();
        }
        getLogger().info("Plugin has loaded");
    }

    public void registerListener(@NotNull Listener listener) {
        if (registeredListeners.contains(listener)) {
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(listener, getPluginInstance());
        Logger.trace("Registered listener: {}", listener.getClass().getSimpleName());
    }

    public void unregisterListener(@NotNull Listener listener) {
        if (!registeredListeners.contains(listener)) {
            return;
        }
        HandlerList.unregisterAll(listener);
        registeredListeners.remove(listener);
        Logger.trace("Unregistered listener: {}", listener.getClass().getSimpleName());
    }

    public List<Listener> getRegisteredListeners() {
        return List.copyOf(registeredListeners);
    }

    @Override
    public void disable() {

    }

    @Override
    public Optional<IGameStorage> getGameStorage(Game game) {
        return ArenaManager.getInstance().getGameStorage(game.getName());
    }

    @Override
    public PlayerWrapper getPlayerWrapper(Player player) {
        return PlayerWrapperService.getInstance().get(player).orElse(null);
    }

    @Override
    public boolean isDebug() {
        return SBAConfig.getInstance().getBoolean("debug.enabled", false);
    }

    @Override
    public boolean isSnapshot() {
        return getVersion().contains("SNAPSHOT");
    }

    @Override
    public String getVersion() {
        return getPluginDescription().getVersion();
    }

    @Override
    public IArenaManager getArenaManager() {
        return ArenaManager.getInstance();
    }

    @Override
    public IPartyManager getPartyManager() {
        return PartyManager.getInstance();
    }

    @Override
    public WrapperService<Player, PlayerWrapper> getPlayerWrapperService() {
        return PlayerWrapperService.getInstance();
    }

    @Override
    public IConfigurator getConfigurator() {
        return SBAConfig.getInstance();
    }

    @Override
    public boolean isPendingUpgrade() {
        return !getVersion().equalsIgnoreCase(SBAConfig.getInstance().node("version").getString());
    }

    @Override
    public ILanguageService getLanguageService() {
        return LanguageService.getInstance();
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return instance.getPluginDescription().as(JavaPlugin.class);
    }
}


