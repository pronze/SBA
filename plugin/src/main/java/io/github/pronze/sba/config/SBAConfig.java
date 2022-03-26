package io.github.pronze.sba.config;
import io.github.pronze.sba.fix.BungeecordNPC;
import io.github.pronze.sba.utils.FirstStartConfigReplacer;
import io.github.pronze.sba.utils.Logger;
import net.md_5.bungee.api.ChatColor;
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
public class SBAConfig implements IConfigurator {

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
            saveFile("languages/language_ru.yml");

            saveFile("games-inventory/solo.yml");
            saveFile("games-inventory/double.yml");
            saveFile("games-inventory/triple.yml");
            saveFile("games-inventory/squad.yml");

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
                    .key("editing-hologram-enabled").defValue(true)
                    .section("debug")
                        .key("enabled").defValue(false)
                        .back()
                    .key("disable-item-damage").defValue(true)
                    .key("permanent-items").defValue(false)
                    .section("tnt-fireball-jumping")
                        .key("source-damage").defValue(1)
                        .key("acceleration-y").defValue(0.8)
                        .key("reduce-y").defValue(2.0)
                        .key("launch-multiplier").defValue(3.4)
                        .key("detection-distance").defValue(8.0D)
                        .key("fall-damage").defValue(3.0D)
                        .back()
                    .key("explosion-damage").defValue(0.25)
                    .key("running-generator-drops").defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT"))
                    .key("block-item-drops").defValue(true)
                    .key("allowed-item-drops").defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT", "GOLDEN_APPLE", "OBSIDIAN", "TNT"))
                    .key("give-killer-resources").defValue(true)
                    .key("replace-sword-on-upgrade").defValue(true)
                    .key("block-players-putting-certain-items-onto-chest").defValue(true)
                    .key("disable-armor-inventory-movement").defValue(true)
                    .key("final-kill-lightning").defValue(true)
                    .section("floating-generator")
                        .section("mapping")
                            .key("EMERALD").defValue("EMERALD_BLOCK")
                            .key("DIAMOND").defValue("DIAMOND_BLOCK")
                            .back()
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
                            .key("Iron").defValue(48)
                            .key("Gold").defValue(8)
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
                            .key("Efficiency-II").defValue(8)
                            .key("Efficiency-III").defValue(12)
                            .key("Efficiency-IV").defValue(16)
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
                        .key("progress-format").defValue("§b%progress%§7/§a%total%")
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
                        .key("can-downgrade-item").defValue(false)
                        .key("removePurchaseMessages").defValue(false)
                        .key("shopback").defValue("BARRIER")
                        .key("pageback").defValue("ARROW")
                        .key("pageforward").defValue("BARRIER")
                        .key("shopcosmetic").defValue("AIR")
                        .section("normal-shop")
                            .key("entity-name").defValue(List.of("§bITEM SHOP", "§e§lRIGHT CLICK"))
                            .section("skin")
                                .key("value").defValue("ewogICJ0aW1lc3RhbXAiIDogMTYyNjA5MTkyNjQ3NCwKICAicHJvZmlsZUlkIiA6ICJiNjM2OWQ0MzMwNTU0NGIzOWE5OTBhODYyNWY5MmEwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb2JpbmhvXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iZmRmOTBkZWI0YmYzNmM3Y2I4Y2Y2Zjg0NWQ0OTYzZWVhODkyNzRlMDBkNmFjMzQxNjJiYTc3MTE1ZjMyMWZhIgogICAgfQogIH0KfQ==")
                                .key("signature").defValue("dRVORv3TXsP80Xfzy2/CYAHN92iF+4UYe8Un7jSEvCc9fwz9z39lB1ooO62hdArqZuNU2b7OKUZd8LYbctj8hUnaKdQ4kxbO1xQENRATNGsk1PWrVLgMikg2Vx4+2DCakE18f9UAVGHqGFVInI3dCCG7QmWqNI+l4g+GjxNzYVDWlW/PsB7CuQsOhJGY1hq2B1JRQ4mhZl0Tks/gU+qdw+ClOShB50KB2Q60d+fd04xYYCAJHk/a9c45EBBU8rHix8M2GV6hYXQZcdXZMB/KZBHAQfCrnlMhlTOzKfUYI5sDzSH6zBIWtE36zVeuYuM4Rppt0doT9qNJXJIYJ4UlZ11l8F9/ShQST+h138yJMgxRmIi3KAGhEJ8aVKkeeXMARbF9uFZbxHoZd66lhA5BWYsrFhyxKrPVO2AsfsfFQCY/DLurEdVVTWcN5K4Frh2Pt97gDJYBYXOuClaS367q57X76yuFqOFe6AvRI1Hvr22k8WvqpSqXzEXlfLUMwz9iKgbptS/Y9X78dseBwS7OdmUTFl1VgDZerQH1RDUrDTxr/Hiv0KE1czhbOQInRTaAT65dPB9RHZ3OnlgHcA7+7joRuPHihPuLH45NKHAxLn10CiolrtgxmGejkWqtNVKNlZNiAl49u4CRCqC13P/crCi9vlonjPg8mkLivsuyA8g=")
                                .back()
                            .key("shopcosmetic").defValue("GRAY_STAINED_GLASS_PANE")
                            .key("name").defValue("[SBA] Item Shop")
                            .key("rows").defValue(6)
                            .key("render-actual-rows").defValue(6)
                            .key("render-offset").defValue(0)
                            .key("render-header-start").defValue(9)
                            .key("render-footer-start").defValue(600)
                            .key("items-on-row").defValue(9)
                            .key("show-page-numbers").defValue(false)
                            .key("enabled").defValue(true)
                            .back()
                        .section("upgrade-shop")
                            .key("name").defValue("[SBA] Upgrade Shop")
                            .key("entity-name").defValue(List.of("§bTEAM", "§bUPGRADES", "§e§lRIGHT CLICK"))
                            .section("skin")
                                .key("value").defValue("ewogICJ0aW1lc3RhbXAiIDogMTYyNjA4ODMxNjI3OCwKICAicHJvZmlsZUlkIiA6ICIxYWZhZjc2NWI1ZGY0NjA3YmY3ZjY1ZGYzYWIwODhhOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJMb3lfQmxvb2RBbmdlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ZjM0N2NiYjg3ZmVjMDA2MDc3ZDI2MTFjZjk4MTM4NGMwOWNlM2FjNDQ1M2FlY2M4MjMzYWMwODk3YjA3ZDYwIgogICAgfQogIH0KfQ==")
                                .key("signature").defValue("oqCVAspoQ/uCoZX/2XTgoYjAGBVJSXLi+/QPHKPaGqP9zEXHE7k5TH5Z7K7x5D8ECCtZq8jW6GCzIzigvTKp2jRXoEnjnOmzc82P6nSV39NKueB6XVi12fluewaLNlzhJUwn1+7NOYlwKH3qN3/Rd8bE3lNv9bkrWN67zjnYDA7o4vxfkzgV9Hd1CEV9oVRsSne3rm7kYN1iRoMYArL4+EYTYUxd6HMUZ33b5yecQz+UctiGcRUXzPDU/RANxYlBkH6WIe6C8QH84MtjTD20X/2qmlhYeTA98Jf5eiPLfTd+30q603moUEf5VyEuaK3qxMektnaaIO0Wdx7fGYQelbDkejxqL7c//gupksKMqlFqBtLYTRcAXCS5hFbl2tnN80O4Kq0v4E1HOmBZYKZf/yYahNbRZyj0hNaG1dDdM/dqfBmBQWbcSnvb3M9YE9mXAoddryRii6kHVEQWO+C8xHECQK69AN/XhGnr+2X+cDfHHGIrVY10/rVXF2faPTauj/aFOZLp/fhOuLzOSQZYQCIe/jiO5BbEJ6owU5cyL0V/8x4609Pu7REhwiS2wDUrfLl5yyTyw232pVwO545awKV0O0/fnWeeLePuv8qBh/ngNZ52iDeEfpDeBe3DB1FFOSup96/eL/7blzDxKmzIVO29egg8xTVsYwUr23J1y0s=")
                                .back()
                            .key("rows").defValue(4)
                            .key("render-actual-rows").defValue(6)
                            .key("render-offset").defValue(9)
                            .key("render-header-start").defValue(0)
                            .key("render-footer-start").defValue(45)
                            .key("items-on-row").defValue(9)
                            .key("show-page-numbers").defValue(false)
                            .key("enabled").defValue(true)
                            .back()
                        .back()
                    .section("player-statistics")
                        .key("xp-to-level-up").defValue(5000)
                        .back()
                    .section("npc")
                        .key("enabled").defValue(true)
                        .key("shop-skin").defValue(561657710)
                        .key("upgrade-shop-skin").defValue(779554483)
                        .back()
                    .section("generator-splitter")
                        .key("allowed-materials").defValue(List.of("GOLD_INGOT", "IRON_INGOT"))
                    .back()
                    .section("upgrade-item")
                        .key("leggings").defValue(true)
                        .key("chestplate").defValue(false)
                    .back()
                    .section("sounds")
                    .key("on_trap_triggered").defValue("ENTITY_ENDER_DRAGON_GROWL")
                    .back()
                    .key("replace-stores-with-npc").defValue(true);

            generator.saveIfModified();
            if (!node("debug", "enabled").getBoolean()) {
                Logger.setMode(Logger.Level.DISABLED);
            }
            
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
            node("version").set(plugin.getDescription().getVersion());
            saveConfig();
            plugin.saveResource("shops/shop.yml", true);
            plugin.saveResource("shops/upgradeShop.yml", true);

            final var langFiles = langFolder.listFiles();
            if (langFiles != null)
                Arrays.stream(langFiles).forEach(File::delete);

            saveFile("languages/language_en.yml");
            ServiceManager.get(FirstStartConfigReplacer.class).updateBedWarsConfig();
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

    public void set(String path, Object value)
    {
        try {
            node(path).set(value);
            generator.saveIfModified();
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
