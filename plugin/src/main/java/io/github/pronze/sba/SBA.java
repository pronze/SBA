package io.github.pronze.sba;

import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.IConfigurator;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.fix.BaseFix;
import io.github.pronze.sba.fix.BungeecordNPC;
import io.github.pronze.sba.fix.MagmaFix;
import io.github.pronze.sba.fix.MohistFix;
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
import io.github.pronze.sba.specials.SpawnerProtection;
import io.github.pronze.sba.specials.listener.BridgeEggListener;
import io.github.pronze.sba.specials.listener.PopupTowerListener;
import io.github.pronze.sba.utils.DateUtils;
import io.github.pronze.sba.utils.FirstStartConfigReplacer;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.Logger.Level;
import io.github.pronze.sba.visuals.LobbyScoreboardManager;
import io.github.pronze.sba.visuals.MainLobbyVisualsManager;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import io.github.pronze.sba.VersionInfo;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.bstats.bukkit.Metrics;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.healthindicator.HealthIndicatorManager;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.npc.NPCManager;
import org.screamingsandals.lib.packet.PacketMapper;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.sidebar.SidebarManager;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import io.github.pronze.lib.pronzelib.scoreboards.ScoreboardManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.github.pronze.sba.utils.MessageUtils.showErrorMessage;

@Plugin(id = "SBA", authors = { "pronze",
        "boiscljo" }, loadTime = Plugin.LoadTime.POSTWORLD, version = VersionInfo.VERSION)
@PluginDependencies(platform = PlatformType.BUKKIT, dependencies = {
        "BedWars"
}, softDependencies = { "PlaceholderAPI", "ViaVersion" })
@Init(services = {
        Logger.class,
        PacketMapper.class,
        HologramManager.class,
        HealthIndicatorManager.class,
        SimpleInventoriesCore.class,
        NPCManager.class,
        UpdateChecker.class,
        SBAConfig.class,
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
        PacketListener.class,
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
        GameModeListener.class,
        SpawnerProtection.class,
        SpawnerProtectionListener.class,
        SidebarManager.class
})
public class SBA extends PluginContainer implements AddonAPI {

    private static SBA instance;
    private List<BaseFix> fixs;
    public static SBA getInstance() {
        return instance;
    }

    private JavaPlugin cachedPluginInstance;
    private final List<Listener> registeredListeners = new ArrayList<>();
    private Metrics metrics;

    public static JavaPlugin getPluginInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("SBA has not yet been initialized!");
        }
        if (instance.cachedPluginInstance == null) {
            instance.cachedPluginInstance = (JavaPlugin) instance.getPluginDescription().as(JavaPlugin.class);
        }
        return instance.cachedPluginInstance;
    }

    @Override
    public void enable() {
        instance = this;
        cachedPluginInstance = instance.getPluginDescription().as(JavaPlugin.class);
        Logger.init(cachedPluginInstance);

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9.4, please upgrade!");
            Bukkit.getServer().getPluginManager().disablePlugin(getPluginInstance());
            return;
        }
        fixs = new ArrayList<>();
        fixs.add(BungeecordNPC.getInstance());
        fixs.add(new MohistFix());
        fixs.add(new MagmaFix());

        for (BaseFix fix : fixs) {
            fix.detect();
        }
        
        ScoreboardManager.init(cachedPluginInstance);

        int pluginId = 14804; // <-- Replace with the id of your plugin!
        metrics = new Metrics(cachedPluginInstance, pluginId);
    }

    @Override
    public void postEnable() {
        
        if (Bukkit.getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            Bukkit.getServer().getPluginManager().disablePlugin(getPluginInstance());
            return;
        }
        else
        {
            Logger.info("SBA initialized using Bedwars {}", BedwarsAPI.getInstance().getPluginVersion());
            if (!List.of("0.2.20", "0.2.21", "0.2.22", "0.2.23").stream()
                    .anyMatch(BedwarsAPI.getInstance().getPluginVersion()::equals)) {
                Logger.warn("SBA hasn't been tested on this version of Bedwars, use version 0.2.20 to 0.2.23. ");
            }
        }
        if (Reflect.has("com.mohistmc.MohistMC"))
        {
            Bukkit.getServer().getPluginManager().disablePlugin(getPluginInstance());
            return;
        }
        for (BaseFix fix : fixs) {
            fix.fix(SBAConfig.getInstance());
            if(fix.IsProblematic())
                fix.warn();
            if(fix.IsCritical())
            {
                Bukkit.getServer().getPluginManager().disablePlugin(getPluginInstance());
            }
        }
        InventoryListener.init(cachedPluginInstance);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Logger.trace("Registering SBAExpansion...");
            new SBAExpansion().register();
        }

        Logger.info("Plugin has finished loading!");
        registerAPI();
        Logger.info("SBA Initialized on JAVA {}", System.getProperty("java.version"));
        Logger.info("SBA Commit is on par with {}", VersionInfo.COMMIT);
        Logger.trace("API has been registered!");

        Logger.setMode(Level.WARNING);
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

    private void registerAPI() {
        if (Bukkit.getServer().getServicesManager().getRegistration(AddonAPI.class) == null) {
            Bukkit.getServer().getServicesManager().register(AddonAPI.class, this, cachedPluginInstance,
                    ServicePriority.Normal);
        }
    }

    @Override
    public void disable() {
        EventManager.getDefaultEventManager().unregisterAll();
        EventManager.getDefaultEventManager().destroy();
        Bukkit.getServer().getServicesManager().unregisterAll(getPluginInstance());
    }

    @Override
    public Optional<IGameStorage> getGameStorage(Game game) {
        return ArenaManager.getInstance().getGameStorage(game.getName());
    }

    @Override
    public SBAPlayerWrapper getPlayerWrapper(Player player) {
        return PlayerWrapperService.getInstance().get(player)
                .orElseGet(() -> PlayerMapper.wrapPlayer(player).as(SBAPlayerWrapper.class));
    }

    @Override
    public boolean isDebug() {
        return SBAConfig.getInstance().getBoolean("debug.enabled", false);
    }

    @Override
    public boolean isSnapshot() {
        return getVersion().contains("SNAPSHOT") || getVersion().contains("dev");
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
    public WrapperService<Player, SBAPlayerWrapper> getPlayerWrapperService() {
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

    public static org.bukkit.plugin.Plugin getBedwarsPlugin() {
        return Bukkit.getPluginManager().getPlugin("BedWars");
    }

    public boolean isPendingUpdate() {
        return UpdateChecker.getInstance().isPendingUpdate();
    }

    public void update(@NotNull CommandSender sender) {
        UpdateChecker.getInstance().update(sender);
    }
}
