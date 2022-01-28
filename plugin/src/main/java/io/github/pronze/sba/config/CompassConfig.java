package io.github.pronze.sba.config;

import io.github.pronze.sba.config.ConfigGenerator.ConfigSection;
import io.github.pronze.sba.utils.FirstStartConfigReplacer;
import io.github.pronze.sba.utils.Logger;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import io.github.pronze.sba.utils.SBAUtil;
import java.io.File;
import java.util.*;
import java.util.List;

@Service
public class CompassConfig implements IConfigurator {

    public static CompassConfig getInstance() {
        return ServiceManager.get(CompassConfig.class);
    }

    public JavaPlugin plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;

    public CompassConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadDefaults();
    }

    public ConfigurationNode node(Object... keys) {
        return configurationNode.node(keys);
    }

    public void loadDefaults() {
        this.dataFolder = plugin.getDataFolder();

        /* To avoid config confusions */
        deleteFile("compass.yml");

        try {
            loader = YamlConfigurationLoader
                    .builder()
                    .path(dataFolder.toPath().resolve("compass.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            configurationNode = loader.load();

            generator = new ConfigGenerator(loader, configurationNode);
            var section = generator.start()
                    .key("version").defValue(plugin.getDescription().getVersion())
                    .key(USE_COMMUNICATIONS).defValue(true)
                    .key(TRACKER_UPDATE_RATE).defValue(1)
                    .key(MESSAGE_SEND_SOUND)
                    .defValue(List.of("SUCCESSFUL_HIT", "ENTITY_ARROW_HIT_PLAYER", "ENTITY_ARROW_HIT_PLAYER"))
                    .key(PLAYER_TRACK_COST).defValue(2)
                    .key(PLAYER_TRACK_RESOURCE).defValue("emerald")
                    .key(MAIN_MENU_SIZE).defValue(27)
                    .key(TRACKER_MENU_SIZE).defValue(36)
                    .key(TRACKER_MENU_SLOTS).defValue(List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25))
                    .key(COMMUNICATIONS_MENU_SIZE).defValue(45)
                    .key(COMMUNICATIONS_MENU_ITEMS).defValue(Collections.emptyList())
                    .section(COMMUNICATIONS_MENU_TEAMS)
                    .key("size").defValue(36)
                    .key("slots").defValue(List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25))
                    .back()
                    .section(COMMUNICATIONS_MENU_RESOURCES)
                    .key("size").defValue(36)
                    .back();

            saveItem(section,COMPASS_ITEM, Material.COMPASS, 8);
            saveItem(section,TRACKER_SHOP, Material.COMPASS, 45);
            saveItem(section,MAIN_MENU_TRACKER, Material.COMPASS, 13);
            saveItem(section,MAIN_MENU_TRACKER_TEAM, Material.COMPASS, 15);
            saveItem(section, MAIN_MENU_COMMUNICATIONS, Material.EMERALD, 11);
            
            saveItem(section,TRACKER_MENU_TEAM_ITEM, List.of("WOOL", "WOOL", "WHITE_WOOL"));
            saveItem(section,TRACKER_MENU_BACK_ITEM, Material.ARROW, 31);
            saveItem(section, COMMUNICATIONS_MENU_BACK, Material.ARROW, 40);
            
            if (isFirstTime()) {
                saveCommunicationItem(section,"1", "BOOK", 10, MenuType.NONE);
                saveCommunicationItem(section,"2", "BOOK", 11, MenuType.NONE);
                saveCommunicationItem(section,"3", Compass.getBedWars().getForCurrentVersion("IRON_FENCE", "IRON_FENCE", "IRON_BARS"), 12, MenuType.NONE);
                saveCommunicationItem(section,"4", "IRON_SWORD", 13, MenuType.TEAM);
                saveCommunicationItem(section,"5", "DIAMOND", 14, MenuType.RESOURCE);
                saveCommunicationItem(section,"6", "CHEST", 15, MenuType.RESOURCE);
                saveCommunicationItem(section,"7", "BOOK", 20, MenuType.NONE);
                saveCommunicationItem(section,"8", "BOOK", 21, MenuType.NONE);
                saveCommunicationItem(section,"9", Compass.getBedWars().getForCurrentVersion("IRON_FENCE", "IRON_FENCE", "IRON_BARS"), 22, MenuType.NONE);
                saveCommunicationItem(section,"10", "IRON_SWORD", 23, MenuType.TEAM);
                saveCommunicationItem(section,"11", "DIAMOND", 24, MenuType.RESOURCE);
                saveCommunicationItem(section,"12", "FEATHER", 25, MenuType.NONE);
            }
            
            generator.saveIfModified();
            if (!node("debug", "enabled").getBoolean()) {
                Logger.setMode(Logger.Level.DISABLED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveCommunicationItem(ConfigSection cs,String path, String material, int slot, MenuType menuType) {
        path = COMMUNICATIONS_MENU_ITEMS + "." + path;
        cs.section(path).key("material").defValue(material);
        cs.section(path).key("enchanted").defValue(false);
        cs.section(path).key("slot").defValue(slot);
        cs.section(path).key("menu").defValue(menuType.toString());
    }
    
    private void saveItem(ConfigSection cs, String path, Material material, int slot) throws SerializationException {
        cs.section(path)
                .key("material").defValue(material.toString())
                .key("enchanted").defValue(false)
                .key("slot").defValue(slot);
    }
    private void saveItem(ConfigSection cs, String path, List<String> material) throws SerializationException {
        cs.section(path)
                .key("material").defValue(material.toString())
                .key("enchanted").defValue(false);
    }

    @OnPostEnable
    public void postEnable() {

    }

    public static final String USE_COMMUNICATIONS = "use-quick-communications",
            COMPASS_ITEM = "compass-item",
            TRACKER_SHOP = "tracker-shop",
            MAIN_MENU_SIZE = "menus.main-menu.size",
            MAIN_MENU_TRACKER = "menus.main-menu.tracker",
            MAIN_MENU_TRACKER_TEAM = "menus.main-menu.tracker-team",
            MAIN_MENU_COMMUNICATIONS = "menus.main-menu.communications",
            TRACKER_MENU = "menus.tracker-menu",
            TRACKER_MENU_SIZE = "menus.tracker-menu.size",
            TRACKER_MENU_SLOTS = "menus.tracker-menu.slots",
            TRACKER_MENU_TEAM_ITEM = "menus.tracker-menu.team-item",
            TRACKER_MENU_BACK_ITEM = "menus.tracker-menu.back-item",
            COMMUNICATIONS_MENU_SIZE = "menus.communications.size",
            COMMUNICATIONS_MENU_BACK = "menus.communications.back-item",
            COMMUNICATIONS_MENU_ITEMS = "menus.communications.items",
            COMMUNICATIONS_MENU_TEAMS = "communication-menus.teams",
            COMMUNICATIONS_MENU_RESOURCES = "communication-menus.resources",
            MESSAGE_SEND_SOUND = "team-message-sound",
            PLAYER_TRACK_COST = "player-track.cost",
            PLAYER_TRACK_RESOURCE = "player-track.resource",
            TRACKER_UPDATE_RATE = "tracker-update-rate";

    public void forceReload() {
        loader = YamlConfigurationLoader
                .builder()
                .path(dataFolder.toPath().resolve("compass.yml"))
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        try {
            configurationNode = loader.load();
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String fileName, String saveTo) {
        final var file = new File(dataFolder, fileName);
        if (!file.exists()) {
            plugin.saveResource(saveTo, false);
        }
    }

    private void deleteFile(String fileName) {
        final var file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    private void saveFile(String fileName) {
        saveFile(fileName, fileName);
    }

    @Override
    public double getDouble(String path, double def) {
        return node((Object[]) path.split("\\.")).getDouble(def);
    }

    @Override
    public void saveConfig() {
        try {
            this.loader.save(this.configurationNode);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getStringList(String string) {
        final var list = new ArrayList<String>();
        try {
            for (String s : Objects.requireNonNull(node((Object[]) string.split("\\.")).getList(String.class))) {
                s = ChatColor.translateAlternateColorCodes('&', s);
                list.add(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public Integer getInt(String path, Integer def) {
        return node((Object[]) path.split("\\.")).getInt(def);
    }

    @Override
    public Byte getByte(String path, Byte def) {
        final var val = node((Object[]) path.split("\\.")).getInt(def);
        if (val > 127 || val < -128)
            return def;
        return (byte) val;
    }

    @Override
    public Boolean getBoolean(String path, boolean def) {
        return node((Object[]) path.split("\\.")).getBoolean(def);
    }

    public String getString(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(node((Object[]) path.split("\\.")).getString()));
    }

    @Override
    public String getString(String path, String def) {
        final var str = getString(path);
        if (str == null) {
            return def;
        }
        return str;
    }

    public Item readDefinedItem(ConfigurationNode node, String def) {
        if (!node.empty()) {
            var obj = node.raw();
            return ItemFactory.build(obj).orElse(ItemFactory.getAir());
        }

        return ItemFactory.build(def).orElse(ItemFactory.getAir());
    }
}
