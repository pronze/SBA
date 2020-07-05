package org.pronze.hypixelify;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.pronze.hypixelify.commands.BWACommand;
import org.pronze.hypixelify.listener.*;
import org.pronze.hypixelify.manager.ArenaManager;
import org.screamingsandals.bedwars.lib.sgui.listeners.*;
import org.pronze.hypixelify.inventories.customShop;


import org.bukkit.ChatColor;

import java.util.Objects;

public class Hypixelify extends JavaPlugin implements Listener {

    private static Hypixelify plugin;
    private Configurator configurator;
    private ArenaManager arenamanager;

    public void onEnable() {
        plugin = this;

        if(this.getServer().getPluginManager().getPlugin("Citizens") == null)
        {
            Bukkit.getLogger().warning("failed to initalize plugin make sure citizens api is installed");
            onDisable();
        }
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

        Bukkit.getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Bukkit.getLogger().info(ChatColor.GOLD + "[SBAHypixelify]: Enabled Bedwars Addon v" + Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("SBAHypixelify")).getDescription().getVersion());
        Bukkit.getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        new PlayerListener(this);
        InventoryListener.init(this);
        new customShop();
        new Shop();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new LobbyScoreboard(),this);
        getCommand("bwaddon").setExecutor(new BWACommand());

        if(!configurator.config.getString("version").contains(getVersion())){
            Bukkit.getLogger().info(ChatColor.GREEN + "[SBAHypixelify]: Addon has been updated, join the server to make changes");
        }
    }

    public void onDisable(){
        if(plugin.isEnabled()){
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


    public static String getVersion(){
        return Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("SBAHypixelify")).getDescription().getVersion();
    }

    public ArenaManager getArenaManager() {
        return this.arenamanager;
    }
    
}


