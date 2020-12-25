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
import io.pronze.hypixelify.specials.listener.DragonListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import io.pronze.hypixelify.game.Arena;
import io.pronze.hypixelify.game.GameStorage;
import io.pronze.hypixelify.placeholderapi.SBAExpansion;
import io.pronze.hypixelify.service.PlayerWrapperService;
import io.pronze.hypixelify.utils.SBAUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.nms.utils.ClassStorage;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import java.util.Arrays;
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
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (!Main.getVersion().contains("0.3.")) {
            showErrorMessage("You need at least a minimum of 0.3.0 version of Screaming-BedWars to run SBAHypixelify!",
                    "Get the latest version from here: https://ci.screamingsandals.org/job/BedWars-0.x.x/");
            return;
        }

        if (!ClassStorage.IS_SPIGOT_SERVER) {
            showErrorMessage("Did not detect spigot",
                    "Make sure you have spigot installed to run this plugin");
            return;
        }

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9, please upgrade!");
            return;
        }

        plugin = this;
        version = this.getDescription().getVersion();
        isSnapshot = version.toLowerCase().contains("snapshot");

        if (!isSnapshot) {
            UpdateChecker.run(this, 79505);
        }

        configurator = new Configurator(this);
        configurator.loadDefaults();

        playerWrapperService = new PlayerWrapperService();
        debug = configurator.config.getBoolean("debug.enabled", false);

        InventoryListener.init(this);
        shop = new CustomShop();
        gamesInventory = new GamesInventory();

        partyManager = new io.pronze.hypixelify.manager.PartyManager();

        registerCommand("party", new PartyCommand());
        registerCommand("shout", new ShoutCommand());
        registerCommand("bwaddon", new BWACommand());

        final var pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(new BedwarsListener(), this);
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new PartyListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new LobbyScoreboard(), this);
        pluginManager.registerEvents(new DragonListener(), this);
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
            RotatingGenerators.format = SBAHypixelify.getConfigurator().getStringList("floating-generator.holo-text");
            SBAUtil.destroySpawnerArmorStandEntities();
            RotatingGenerators.scheduleTask();
        }

        getServer().getServicesManager().register(SBAHypixelifyAPI.class, this, this, ServicePriority.Normal);
        getLogger().info("Plugin has loaded");
    }

    protected void showErrorMessage(String... messages) {
        getLogger().severe("======PLUGIN ERROR===========");
        getLogger().severe("Plugin: SBAHypixelify is being disabled for the following error:");
        Arrays.stream(messages)
                .filter(Objects::nonNull)
                .forEach(getLogger()::severe);
        getLogger().severe("=============================");
        getServer().getPluginManager().disablePlugin(this);
    }

    public void registerCommand(String commandName, CommandExecutor executor) {
        var pluginCommand = getCommand(commandName);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(executor);
        }
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
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(SBAUtil::removeScoreboardObjective);
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


