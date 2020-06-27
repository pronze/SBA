package org.pronze.bwaddon;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.Plugin;


public class Configurator {

    public File file, oldfile, shopFile, upgradeShop;
    public FileConfiguration config;

    public final File dataFolder;
    public final BwAddon main;

    public Configurator(BwAddon main) {
        this.dataFolder = main.getDataFolder();
        this.main = main;
    }

    public void loadDefaults() {
        dataFolder.mkdirs();

        file = new File(dataFolder, "bwaconfig.yml");
        oldfile = new File(dataFolder, "bwconfig.yml");
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


        shopFile = new File(dataFolder, "shop.yml");
        upgradeShop = new File(dataFolder, "upgradeShop.yml");

        if (!shopFile.exists()) {
            main.saveResource("shop.yml", false);
        }

        if (!upgradeShop.exists()) {
            main.saveResource("upgradeShop.yml", false);
        }


        AtomicBoolean modify = new AtomicBoolean(false);

        checkOrSetConfig(modify, "store.replace-store-with-hypixelstore", true);
        checkOrSetConfig(modify, "running-generator-drops", Arrays.asList("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT"));;
        checkOrSetConfig(modify, "allowed-item-drops", Arrays.asList("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT", "GOLDEN_APPLE", "ENDER_PEAL", "OBSIDIAN"));
        checkOrSetConfig(modify, "give-killer-resources", true);
        checkOrSetConfig(modify, "remove-sword-on-upgrade", true);
        checkOrSetConfig(modify, "block-players-putting-certain-items-onto-chest", true);
        checkOrSetConfig(modify, "disable-armor-inventory-movement", true);
        checkOrSetConfig(modify, "version", BwAddon.getVersion());
        checkOrSetConfig(modify, "autoset-bw-config", true);

        if (modify.get()) {
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(config.getBoolean("autoset-bw-config") || !config.getString("version").equalsIgnoreCase(BwAddon.getVersion())){
            shopFile.delete();
            upgradeShop.delete();
            config.set("version", BwAddon.getVersion());
            config.set("autoset-bw-config", false);
            saveConfig();
            File file2 = new File(dataFolder, "config.yml");
            main.saveResource("config.yml", true);
            String pathname = Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/plugins/BedWars/config.yml";

            File file3 = new File(pathname);
            file3.delete();
            file2.renameTo(new File(pathname));

            shopFile = new File(dataFolder, "shop.yml");
            upgradeShop = new File(dataFolder, "upgradeShop.yml");

            if (!shopFile.exists()) {
                main.saveResource("shop.yml", false);
            }

            if (!upgradeShop.exists()) {
                main.saveResource("upgradeShop.yml", false);
            }

            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("BedWars");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            Bukkit.getServer().getPluginManager().enablePlugin(plugin);
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