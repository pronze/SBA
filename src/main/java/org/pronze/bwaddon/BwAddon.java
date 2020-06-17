package org.pronze.bwaddon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.pronze.bwaddon.commands.BWACommand;
import org.pronze.bwaddon.listener.*;
import org.screamingsandals.bedwars.lib.sgui.listeners.*;
import org.pronze.bwaddon.inventories.customShop;


import org.bukkit.ChatColor;

public class BwAddon extends JavaPlugin {

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
        getCommand("bwaddon").setExecutor(new BWACommand());

    }

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }


    @Override
    public void onDisable() { this.getServer().getServicesManager().unregisterAll(this); }

    public static BwAddon getInstance() {
        return plugin;
    }

}


