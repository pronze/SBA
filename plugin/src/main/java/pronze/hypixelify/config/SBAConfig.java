package pronze.hypixelify.config;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.config.ConfigGenerator;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurateException;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurationNode;
import org.screamingsandals.bedwars.lib.ext.configurate.serialize.SerializationException;
import org.screamingsandals.bedwars.lib.ext.configurate.yaml.NodeStyle;
import org.screamingsandals.bedwars.lib.ext.configurate.yaml.YamlConfigurationLoader;
import org.screamingsandals.bedwars.lib.material.Item;
import org.screamingsandals.bedwars.lib.material.builder.ItemFactory;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.config.IConfigurator;
import pronze.hypixelify.utils.SBAUtil;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnInit;
import pronze.lib.core.auto.InitializationPriority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@AutoInitialize(initPriority = InitializationPriority.HIGHEST)
public class SBAConfig implements IConfigurator {
    public static HashMap<String, Integer> game_size = new HashMap<>();

    public final File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;

    public ConfigurationNode node(Object... keys) {
        return configurationNode.node(keys);
    }

    public static SBAConfig getInstance() {
        return Core.getObjectFromClass(SBAConfig.class);
    }

    public SBAConfig() {
        this.dataFolder = SBAHypixelify
                .getInstance()
                .getDataFolder();

        /* To avoid config confusions*/
        deleteFile("config.yml");
        deleteFile("bwaconfig.yml");
        loadDefaults();
    }

    @OnInit
    public void loadDefaults() {
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

            SBAHypixelify.getInstance().saveResource("languages/language_fallback.yml", true);

            saveFile("shops/" + (Main.isLegacy() ? "legacy-" : "") + "shop.yml");
            saveFile("shops/" + (Main.isLegacy() ? "legacy-" : "") + "upgradeShop.yml");

            loader = YamlConfigurationLoader
                    .builder()
                    .path(dataFolder.toPath().resolve("sbaconfig.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            configurationNode = loader.load();

            var generator = new ConfigGenerator(loader, configurationNode);
            generator.start()
                    .key("version").defValue(SBAHypixelify.getInstance().getVersion())
                    .key("locale").defValue("en")
                    .key("prefix").defValue("[SBAHypixelify]")
                    .section("debug")
                        .key("enabled").defValue(false)
                        .back()
                    .key("permanent-items").defValue(false)
                    .section("store")
                        .key("replace-store-with-hypixelstore").defValue(true)
                        .back()
                    .key("running-generator-drops").defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT"))
                    .key("block-item-drops").defValue(true)
                    .key("allowed-item-drops").defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT", "GOLDEN_APPLE", "OBSIDIAN", "TNT"))
                    .key("give-killer-resources").defValue(true)
                    .key("remove-sword-on-upgrade").defValue(true)
                    .key("block-players-putting-certain-items-onto-chest").defValue(true)
                    .key("disable-armor-inventory-movement").defValue(SBAHypixelify.getInstance().getVersion())
                    .section("floating-generator")
                        .key("enabled").defValue(true)
                        .key("holo-height").defValue(2.0)
                        .back()
                    .section("upgrades")
                        .key("timer-upgrades-enabled").defValue(true)
                        .key("show-upgrade-message").defValue(true)
                        .key("trap-detection-range").defValue(7)
                        .key("multiplier").defValue(0.25)
                        .section("prices")
                            .key("Sharpness-I").defValue(4)
                            .key("Sharpness-II").defValue(8)
                            .key("Sharpness-III").defValue(12)
                            .key("Sharpness-IV").defValue(16)
                            .key("Prot-I").defValue(4)
                            .key("Prot-II").defValue(8)
                            .key("Prot-III").defValue(12)
                            .key("Prot-IV").defValue(16)
                            .back()
                        .section("time")
                            .key("Diamond-II").defValue(200)
                            .key("Emerald-II").defValue(320)
                            .key("Diamond-III").defValue(540)
                            .key("Emerald-III").defValue(750)
                            .key("Diamond-IV").defValue(900)
                            .key("Emerald-IV").defValue(1000)
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
                            .key("format").defValue("§7[%color%%team%&7] %color%%player% §f> %message%")
                            .key("format-spectator").defValue("§7[§fSpectator§7] §f%player% > %message%")
                            .key("all-chat-prefix").defValue("@")
                            .key("all-chat-format").defValue("§7[§fALL§7] §7[%color%%team%§7] %color%player% §f> %message%")
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
                        .key("upgrade-shop-skin").defValue(779554483);


            var gameSection = generator
                    .start()
                    .section("lobby-scoreboard")
                    .section("player-size")
                    .section("games");

            BedwarsAPI.getInstance().getGameManager().getGameNames().forEach(gameName -> {
                try {
                    gameSection.key(gameName).defValue(4);
                } catch (SerializationException e) {
                    e.printStackTrace();
                }
            });

            generator.saveIfModified();
        } catch (Exception ex) {
            SBAHypixelify.getExceptionManager().handleException(ex);
        }

        if (node("first_start").getBoolean(false)) {
            Bukkit.getLogger().info("§aDetected first start");
            upgrade();
            try {
                node("first_start").set(false);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
            saveConfig();
        }
        Core.setDebugEnabled(node("debug", "enabled").getBoolean(false));
    }

    private void saveFile(String fileName, String saveTo) {
        final var file = new File(dataFolder, fileName);
        if (!file.exists()) {
            SBAHypixelify.getInstance().saveResource(saveTo, false);
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
            node("version").set(SBAHypixelify.getInstance().getVersion());
            node("autoset-bw-config").set(false);
            saveConfig();

            SBAHypixelify.getInstance().saveResource("shops/shop.yml", true);
            SBAHypixelify.getInstance().saveResource("shops/upgradeShop.yml", true);
            try (final var inputStream = SBAHypixelify.getInstance().getResource("config.yml")) {
                if (inputStream != null) {
                    final var configFile =
                            new File(Main.getInstance().getDataFolder().toFile(), "config.yml");
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
            SBAUtil.reloadPlugin(Main.getInstance().as(JavaPlugin.class));
        } catch (Exception ex) {
            SBAHypixelify.getExceptionManager().handleException(ex);
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
            for (String s : node((Object[]) string.split("\\.")).getList(String.class)) {
                s = ChatColor.translateAlternateColorCodes('&', s);
                list.add(s);
            }
        } catch (Exception ex) {
            SBAHypixelify.getExceptionManager().handleException(ex);
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
        return ChatColor.translateAlternateColorCodes('&', node((Object[])path.split("\\.")).getString());
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