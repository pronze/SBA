package io.pronze.hypixelify;

import io.pronze.hypixelify.api.SBAHypixelifyAPI;
import io.pronze.hypixelify.api.party.PartyManager;
import io.pronze.hypixelify.api.wrapper.PlayerWrapper;
import io.pronze.hypixelify.commands.BWACommand;
import io.pronze.hypixelify.commands.PartyCommand;
import io.pronze.hypixelify.commands.ShoutCommand;
import io.pronze.hypixelify.inventories.CustomShop;
import io.pronze.hypixelify.inventories.GamesInventory;
import io.pronze.hypixelify.listener.*;
import io.pronze.hypixelify.game.RotatingGenerators;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import io.pronze.hypixelify.game.Arena;
import io.pronze.hypixelify.game.GameStorage;
import io.pronze.hypixelify.message.Messages;
import io.pronze.hypixelify.placeholderapi.SBAExpansion;
import io.pronze.hypixelify.service.PlayerWrapperService;
import io.pronze.hypixelify.utils.SBAUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SBAHypixelify extends JavaPlugin implements SBAHypixelifyAPI {

    private static SBAHypixelify plugin;

    private CustomShop shop;

    private String version;

    private PlayerWrapperService playerWrapperService;

    private io.pronze.hypixelify.manager.PartyManager partyManager;

    private Configurator configurator;

    private GamesInventory gamesInventory;

    private Messages messages;

    private boolean debug = false;
    private boolean isSnapshot;

    private final Map<String, Arena> arenas = new HashMap<>();


    public static GameStorage getGamestorage(Game game) {
        if (plugin.arenas.containsKey(game.getName()))
            return plugin.arenas.get(game.getName()).getStorage();

        return null;
    }

    @Override
    public GameStorage getGameStorage(Game game){
        return SBAHypixelify.getGamestorage(game);
    }

    @Override
    public PlayerWrapper getPlayerWrapper(Player player) {
        return playerWrapperService.getWrapper(player);
    }

    public static Arena getArena(String arenaName) { return plugin.arenas.get(arenaName); }

    public static void addArena(Arena arena) { plugin.arenas.put(arena.getGame().getName(), arena); }

    public static void removeArena(String arenaName) { plugin.arenas.remove(arenaName); }

    public static Map<String, Arena> getArenas() {
        return plugin.arenas;
    }

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }

    public static boolean isProtocolLib() {
        return plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib");
    }

    public static PartyManager getPartyManager() {
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


    public static boolean isUpgraded() {
        return !Objects.requireNonNull(getConfigurator()
                .config.getString("version")).contains(SBAHypixelify.getVersion());
    }

    public static void debug(String message) {
        if (!plugin.debug || message == null) return;
        Bukkit.getLogger().info("§c[DEBUG]: §f" + message);
    }

    @Override
    public void onEnable() {
        if (getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            getLogger().severe("Could not find Screaming BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;
        version = this.getDescription().getVersion();
        isSnapshot = version.toLowerCase().contains("snapshot");

        playerWrapperService = new PlayerWrapperService();

        if (!isSnapshot) {
            UpdateChecker.run(this, 79505);
        }

        configurator = new Configurator(this);
        configurator.loadDefaults();

        debug = configurator.config.getBoolean("debug.enabled", false);

        messages = new Messages();
        messages.loadConfig();

        InventoryListener.init(this);
        shop = new CustomShop();
        gamesInventory = new GamesInventory();

        partyManager = new io.pronze.hypixelify.manager.PartyManager();

        getCommand("party").setExecutor(new PartyCommand());
        getCommand("shout").setExecutor(new ShoutCommand());
        getCommand("bwaddon").setExecutor(new BWACommand());

        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(new BedwarsListener(), this);
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new PartyListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new LobbyScoreboard(), this);
        if (configurator.config.getBoolean("main-lobby.enabled", false))
            pluginManager.registerEvents(new LobbyBoard(), this);

        pluginManager.registerEvents(gamesInventory, this);
        pluginManager.registerEvents(shop, this);

        //Do changes for legacy support.
        changeBedWarsConfig();



        try {
            if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
                new SBAExpansion().register();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if(configurator.config.getBoolean("floating-generator.enabled", true)) {
            if(Main.getConfigurator().config.getBoolean("spawner-holograms", true)){
                Main.getConfigurator().config.set("spawner-holograms", false);
                Main.getConfigurator().saveConfig();
                pluginManager.disablePlugin(Main.getInstance());
                pluginManager.enablePlugin(Main.getInstance());
                return;
            }
            SBAUtil.destroySpawnerArmorStandEntities();
            RotatingGenerators.scheduleTask();
        }

        getServer().getServicesManager().register(SBAHypixelifyAPI.class, this, this, ServicePriority.Normal);

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
                Bukkit.getServer().getPluginManager().disablePlugin(this);
                Bukkit.getServer().getPluginManager().enablePlugin(this);
            }

        }
    }

    @Override
    public void onDisable() {
        if (SBAHypixelify.isProtocolLib()) {
            Bukkit.getOnlinePlayers().forEach(SBAUtil::removeScoreboardObjective);
        }

        RotatingGenerators.destroy(RotatingGenerators.cache);

        if (gamesInventory != null)
            gamesInventory.destroy();

        if(shop != null)
            shop.destroy();

        getLogger().info("Cancelling current tasks....");
        this.getServer().getScheduler().cancelTasks(plugin);
        this.getServer().getServicesManager().unregisterAll(plugin);

        arenas.clear();
    }




}


