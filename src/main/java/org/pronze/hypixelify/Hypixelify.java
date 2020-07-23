package org.pronze.hypixelify;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.pronze.hypixelify.commands.BWACommand;
import org.pronze.hypixelify.inventories.*;
import org.pronze.hypixelify.listener.*;
import org.pronze.hypixelify.manager.ArenaManager;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.sgui.listeners.*;


import org.bukkit.ChatColor;

import java.util.Objects;

public class Hypixelify extends JavaPlugin implements Listener {

    private static Hypixelify plugin;
    private static customShop shop;
    private Configurator configurator;
    private ArenaManager arenamanager;
    private GamesInventory gamesInventory;
    //public HashMap<UUID, PlayerDatabase> playerData = new HashMap<>();
   //  public PartyTask partyTask;
   // public PartyManager partyManager;

    public GamesInventory getGamesInventory(){
        return gamesInventory;
    }

    public void onEnable() {
        plugin = this;
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
        Bukkit.getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Bukkit.getLogger().info("");

        new PlayerListener();
        InventoryListener.init(this);

        shop = new customShop();
        gamesInventory = new GamesInventory();
        if(this.getServer().getPluginManager().getPlugin("Citizens") == null ||
        !Hypixelify.getConfigurator().config.getBoolean("citizens-shop", true))
        {
           Bukkit.getLogger().warning("Failed to initalize Citizens shop reverting to normal shops...");
           if(Main.getConfigurator().config.getBoolean("shop.citizens-enabled", false)) {
               Main.getConfigurator().config.set("shop.citizens-enabled", false);
               Main.getConfigurator().saveConfig();
               Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
               Bukkit.getServer().getPluginManager().enablePlugin(Main.getInstance());
           }
        }
       else{
            new Shop();
        //    if(playerData == null){
       //         playerData = new HashMap<>();
        }



      // partyManager = new PartyManager();
     //  partyTask = new PartyTask();

       Bukkit.getPluginManager().registerEvents(this, this);
       Bukkit.getPluginManager().registerEvents(new LobbyScoreboard(),this);
       getCommand("bwaddon").setExecutor(new BWACommand());
     //  getCommand("party").setExecutor(new PartyCommand());

        if(!configurator.config.getString("version").contains(getVersion())){
            Bukkit.getLogger().info(ChatColor.GREEN + "[SBAHypixelify]: Addon has been updated, join the server to make changes");
        }

       //for(Player player : Bukkit.getOnlinePlayers()){
       //    if(player == null) continue;
       //    if(playerData.get(player.getUniqueId()) == null){
       //        playerData.put(player.getUniqueId(), new PlayerDatabase(player));
       //    }
       //}
    }

    public void onDisable(){
        if(plugin.isEnabled()){
          //partyTask.cancel();
          //partyManager.parties.clear();
          //partyManager.parties = null;
          //partyManager = null;
          //playerData.clear();
          //playerData = null;
            plugin.getPluginLoader().disablePlugin(plugin);
            this.getServer().getScheduler().cancelTasks(plugin);
            this.getServer().getServicesManager().unregisterAll(plugin);
        }
    }

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }

    public static Hypixelify getInstance() {
        return plugin;
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        String plugin = event.getPlugin().getName();
        if (plugin.equalsIgnoreCase("BedWars")) {
            Bukkit.getServer().getPluginManager().disablePlugin(Hypixelify.getInstance());
            Bukkit.getServer().getPluginManager().enablePlugin(Hypixelify.getInstance());
        }
    }

    public static customShop getShop(){
        return shop;
    }

    public static String getVersion(){
        return Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("SBAHypixelify")).getDescription().getVersion();
    }

    public ArenaManager getArenaManager() {
        return this.arenamanager;
    }
    
}


