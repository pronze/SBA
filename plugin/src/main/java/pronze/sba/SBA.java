package pronze.sba;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.event.EventManager;
import pronze.sba.config.IConfigurator;
import pronze.sba.game.GameStorage;
import pronze.sba.lang.ILanguageService;
import pronze.sba.manager.IArenaManager;
import pronze.sba.manager.IPartyManager;
import pronze.sba.service.WrapperService;
import pronze.sba.wrapper.PlayerWrapper;
import pronze.sba.commands.CommandManager;
import pronze.sba.config.SBAConfig;
import pronze.sba.game.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import pronze.sba.inventories.GamesInventory;
import pronze.sba.inventories.SBAStoreInventory;
import pronze.sba.lib.lang.LanguageService;
import pronze.sba.listener.BedWarsListener;
import pronze.sba.listener.GameChatListener;
import pronze.sba.listener.PartyListener;
import pronze.sba.listener.PlayerListener;
import pronze.sba.party.PartyManager;
import pronze.sba.placeholderapi.SBAExpansion;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;
import pronze.sba.service.HealthIndicatorService;
import pronze.sba.service.PlayerInvisibilityMaintainerService;
import pronze.sba.service.PlayerWrapperService;
import pronze.sba.utils.DateUtils;
import pronze.sba.utils.Logger;
import pronze.sba.visuals.LobbyScoreboardManager;
import pronze.sba.visuals.MainLobbyVisualsManager;
import pronze.lib.scoreboards.ScoreboardManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static pronze.sba.utils.MessageUtils.showErrorMessage;

@Plugin(
        id = "SBA",
        authors = { "pronze" },
        loadTime = Plugin.LoadTime.POSTWORLD,
        version = "1.5.0-SNAPSHOT"
)
@PluginDependencies(platform = PlatformType.BUKKIT, dependencies = {
        "BedWars"
}, softDependencies =
        "PlaceholderAPI"
)
@Init(services = {
        EventManager.class,
        UpdateChecker.class,
        SBAConfig.class,
        Logger.class,
        LanguageService.class,
        CommandManager.class,
        ArenaManager.class,
        PartyManager.class,
        SBAStoreInventory.class,
        GamesInventory.class,
        PlayerWrapperService.class,
        HealthIndicatorService.class,
        PlayerInvisibilityMaintainerService.class,
        DateUtils.class,
        BedWarsListener.class,
        GameChatListener.class,
        PartyListener.class,
        PlayerListener.class,
        LobbyScoreboardManager.class,
        MainLobbyVisualsManager.class
})

public class SBA extends PluginContainer implements AddonAPI {
    private static SBA instance;

    public static SBA getInstance() {
        return instance;
    }

    public static JavaPlugin getPluginInstance() {
        return instance.getPluginDescription().as(JavaPlugin.class);
    }

    private final List<Listener> registeredListeners = new ArrayList<>();

    @Override
    public void load() {
        instance = this;
    }

    @Override
    public void enable() {
        instance = this;

        if (Bukkit.getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (!Main.getVersion().contains("0.2.15")) {
            showErrorMessage("You need ScreamingBedWars v0.2.15 to run SBA v1.5.0",
                    "Get the latest version from here: https://www.spigotmc.org/resources/screaming-bedwars-1-9-1-16.63714/");
            return;
        }

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9, please upgrade!");
            return;
        }

        InventoryListener.init(getPluginInstance());

        //Do changes for legacy support.
        enableLegacySupport();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SBAExpansion().register();
        }

        ScoreboardManager.init(getPluginInstance());
        Bukkit.getServer().getServicesManager().register(AddonAPI.class, this, getPluginInstance(), ServicePriority.Normal);
        getLogger().info("Plugin has loaded");
    }

    private static final Map<Map.Entry<String, String>, String> replacementMap = new HashMap<>() {
        {
            put(Map.entry("items.leavegame", "RED_BED"), "BED");
            put(Map.entry("items.shopcosmetic", "GRAY_STAINED_GLASS_PANE"), "STAINED_GLASS_PANE");
        }
    };

    public void enableLegacySupport() {
        //Do changes for legacy support.
        if (Main.isLegacy()) {
            final var doneChanges =  new AtomicBoolean(false);

            replacementMap.forEach((key, value) -> {
                if (Main.getConfigurator().config.getString(key.getKey(), key.getValue()).equalsIgnoreCase(key.getValue())) {
                    Main.getConfigurator().config.set(key.getKey(), value);
                    doneChanges.set(true);
                }
            });

            if (doneChanges.get()) {
                getLogger().info("[SBA]: Making legacy changes");
                Main.getConfigurator().saveConfig();
                Bukkit.getServer().getPluginManager().disablePlugin(getPluginInstance());
                Bukkit.getServer().getPluginManager().enablePlugin(getPluginInstance());
            }

        }
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
        getLogger().info("Cancelling current tasks....");
        Bukkit.getServer().getScheduler().cancelTasks(getPluginInstance());
        Bukkit.getServer().getServicesManager().unregisterAll(getPluginInstance());
    }

    @Override
    public Optional<GameStorage> getGameStorage(Game game) {
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
        return getVersion().contains("snapshot");
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
        return !getVersion().contains(Objects.requireNonNull(SBAConfig.getInstance().node("version").getString()));
    }

    @Override
    public ILanguageService getLanguageService() {
        return LanguageService.getInstance();
    }
}


