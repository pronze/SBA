package pronze.hypixelify;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.event.EventManager;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import pronze.hypixelify.api.config.IConfigurator;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.lang.ILanguageService;
import pronze.hypixelify.api.manager.IArenaManager;
import pronze.hypixelify.api.manager.IPartyManager;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.game.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import pronze.hypixelify.inventories.GamesInventory;
import pronze.hypixelify.inventories.SBAStoreInventory;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.listener.BedWarsListener;
import pronze.hypixelify.listener.GameChatListener;
import pronze.hypixelify.listener.PartyListener;
import pronze.hypixelify.listener.PlayerListener;
import pronze.hypixelify.party.PartyManager;
import pronze.hypixelify.placeholderapi.SBAExpansion;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;
import pronze.hypixelify.service.HealthIndicatorService;
import pronze.hypixelify.service.PlayerInvisibilityMaintainerService;
import pronze.hypixelify.service.PlayerWrapperService;
import pronze.hypixelify.utils.DateUtils;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.visuals.LobbyScoreboardManager;
import pronze.hypixelify.visuals.MainLobbyVisualsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static pronze.hypixelify.utils.MessageUtils.showErrorMessage;

@Plugin(
        id = "SBAHypixelify",
        authors = {"pronze"},
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

public class SBAHypixelify extends PluginContainer implements SBAHypixelifyAPI {
    private static SBAHypixelify instance;

    public static SBAHypixelify getInstance() {
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
            showErrorMessage("You need ScreamingBedWars v0.2.15 to run SBAHypixelify v1.0",
                    "Get the latest version from here: https://www.spigotmc.org/resources/screaming-bedwars-1-9-1-16.63714/");
            return;
        }

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9, please upgrade!");
            return;
        }

        InventoryListener.init(getPluginInstance());

        //Do changes for legacy support.
        changeBedWarsConfig();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SBAExpansion().register();
        }

        Bukkit.getServer().getServicesManager().register(SBAHypixelifyAPI.class, this, getPluginInstance(), ServicePriority.Normal);
        getLogger().info("Plugin has loaded");
    }


    public void changeBedWarsConfig() {
        //Do changes for legacy support.
        if (Main.isLegacy()) {
            boolean doneChanges = false;
            if (Objects.requireNonNull(Main.getConfigurator()
                    .config.getString("items.leavegame")).equalsIgnoreCase("RED_BED")) {
                Main.getConfigurator().config.set("items.leavegame", "BED");
                doneChanges = true;
            }
            if (Objects.requireNonNull(Main.getConfigurator()
                    .config.getString("items.shopcosmetic")).equalsIgnoreCase("GRAY_STAINED_GLASS_PANE")) {
                Main.getConfigurator().config.set("items.shopcosmetic", "STAINED_GLASS_PANE");
                doneChanges = true;
            }

            if (Main.getConfigurator().config.getBoolean("scoreboard.enable", true)
            || Main.getConfigurator().config.getBoolean("lobby-scoreboard.enabled", true)) {
                Main.getConfigurator().config.set("scoreboard.enable", false);
                Main.getConfigurator().config.set("lobby-scoreboard.enabled", false);
                doneChanges = true;
            }

            if (doneChanges) {
                getLogger().info("[SBAHypixelify]: Making legacy changes");
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


