package io.github.pronze.sba.config;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.material.Item;
import org.screamingsandals.lib.material.builder.ItemFactory;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Service
public class SBAConfig implements IConfigurator {
    public static HashMap<String, Integer> game_size = new HashMap<>();

    public static SBAConfig getInstance() {
        return ServiceManager.get(SBAConfig.class);
    }

    public JavaPlugin plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;

    public SBAConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadDefaults();
    }

    public ConfigurationNode node(Object... keys) {
        return configurationNode.node(keys);
    }

    public void loadDefaults() {
        this.dataFolder = plugin.getDataFolder();

        /* To avoid config confusions*/
        deleteFile("config.yml");
        deleteFile("bwaconfig.yml");
        try {
            langFolder = new File(dataFolder, "languages");
            gamesInventoryFolder = new File(dataFolder, "games-inventory");
            shopFolder = new File(dataFolder, "shops");

            if (!shopFolder.exists()) {
                shopFolder.mkdirs();
            }
            if (!gamesInventoryFolder.exists()) {
                gamesInventoryFolder.mkdirs();
            }
            if (!langFolder.exists()) {
                langFolder.mkdirs();
            }

            saveFile("languages/language_en.yml");
            saveFile("games-inventory/solo.yml");
            saveFile("games-inventory/double.yml");
            saveFile("games-inventory/triple.yml");
            saveFile("games-inventory/squad.yml");

            plugin.saveResource("languages/language_fallback.yml", true);

            saveFile("shops/shop.yml");
            saveFile("shops/upgradeShop.yml");

            loader = YamlConfigurationLoader
                    .builder()
                    .path(dataFolder.toPath().resolve("sbaconfig.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            configurationNode = loader.load();

            generator = new ConfigGenerator(loader, configurationNode);
            generator.start()
                    .key("version").defValue(plugin.getDescription().getVersion())
                    .key("locale").defValue("en")
                    .key("prefix").defValue("[SBA]")
                    .section("debug")
                        .key("enabled").defValue(false)
                        .back()
                    .key("permanent-items").defValue(false)
                    .section("store")
                        .key("replace-store-with-hypixelstore").defValue(true)
                        .back()
                    .section("tnt-fireball-jumping")
                        .key("source-damage").defValue(0.25)
                        .key("acceleration-y").defValue(0.8)
                        .key("reduce-y").defValue(2.0)
                        .key("launch-multiplier").defValue(3.4)
                        .key("detection-distance").defValue(8.0D)
                        .back()
                    .key("explosion-damage").defValue(0.25)
                    .key("running-generator-drops").defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT"))
                    .key("block-item-drops").defValue(true)
                    .key("allowed-item-drops").defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT", "GOLDEN_APPLE", "OBSIDIAN", "TNT"))
                    .key("give-killer-resources").defValue(true)
                    .key("remove-sword-on-upgrade").defValue(true)
                    .key("block-players-putting-certain-items-onto-chest").defValue(true)
                    .key("disable-armor-inventory-movement").defValue(true)
                    .section("floating-generator")
                        .key("enabled").defValue(true)
                        .key("height").defValue(2.5)
                        .back()
                    .section("upgrades")
                        .key("timer-upgrades-enabled").defValue(true)
                        .key("show-upgrade-message").defValue(true)
                        .key("trap-detection-range").defValue(7)
                        .key("multiplier").defValue(0.25)
                        .section("limit")
                            .key("Sharpness").defValue(1)
                            .key("Protection").defValue(4)
                            .key("Efficiency").defValue(2)
                            .key("Iron").defValue(30)
                            .key("Gold").defValue(7)
                            .key("Diamond-I").defValue(4)
                            .key("Emerald-I").defValue(4)
                            .key("Diamond-II").defValue(6)
                            .key("Emerald-II").defValue(6)
                            .key("Diamond-III").defValue(8)
                            .key("Emerald-III").defValue(8)
                            .key("Diamond-IV").defValue(12)
                            .key("Emerald-IV").defValue(12)
                            .back()
                        .section("prices")
                            .key("Sharpness-I").defValue(4)
                            .key("Sharpness-II").defValue(8)
                            .key("Sharpness-III").defValue(12)
                            .key("Sharpness-IV").defValue(16)
                            .key("Prot-I").defValue(4)
                            .key("Prot-II").defValue(8)
                            .key("Prot-III").defValue(12)
                            .key("Prot-IV").defValue(16)
                            .key("Efficiency-I").defValue(4)
                            .key("Efficiency-II").defValue(4)
                            .key("Efficiency-III").defValue(4)
                            .key("Efficiency-IV").defValue(4)
                            .back()
                        .section("time")
                            .key("Diamond-II").defValue(300)
                            .key("Emerald-II").defValue(700)
                            .key("Diamond-III").defValue(1000)
                            .key("Emerald-III").defValue(1300)
                            .key("Diamond-IV").defValue(1500)
                            .key("Emerald-IV").defValue(1800)
                            .back()
                        .back()
                    .section("date")
                        .key("format").defValue("MM/dd/yy")
                        .back()
                    .section("lobby-scoreboard")
                        .key("enabled").defValue(true)
                        .back()
                    .key("first_start").defValue(true)
                    .section("shout")
                        .key("time-out").defValue(60)
                        .back()
                    .key("disabled-sword-armor-damage").defValue(true)
                    .section("game")
                        .key("tab-health").defValue(true)
                        .key("tag-health").defValue(true)
                        .back()
                    .section("games-inventory")
                        .key("enabled").defValue(true)
                        .back()
                    .key("show-health-in-tablist").defValue(true)
                    .key("show-health-under-player-name").defValue(true)
                    .section("main-lobby")
                        .key("enabled").defValue(false)
                        .key("custom-chat").defValue(true)
                        .key("tablist-modifications").defValue(true)
                        .key("progress-format").defValue("§b%progress%§7/§e%total%")
                        .back()
                    .section("experimental")
                        .key("reset-item-meta-on-purchase").defValue(false)
                        .back()
                        .back()
                    .section("party")
                        .key("enabled").defValue(true)
                        .key("leader-autojoin-autoleave").defValue(true)
                        .key("invite-expiration-time").defValue(60)
                        .back()
                    .section("team-status")
                        .key("target-destroyed").defValue("§c\u2717")
                        .key("target-exists").defValue("§a\u2713")
                        .key("alive").defValue("%color% %team% §a\u2713 §8%you%")
                        .key("destroyed").defValue("%color% %team% §a§f%players%§8 %you%")
                        .key("eliminated").defValue("%color% %team% §c\u2718 %you%")
                        .back()
                    .section("chat-format")
                        .section("game-chat")
                            .key("enabled").defValue(true)
                            .key("format").defValue("§7[%color%%team%§7] %color%%player% §f> %message%")
                            .key("format-spectator").defValue("§7[§fSpectator§7] §f%player% > %message%")
                            .key("all-chat-prefix").defValue("@a")
                            .key("all-chat-format").defValue("§7[§fALL§7] §7[%color%%team%§7] %color%%player% §f> %message%")
                            .back()
                        .section("lobby-chat")
                            .key("enabled").defValue(true)
                            .key("format").defValue("%color%%player% §f> %message%")
                            .back()
                       .back()
                    .section("shop")
                        .key("removePurchaseMessages").defValue(false)
                        .key("shopback").defValue("BARRIER")
                        .key("pageback").defValue("ARROW")
                        .key("pageforward").defValue("BARRIER")
                        .key("shopcosmetic").defValue("AIR")
                        .section("normal-shop")
                            .key("name").defValue("[SBA] Shop")
                            .key("rows").defValue(4)
                            .key("render-actual-rows").defValue(6)
                            .key("render-offset").defValue(9)
                            .key("render-header-start").defValue(0)
                            .key("render-footer-start").defValue(45)
                            .key("items-on-row").defValue(9)
                            .key("show-page-numbers").defValue(true)
                            .key("enabled").defValue(true)
                            .back()
                        .section("upgrade-shop")
                            .key("rows").defValue(4)
                            .key("render-actual-rows").defValue(6)
                            .key("render-offset").defValue(9)
                            .key("render-header-start").defValue(0)
                            .key("render-footer-start").defValue(45)
                            .key("items-on-row").defValue(9)
                            .key("show-page-numbers").defValue(true)
                            .key("enabled").defValue(true)
                            .back()
                        .back()
                    .section("player-statistics")
                        .key("xp-to-level-up").defValue(500)
                        .back()
                    .section("npc")
                        .key("enabled").defValue(true)
                        .key("shop-skin").defValue(561657710)
                        .key("upgrade-shop-skin").defValue(779554483)
                        .back()
                    .section("generator-splitter")
                        .key("allowed-materials").defValue(List.of("GOLD_INGOT", "IRON_INGOT"))
                        .back();
            generator.saveIfModified();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnPostEnable
    public void postEnable() {
        var gameSection = generator
                .start()
                .section("lobby-scoreboard")
                .section("player-size")
                .section("games");

        Main.getGameNames().forEach(gameName -> {
            try {
                gameSection.key(gameName).defValue(4);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });

        try {
            generator.saveIfModified();
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

    public void forceReload() {
        loader = YamlConfigurationLoader
                .builder()
                .path(dataFolder.toPath().resolve("sbaconfig.yml"))
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
    public void upgrade() {
        try {
            plugin.saveResource("shops/shop.yml", true);
            plugin.saveResource("shops/upgradeShop.yml", true);
            try (final var inputStream = plugin.getResource("config.yml")) {
                if (inputStream != null) {
                    final var configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
                    if (configFile.exists()) {
                        configFile.delete();
                    }
                    configFile.createNewFile();
                    try (final var outputStream = new FileOutputStream(configFile)) {
                        inputStream.transferTo(outputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            SBAUtil.reloadPlugin(Main.getInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public double getDouble(String path, double def) {
        return node((Object[])path.split("\\.")).getDouble(def);
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
    public List<String> getStringList(String string)  {
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
        return node((Object[])path.split("\\.")).getInt(def);
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
        return node((Object[])path.split("\\.")).getBoolean(def);
    }

    public String getString(String path) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(node((Object[]) path.split("\\.")).getString()));
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
