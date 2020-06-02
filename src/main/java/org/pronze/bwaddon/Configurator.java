package org.pronze.bwaddon;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class Configurator {

    public File file;
    public File oldfile;
    public FileConfiguration config;

    public final File dataFolder;
    public final BwAddon main;
    
    public Configurator(BwAddon main) {
    	this.dataFolder = main.getDataFolder();
    	this.main = main;
    }
    
	public void loadDefaults() {
		 dataFolder.mkdirs();
		 
		file = new File(dataFolder, "bwconfig.yml");
		oldfile = new File(dataFolder, "config.yml");
		
		config = new YamlConfiguration();
		
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
        	e.printStackTrace();
        }
		
		
		if (oldfile.exists())
				oldfile.delete();
		
		AtomicBoolean modify = new AtomicBoolean(false);
		
        checkOrSetConfig(modify, "store.replace-store-with-hypixelstore", true);
        checkOrSetConfig(modify, "allowed-item-drops", Arrays.asList("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT")); 


        checkOrSetConfig(modify, "version", 1);

        if (modify.get()) {
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
        
	}
	
	public void saveConfig() {
	    try {
	        config.save(file);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	public String getString(String string, String defaultString) {
		if (config.getConfigurationSection(string) == null) {
			return ChatColor.translateAlternateColorCodes('&', defaultString);
		}
		return ChatColor.translateAlternateColorCodes('&', config.getString(string));
	}
	
	public boolean getBoolean(String string, boolean defaultBoolean) {
		if (config.getConfigurationSection(string) == null) {
			return defaultBoolean;
		}
		return config.getBoolean(string);
	}
	
	public int getInt(String string, int defaultInt) {
		if (config.getConfigurationSection(string) == null) {
			return defaultInt;
		}
		return config.getInt(string);
	}
	
	public List<String> getStringList(String string) {
		if (config.getConfigurationSection("").getStringList(string).size() == 0) {
			for (String l : config.getConfigurationSection("").getStringList(string)) {
				ChatColor.translateAlternateColorCodes('&', l);
			}
		}
		return config.getConfigurationSection("").getStringList(string);
		
	}
	
	public Set<String> getStringKeys(String string) {
		return config.getConfigurationSection(string).getKeys(true);
	}

	
    private void checkOrSetConfig(AtomicBoolean modify, String path, Object value) {
        checkOrSet(modify, this.config, path, value);
    }

    private static void checkOrSet(AtomicBoolean modify, FileConfiguration config, String path, Object value) {
        if (!config.isSet(path)) {
            if (value instanceof Map) {
                config.createSection(path, (Map<?, ?>) value);
            } else {
                config.set(path, value);
            }
            modify.set(true);
        }
    }
}