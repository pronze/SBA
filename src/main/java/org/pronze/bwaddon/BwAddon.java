package org.pronze.bwaddon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.pronze.bwaddon.listener.*;
import org.screamingsandals.simpleinventories.listeners.InventoryListener;
import org.pronze.bwaddon.inventories.customShop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;
import org.bukkit.ChatColor;

public class BwAddon extends JavaPlugin{
	
	private static BwAddon plugin;
	private Configurator configurator;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		
		 new UpdateChecker(this, 12345).getVersion(version -> {
	            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
	                Bukkit.getLogger().info("[ScreamingBedwarsAddon] There is not a new update available.");
	            } else {
	                Bukkit.getLogger().info(ChatColor.YELLOW +" " + ChatColor.BOLD + "[ScreamingBedwarsAddon]: THERE IS A NEW UPDATE AVAILABLE.");
	            }
	        });
		 
		configurator = new Configurator(this);
		configurator.loadDefaults();
		
		Bukkit.getLogger().info("[BW ADDON]: Enabled Bedwars Addon v1.0.1");
		PluginManager pm = getServer().getPluginManager();
		new PlayerListener(this);
		InventoryListener.init(this);
		customShop shop = new customShop();
	}
	public static Configurator getConfigurator() {
        return plugin.configurator;
    }

	
	@Override
	public void onDisable()
	{
		plugin = null;
	}

	public static BwAddon getInstance()
	{
		return plugin;
	}
	
}


