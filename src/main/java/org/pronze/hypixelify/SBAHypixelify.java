package org.pronze.hypixelify;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.data.GameStorage;
import org.pronze.hypixelify.inventories.CustomShop;
import org.pronze.hypixelify.inventories.GamesInventory;
import org.pronze.hypixelify.listener.LobbyScoreboard;
import org.pronze.hypixelify.manager.ArenaManager;
import org.pronze.hypixelify.manager.CommandManager;
import org.pronze.hypixelify.manager.ListenerManager;
import org.pronze.hypixelify.manager.PartyManager;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.placeholderapi.SBAExpansion;
import org.pronze.hypixelify.service.PlayerWrapperService;
import org.pronze.hypixelify.utils.SBAUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;

import java.util.Objects;

public class SBAHypixelify extends JavaPlugin implements Listener {

    private static SBAHypixelify plugin;
    public boolean papiEnabled;
    private CustomShop shop;
    private String version;
    private PlayerWrapperService playerWrapperService;
    private PartyManager partyManager;
    private Configurator configurator;
    private ArenaManager arenaManager;
    private GamesInventory gamesInventory;
    private Messages messages;
    private ListenerManager listenerManager;
    private boolean isProtocolLib;
    private boolean debug = false;
    private boolean mainLobby;
    private boolean partyEnabled;
    private CommandManager commandManager;


    public static GameStorage getGameStorage(Game game) {
        if (getArenaManager().getArenas().containsKey(game.getName()))
            return getArenaManager().getArenas().get(game.getName()).getStorage();

        return null;
    }

    public static boolean arenaExists(String arenaName) {
        return getArenaManager().getArenas().containsKey(arenaName);
    }

    public static Arena getArena(String arenaName) {
        return getArenaManager().getArenas().get(arenaName);
    }

    public static boolean LobbyBoardEnabled() {
        return plugin.mainLobby;
    }

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }

    public static boolean isProtocolLib() {
        return plugin.isProtocolLib;
    }

    public static org.pronze.hypixelify.api.party.PartyManager getPartyManager() {
        return plugin.partyManager;
    }

    public static SBAHypixelify getInstance() {
        return plugin;
    }

    public static String getVersion() {
        return plugin.version;
    }

    public static GamesInventory getGamesInventory() {
        return plugin.gamesInventory;
    }

    public static PlayerWrapperService getWrapperService() {
        return plugin.playerWrapperService;
    }

    public static boolean isPapiEnabled() {
        return plugin.papiEnabled;
    }

    public static ArenaManager getArenaManager() {
        return plugin.arenaManager;
    }

    public static boolean isUpgraded() {
        return !Objects.requireNonNull(getConfigurator()
                .config.getString("version")).contains(SBAHypixelify.getVersion());
    }

    public static void debug(String message) {
        if (!plugin.debug || message == null) return;
        Bukkit.getLogger().info("§c[DEBUG]: §f" + message);
    }

    public static void checkForUpdates() {
        if (!Main.isLegacy()) {
            new UpdateChecker(plugin, 79505).getVersion(version -> {
                if (plugin.getDescription().getVersion().contains(version)) {
                    Bukkit.getLogger().info("You are using the latest version of the addon");
                } else {
                    Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE.");
                }
            });
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        version = this.getDescription().getVersion();
        arenaManager = new ArenaManager();

        checkForUpdates();

        configurator = new Configurator(this);
        configurator.loadDefaults();

        partyEnabled = configurator.config.getBoolean("party.enabled", true);
        debug = configurator.config.getBoolean("debug.enabled", false);

        mainLobby = SBAHypixelify.getConfigurator().config.getBoolean("main-lobby.enabled", false);
        isProtocolLib = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null;


        listenerManager = new ListenerManager();
        listenerManager.registerAll(this);

        messages = new Messages();
        messages.loadConfig();

        InventoryListener.init(this);
        shop = new CustomShop();


        if (configurator.config.getBoolean("games-inventory.enabled", true))
            gamesInventory = new GamesInventory();

        if (playerWrapperService == null)
            playerWrapperService = new PlayerWrapperService();

        if (configurator.config.getBoolean("party.enabled", true)) {
            partyManager = new PartyManager();
        }

        if (!Main.isLegacy())
            Bukkit.getPluginManager().registerEvents(new LobbyScoreboard(), this);


        if (!Objects.requireNonNull(configurator.config.getString("version")).contains(version)) {
            getLogger().info("§a[SBAHypixelify]: Addon has been updated, join the server to make changes");
        }

        //Do changes for legacy support.
        changeBedWarsConfig();

        papiEnabled = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        Bukkit.getPluginManager().registerEvents(this, this);
        commandManager = new CommandManager();
        commandManager.registerAll(this);


        try {
            if (papiEnabled) {
                new SBAExpansion().register();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
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

            if (Main.getConfigurator().config.getBoolean("scoreboard.enable", true)) {
                Main.getConfigurator().config.set("scoreboard.enable", false);
                Main.getConfigurator().config.set("lobby-scoreboard.enabled", false);
                doneChanges = true;
            }

            if (doneChanges) {
                Bukkit.getLogger().info("[SBAHypixelify]: Making legacy changes");
                Main.getConfigurator().saveConfig();
                Bukkit.getServer().getPluginManager().disablePlugin(this);
                Bukkit.getServer().getPluginManager().enablePlugin(this);
            }

        }
    }

    @Override
    public void onDisable() {
        if (SBAHypixelify.isProtocolLib() && !Bukkit.getOnlinePlayers().isEmpty()) {
            Bukkit.getOnlinePlayers().forEach(SBAUtil::removeScoreboardObjective);
        }

        Bukkit.getLogger().info("Unregistering listeners....");
        if (listenerManager != null)
            listenerManager.unregisterAll();

        Bukkit.getLogger().info("Cancelling current tasks....");
        messages = null;
        arenaManager = null;
        if (gamesInventory != null)
            gamesInventory.destroy();


        shop.destroy();
        this.getServer().getScheduler().cancelTasks(plugin);
        this.getServer().getServicesManager().unregisterAll(plugin);
        if (plugin.isEnabled()) {
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final String plugin = event.getPlugin().getName();
        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();

        if (plugin.equalsIgnoreCase("BedWars")) {
            pluginManager.disablePlugin(SBAHypixelify.getInstance());
            pluginManager.enablePlugin(SBAHypixelify.getInstance());
        }
    }


}


