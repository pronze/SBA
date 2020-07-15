package org.pronze.hypixelify;

import java.io.File;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.Plugin;
import org.pronze.hypixelify.listener.LobbyScoreboard;
import org.screamingsandals.bedwars.Main;


public class Configurator {

    public File file, oldfile, shopFile, upgradeShop;
    public FileConfiguration config;
    public static HashMap<String, List<String>> Scoreboard_Lines;
    public static List<String> overstats_message;
    public static List<String> gamestart_message;
    public static HashMap<String, Integer> game_size;

    public final File dataFolder;
    public final Hypixelify main;

    public Configurator(Hypixelify main) {
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
        checkOrSetConfig(modify, "running-generator-drops", Arrays.asList("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT"));
        checkOrSetConfig(modify, "allowed-item-drops", Arrays.asList("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT", "GOLDEN_APPLE", "ENDER_PEAL", "OBSIDIAN", "TNT"));
        checkOrSetConfig(modify, "give-killer-resources", true);
        checkOrSetConfig(modify, "remove-sword-on-upgrade", true);
        checkOrSetConfig(modify, "block-players-putting-certain-items-onto-chest", true);
        checkOrSetConfig(modify, "disable-armor-inventory-movement", true);
        checkOrSetConfig(modify, "version", Hypixelify.getVersion());
        checkOrSetConfig(modify, "autoset-bw-config", true);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-I", 4);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-II", 8);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-III", 12);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-IV", 16);
        checkOrSetConfig(modify, "lobby-scoreboard.enabled", true);
        checkOrSetConfig(modify, "lobby-scoreboard.interval", 2);
        checkOrSetConfig(modify, "lobby-scoreboard.state.waiting", "§fWaiting...");
        checkOrSetConfig(modify, "first_start", true);
        for(String game : Main.getGameNames()){
            String str = "lobby-scoreboard.player-size.games." + game;
            checkOrSetConfig(modify, str, 4);
        }
        checkOrSetConfig(modify, "game-start.message", Arrays.asList(
                "&a\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac"
                , "                             &f&lBed Wars"
                , ""
                , "    &e&lProtect your bed and destroy the enemy beds."
                , "     &e&lUpgrade yourself and your team by collecting"
                , "   &e&lIron, Gold, Emerald and Diamond from generators"
                , "            &e&lto access powerful upgrades."
                , ""
                , "&a\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac"
        ));
        checkOrSetConfig(modify, "overstats.message", Arrays.asList(
                "&a\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac"
                , "                             &e&lBEDWARS"
                , ""
                , "                             {color}{win_team}"
                , "                             {win_team_players}"
                , ""
                , "    &e&l1st&7 - &f{first_1_kills_player} &7- &f{first_1_kills}"
                , "    &6&l2nd&7 - &f{first_2_kills_player} &7- &f{first_2_kills}"
                , "    &c&l3rd&7 - &f{first_3_kills_player} &7- &f{first_3_kills}"
                , ""
                , "&a\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac"
        ));
        checkOrSetConfig(modify, "scoreboard.lines.default", Arrays.asList(
                "&7{date}"
                , ""
                , ""
                , "{team_status}"
                , ""
                , "&fKills: &a{kills}"
                , "&fFinal Kills: &a{finalkills}"
                , "&fBed Broken: &a{beds}"
                , ""
                , "&ewww.minecraft.net"
        ));
        checkOrSetConfig(modify, "scoreboard.lines.8", Arrays.asList(
                "&7{date}"
                , ""
                , ""
                , "{team_status}"
                , ""
                , "&ewww.minecraft.net"
        ));

        checkOrSetConfig(modify, "lobby-scoreboard.state.countdown", "&fStarting in &a{countdown}s");
        checkOrSetConfig(modify, "lobby-scoreboard.title", Arrays.asList(
                "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&e&lBED WARS"
                , "&6&lB&e&lED WARS"
                , "&f&lB&6&lE&e&lD WARS"
                , "&f&lBE&6&lD&e&l WARS"
                , "&f&lBED&6&l &e&lWARS"
                , "&f&lBED &6&lW&e&lARS"
                , "&f&lBED W&6&lA&e&lRS"
                , "&f&lBED WA&6&lR&e&lS"
                , "&f&lBED WAR&6&lS"
                , "&f&lBED WARS"
                , "&e&lBED WARS"
                , "&f&lBED WARS"));

        checkOrSetConfig(modify, "lobby_scoreboard.lines", Arrays.asList(
                "&7{date}"
                , ""
                , "&fMap: &a{game}"
                , "&fPlayers: &a{players}/{maxplayers}"
                , ""
                , "{state}"
                , ""
                , "&fMode: &a4v4v4v4"
                , "&fVersion: &7v1.1"
                , ""
                , "&ewww.sample.net"
        ));
        if (modify.get()) {
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Scoreboard_Lines = new HashMap<>();
        for (String key : Objects.requireNonNull(config.getConfigurationSection("scoreboard.lines")).getKeys(false))
            Scoreboard_Lines.put(key,
                    LobbyScoreboard.listcolor(config.getStringList("scoreboard.lines." + key)));

        game_size = new HashMap<>();
        for(String s : Main.getGameNames()){
            int size = config.getInt("lobby-scoreboard.player-size.games." + s, 4);
            game_size.put(s, size);
        }

        overstats_message = LobbyScoreboard.listcolor(config.getStringList("overstats.message"));
        gamestart_message = LobbyScoreboard.listcolor(config.getStringList("game-start.message"));

        if(config.getBoolean("first_start")){
            Bukkit.getLogger().info("[SBAHypixelify]:" + ChatColor.GREEN +" Detected first start");
            upgradeCustomFiles();
            config.set("first_start", false);
            saveConfig();
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("BedWars");
            assert plugin != null;
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            Bukkit.getServer().getPluginManager().enablePlugin(plugin);
            Bukkit.getLogger().info("[SBAHypixelify]: " + ChatColor.GREEN +" Made changes to the config.yml file!");
        }
    }

    public void upgradeCustomFiles() {
        shopFile.delete();
        upgradeShop.delete();
        config.set("version", Hypixelify.getVersion());
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
        assert plugin != null;
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
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
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(string)));
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
        if (Objects.requireNonNull(config.getConfigurationSection("")).getStringList(string).size() == 0) {
            for (String l : Objects.requireNonNull(config.getConfigurationSection("")).getStringList(string)) {
                ChatColor.translateAlternateColorCodes('&', l);
            }
        }
        return Objects.requireNonNull(config.getConfigurationSection("")).getStringList(string);

    }

    public Set<String> getStringKeys(String string) {
        return Objects.requireNonNull(config.getConfigurationSection(string)).getKeys(true);
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