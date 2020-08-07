package org.pronze.hypixelify;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.pronze.hypixelify.commands.BWACommand;
import org.pronze.hypixelify.commands.PartyCommand;
import org.pronze.hypixelify.commands.ShoutCommand;
import org.pronze.hypixelify.manager.DatabaseManager;
import org.pronze.hypixelify.inventories.GamesInventory;
import org.pronze.hypixelify.inventories.CustomShop;
import org.pronze.hypixelify.listener.*;
import org.pronze.hypixelify.manager.ArenaManager;
import org.pronze.hypixelify.manager.PartyManager;
import org.pronze.hypixelify.message.Messages;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import java.util.Objects;

public class Hypixelify extends JavaPlugin implements Listener {

    private static Hypixelify plugin;
    private CustomShop shop;
    private String version;
  //  public HashMap<UUID, org.pronze.hypixelify.api.database.PlayerDatabase> playerData = new HashMap<>();
    private DatabaseManager databaseManager;

    private PartyManager partyManager;
    private Configurator configurator;
    private ArenaManager arenamanager;
    private GamesInventory gamesInventory;
    private Messages messages;
    private ListenerManager listenerManager;
    public boolean papiEnabled;

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }

    public static org.pronze.hypixelify.api.party.PartyManager getPartyManager(){
        return plugin.partyManager;
    }

    public static Hypixelify getInstance() {
        return plugin;
    }

    public static CustomShop getShop() {
        return plugin.shop;
    }

    public static String getVersion() {
        return plugin.version;
    }

    public static GamesInventory getGamesInventory() {
        return plugin.gamesInventory;
    }

    public static DatabaseManager getDatabaseManager(){
        return plugin.databaseManager;
    }
    public static boolean isPapiEnabled(){
        return plugin.papiEnabled;
    }

    public void onEnable() {
        plugin = this;
        version = this.getDescription().getVersion();
        arenamanager = new ArenaManager();

        new UpdateChecker(this, 79505).getVersion(version -> {
            if (this.getDescription().getVersion().contains(version)) {
                Bukkit.getLogger().info("[SBAHypixelify]: You are using the latest version of the addon");
            } else {
                Bukkit.getLogger().info(ChatColor.YELLOW + " " + ChatColor.BOLD + "[SBAHypixelify]: THERE IS A NEW UPDATE AVAILABLE.");
            }
        });

        configurator = new Configurator(this);
        configurator.loadDefaults();


        boolean hookedWithCitizens = this.getServer().getPluginManager().getPlugin("Citizens") != null;
        boolean isLegacy = Main.isLegacy();

        Bukkit.getLogger().info("");
        Bukkit.getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Bukkit.getLogger().info("<  _____ ______   ___   _   _                _             _  _   __                              >");
        Bukkit.getLogger().info("< /  ___|| ___ \\ / _ \\ | | | |              (_)           | |(_) / _|                             >");
        Bukkit.getLogger().info("< \\ `--. | |_/ // /_\\ \\| |_| | _   _  _ __   _ __  __ ___ | | _ | |_  _   _                       >");
        Bukkit.getLogger().info("<  `--. \\| ___ \\|  _  ||  _  || | | || '_ \\ | |\\ \\/ // _ \\| || ||  _|| | | |                      >");
        Bukkit.getLogger().info("< /\\__/ /| |_/ /| | | || | | || |_| || |_) || | >  <|  __/| || || |  | |_| |                      > ");
        Bukkit.getLogger().info("< \\____/ \\____/ \\_| |_/\\_| |_/ \\__, || .__/ |_|/_/\\_\\____||_||_||_|  \\__,  |                      >");
        Bukkit.getLogger().info("<                               __/ || |                               __/ |                      >");
        Bukkit.getLogger().info("<                              |___/ |_|                              |___/                       >");
        Bukkit.getLogger().info("<  ______  ______  ______  ______  ______  ______  ______  ______  ______  ______  ______  ______ >");
        Bukkit.getLogger().info("< |______||______||______||______||______||______||______||______||______||______||______||______|>");
        Bukkit.getLogger().info("<  ______  ______  ______  ______  ______  ______  ______  ______  ______  ______  ______  ______ >");
        Bukkit.getLogger().info("< |______||______||______||______||______||______||______||______||______||______||______||______|>");
        Bukkit.getLogger().info("<                                                                                                 >");
        Bukkit.getLogger().info("< Status: §fEnabled                                                                                 >");
        Bukkit.getLogger().info("< Version: §f{Version}                                                                                  >".replace("{Version}", this.getDescription().getVersion()));
        Bukkit.getLogger().info("< Build: §6Stable                                                                               §7    >");
        Bukkit.getLogger().info("< Hooked To Citizens: §atrue§7                                                                        >"
                .replace("true", String.valueOf(hookedWithCitizens)));
        Bukkit.getLogger().info("< Legacy Support: §atrue§7                                                                            >"
                .replace("true", String.valueOf(isLegacy)));
        Bukkit.getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Bukkit.getLogger().info("");

        listenerManager = new ListenerManager();

        if(messages == null) {
            messages = new Messages();
            messages.loadConfig();
        }
        InventoryListener.init(this);
        shop = new CustomShop();

        if (Hypixelify.getConfigurator().config.getBoolean("games-inventory.enabled", true))
            gamesInventory = new GamesInventory();

        if (this.getServer().getPluginManager().getPlugin("Citizens") == null ||
                !Hypixelify.getConfigurator().config.getBoolean("citizens-shop", true)) {

            Bukkit.getLogger().warning("Failed to initalize Citizens shop reverting to normal shops...");

            if (Main.getConfigurator().config.getBoolean("shop.citizens-enabled", false)
            || configurator.config.getBoolean("citizens-shop", true)) {

                configurator.config.set("citizens-shop", false);
                configurator.saveConfig();
                Main.getConfigurator().config.set("shop.citizens-enabled", false);
                Main.getConfigurator().saveConfig();
                Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
                Bukkit.getServer().getPluginManager().enablePlugin(Main.getInstance());
            }
        } else {
            if (!Main.getConfigurator().config.getBoolean("shop.citizens-enabled", false)) {
                Main.getConfigurator().config.set("shop.citizens-enabled", true);
                Main.getConfigurator().saveConfig();
                Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
                Bukkit.getServer().getPluginManager().enablePlugin(Main.getInstance());
            }
        }

        if (configurator.config.getBoolean("party.enabled", true)) {
            if (databaseManager == null)
                databaseManager = new DatabaseManager();

            partyManager = new PartyManager();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == null) continue;
                if (databaseManager.getDatabase(player) == null) {
                    databaseManager.createDatabase(player);
                }
            }
            Objects.requireNonNull(getCommand("party")).setExecutor(new PartyCommand());
        }

        if (!Main.isLegacy())
            Bukkit.getPluginManager().registerEvents(new LobbyScoreboard(), this);


        if (!configurator.config.getString("version").contains(version)) {
            Bukkit.getLogger().info(ChatColor.GREEN + "[SBAHypixelify]: Addon has been updated, join the server to make changes");
        }

        //Do changes for legacy support.
        if (Main.isLegacy()) {
            boolean doneChanges = false;
            if (Main.getConfigurator().config.getString("items.leavegame").equalsIgnoreCase("RED_BED")) {
                Main.getConfigurator().config.set("items.leavegame", "BED");
                doneChanges = true;
            }
            if (Main.getConfigurator().config.getString("items.shopcosmetic").equalsIgnoreCase("GRAY_STAINED_GLASS_PANE")) {
                Main.getConfigurator().config.set("items.shopcosmetic", "STAINED_GLASS_PANE");
                doneChanges = true;
            }

            if(Main.getConfigurator().config.getBoolean("scoreboard.enable", true)){
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

        papiEnabled = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("bwaddon")).setExecutor(new BWACommand());
        Objects.requireNonNull(getCommand("shout")).setExecutor(new ShoutCommand());
    }

    @Override
    public void onDisable() {
        if (Hypixelify.getConfigurator().config.getBoolean("party.enabled", true)) {
            Bukkit.getLogger().info("[SBAHypixelify]: Shutting down party tasks...");
            if (partyManager != null && partyManager.parties != null) {
                partyManager.parties.clear();
                partyManager.parties = null;
                partyManager = null;
            }

            if (databaseManager != null) {
                databaseManager.destroy();
                databaseManager = null;
            }
        }
        Bukkit.getLogger().info("[SBAHypixelify]: Unregistering listeners....");
        listenerManager.unregisterAll();
        listenerManager = null;
        Bukkit.getLogger().info("[SBAHypixelify]: Cancelling current tasks....");
        configurator = null;
        messages = null;
        arenamanager = null;
        gamesInventory.destroy();
        gamesInventory = null;
        shop = null;
        this.getServer().getScheduler().cancelTasks(plugin);
        this.getServer().getServicesManager().unregisterAll(plugin);
        if (plugin.isEnabled()) {
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        String plugin = event.getPlugin().getName();
        if (plugin.equalsIgnoreCase("BedWars")) {
            Bukkit.getServer().getPluginManager().disablePlugin(Hypixelify.getInstance());
            Bukkit.getServer().getPluginManager().enablePlugin(Hypixelify.getInstance());
        }
    }

    public ArenaManager getArenaManager() {
        return this.arenamanager;
    }

}


