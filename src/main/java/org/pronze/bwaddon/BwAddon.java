package org.pronze.bwaddon;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.pronze.bwaddon.commands.BWACommand;
import org.pronze.bwaddon.listener.*;
import org.screamingsandals.bedwars.lib.sgui.listeners.*;
import org.pronze.bwaddon.inventories.customShop;


import org.bukkit.ChatColor;

public class BwAddon extends JavaPlugin implements Listener {

    private static BwAddon plugin;
    private Configurator configurator;

    @Override
    public void onEnable() {
        plugin = this;

        new UpdateChecker(this, 79505).getVersion(version -> {
            if (this.getDescription().getVersion().contains(version)) {
                Bukkit.getLogger().info("[ScreamingBedwarsAddon] You are using the latest version of the addon");
            } else {
                Bukkit.getLogger().info(ChatColor.YELLOW + " " + ChatColor.BOLD + "[ScreamingBedwarsAddon]: THERE IS A NEW UPDATE AVAILABLE.");
            }
        });

        configurator = new Configurator(this);
        configurator.loadDefaults();

        Bukkit.getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Bukkit.getLogger().info(ChatColor.GOLD + "[BW ADDON]: Enabled Bedwars Addon v" + Bukkit.getServer().getPluginManager().getPlugin("ScreamingBedwarsAddon").getDescription().getVersion());
        Bukkit.getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        new PlayerListener(this);
        InventoryListener.init(this);
        new customShop();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("bwaddon").setExecutor(new BWACommand());

    }

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }

    public static BwAddon getInstance() {
        return plugin;
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        String plugin = event.getPlugin().getName();
        if (plugin.equalsIgnoreCase("BedWars")) {
            Bukkit.getServer().getPluginManager().disablePlugin(BwAddon.getInstance());
            Bukkit.getServer().getPluginManager().enablePlugin(BwAddon.getInstance());
        }
    }
}


