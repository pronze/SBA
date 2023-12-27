package io.github.pronze.sba.config;

import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.fix.BungeecordNPC;
import io.github.pronze.sba.utils.FirstStartConfigReplacer;
import io.github.pronze.sba.utils.Logger;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.item.builder.ItemStackFactory;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.screamingsandals.bedwars.lib.lang.I18n.i18nonly;

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

        /* To avoid config confusions */
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
            saveFile("games-inventory/triples.yml");
            saveFile("games-inventory/squads.yml");

            saveFile("shops/moved-to-bedwars.txt");

            moveFileIfNeeded("shop.yml");
            moveFileIfNeeded("upgradeShop.yml");

            saveShop("shop.yml", false);
            saveShop("upgradeShop.yml", false);

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
                    .key("acceleration-y").defValue(0.1)
                    .key("reduce-y").defValue(2.0)
                    .key("launch-multiplier").defValue(3.4)
                    .key("detection-distance").defValue(8.0D)
                    .key("fall-damage").defValue(3.0D)
                    .back()
                    .key("explosion-damage").defValue(0.25)
                    .key("running-generator-drops").defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT"))
                    .key("block-item-drops").defValue(true)
                    .key("allowed-item-drops")
                    .defValue(List.of("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT", "GOLDEN_APPLE", "OBSIDIAN",
                            "TNT"))
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
                    .back();
            generator.saveIfModified();

            if (!configurationNode.hasChild("upgrades"))
                generator.start().section("upgrades").section("limit").key("Iron").defValue(48)
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
                        .section("time")
                        .key("Diamond-II").defValue(300)
                        .key("Emerald-II").defValue(700)
                        .key("Diamond-III").defValue(1000)
                        .key("Emerald-III").defValue(1300)
                        .key("Diamond-IV").defValue(1500)
                        .key("Emerald-IV").defValue(1800)
                        .back();
            generator.saveIfModified();

            Material repeater = Material.matchMaterial("REPEATER");
            if (repeater == null)
                repeater = Material.matchMaterial("REDSTONE_WIRE");
            if (repeater == null)
                repeater = Material.matchMaterial("CAKE");
            generator.start()
                    .section("upgrades")
                    .key("timer-upgrades-enabled").defValue(true)
                    .key("show-upgrade-message").defValue(true)
                    .key("trap-detection-range").defValue(7)
                    .key("multiplier").defValue(0.25)
                    .section("limit")
                    .key("Sharpness").defValue(1)
                    .key("Protection").defValue(4)
                    .key("Efficiency").defValue(2)
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
                    .back()
                    .section("date")
                    .key("format").defValue("MM/dd/yy")
                    .back()
                    .section("lobby-scoreboard")
                    .key("enabled").defValue(true)
                    .back()
                    .section("game-scoreboard")
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
                    .key("name-provider").defValue("%player%")
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
                    .section("spectator")
                    .key("adventure-mode").defValue(false)
                    .section("teleporter")
                    .key("enabled").defValue(true)
                    .key("name").defValue("§cP§6l§ea§ay§9e§br§5s")
                    .key("material").defValue(repeater.toString())
                    .key("slot").defValue(0)
                    .back()
                    .section("tracker")
                    .key("enabled").defValue(true)
                    .key("keep-on-start").defValue(true)
                    .key("name").defValue("§cP§6l§ea§ay§9e§br§5s")
                    .key("material").defValue("COMPASS")
                    .key("slot").defValue(4)
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
                    .key("shopcosmetic").defValue("GRAY_STAINED_GLASS_PANE")
                    .key("rows").defValue(6)
                    .key("render-actual-rows").defValue(6)
                    .key("render-offset").defValue(0)
                    .key("render-header-start").defValue(9)
                    .key("render-footer-start").defValue(600)
                    .key("items-on-row").defValue(9)
                    .key("show-page-numbers").defValue(false)
                    .key("trap-title").defValue(true)
                    .key("trap-message").defValue(true)
                    .section("normal-shop")
                    .key("entity-name").defValue(List.of("§bITEM SHOP", "§e§lRIGHT CLICK"))
                    .section("skin")
                    .key("value")
                    .defValue(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYyNjA5MTkyNjQ3NCwKICAicHJvZmlsZUlkIiA6ICJiNjM2OWQ0MzMwNTU0NGIzOWE5OTBhODYyNWY5MmEwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb2JpbmhvXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iZmRmOTBkZWI0YmYzNmM3Y2I4Y2Y2Zjg0NWQ0OTYzZWVhODkyNzRlMDBkNmFjMzQxNjJiYTc3MTE1ZjMyMWZhIgogICAgfQogIH0KfQ==")
                    .key("signature")
                    .defValue(
                            "dRVORv3TXsP80Xfzy2/CYAHN92iF+4UYe8Un7jSEvCc9fwz9z39lB1ooO62hdArqZuNU2b7OKUZd8LYbctj8hUnaKdQ4kxbO1xQENRATNGsk1PWrVLgMikg2Vx4+2DCakE18f9UAVGHqGFVInI3dCCG7QmWqNI+l4g+GjxNzYVDWlW/PsB7CuQsOhJGY1hq2B1JRQ4mhZl0Tks/gU+qdw+ClOShB50KB2Q60d+fd04xYYCAJHk/a9c45EBBU8rHix8M2GV6hYXQZcdXZMB/KZBHAQfCrnlMhlTOzKfUYI5sDzSH6zBIWtE36zVeuYuM4Rppt0doT9qNJXJIYJ4UlZ11l8F9/ShQST+h138yJMgxRmIi3KAGhEJ8aVKkeeXMARbF9uFZbxHoZd66lhA5BWYsrFhyxKrPVO2AsfsfFQCY/DLurEdVVTWcN5K4Frh2Pt97gDJYBYXOuClaS367q57X76yuFqOFe6AvRI1Hvr22k8WvqpSqXzEXlfLUMwz9iKgbptS/Y9X78dseBwS7OdmUTFl1VgDZerQH1RDUrDTxr/Hiv0KE1czhbOQInRTaAT65dPB9RHZ3OnlgHcA7+7joRuPHihPuLH45NKHAxLn10CiolrtgxmGejkWqtNVKNlZNiAl49u4CRCqC13P/crCi9vlonjPg8mkLivsuyA8g=")
                    .back()
                    .key("name").defValue("[SBA] Item Shop")
                    .back()
                    .section("upgrade-shop")
                    .key("name").defValue("[SBA] Upgrade Shop")
                    .key("entity-name").defValue(List.of("§bTEAM", "§bUPGRADES", "§e§lRIGHT CLICK"))
                    .section("skin")
                    .key("value")
                    .defValue(
                            "ewogICJ0aW1lc3RhbXAiIDogMTYyNjA4ODMxNjI3OCwKICAicHJvZmlsZUlkIiA6ICIxYWZhZjc2NWI1ZGY0NjA3YmY3ZjY1ZGYzYWIwODhhOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJMb3lfQmxvb2RBbmdlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ZjM0N2NiYjg3ZmVjMDA2MDc3ZDI2MTFjZjk4MTM4NGMwOWNlM2FjNDQ1M2FlY2M4MjMzYWMwODk3YjA3ZDYwIgogICAgfQogIH0KfQ==")
                    .key("signature")
                    .defValue(
                            "oqCVAspoQ/uCoZX/2XTgoYjAGBVJSXLi+/QPHKPaGqP9zEXHE7k5TH5Z7K7x5D8ECCtZq8jW6GCzIzigvTKp2jRXoEnjnOmzc82P6nSV39NKueB6XVi12fluewaLNlzhJUwn1+7NOYlwKH3qN3/Rd8bE3lNv9bkrWN67zjnYDA7o4vxfkzgV9Hd1CEV9oVRsSne3rm7kYN1iRoMYArL4+EYTYUxd6HMUZ33b5yecQz+UctiGcRUXzPDU/RANxYlBkH6WIe6C8QH84MtjTD20X/2qmlhYeTA98Jf5eiPLfTd+30q603moUEf5VyEuaK3qxMektnaaIO0Wdx7fGYQelbDkejxqL7c//gupksKMqlFqBtLYTRcAXCS5hFbl2tnN80O4Kq0v4E1HOmBZYKZf/yYahNbRZyj0hNaG1dDdM/dqfBmBQWbcSnvb3M9YE9mXAoddryRii6kHVEQWO+C8xHECQK69AN/XhGnr+2X+cDfHHGIrVY10/rVXF2faPTauj/aFOZLp/fhOuLzOSQZYQCIe/jiO5BbEJ6owU5cyL0V/8x4609Pu7REhwiS2wDUrfLl5yyTyw232pVwO545awKV0O0/fnWeeLePuv8qBh/ngNZ52iDeEfpDeBe3DB1FFOSup96/eL/7blzDxKmzIVO29egg8xTVsYwUr23J1y0s=")
                    .back()
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
                    .key("boots").defValue(true)
                    .key("leggings").defValue(true)
                    .key("chestplate").defValue(false)
                    .key("helmet").defValue(false)
                    .section("enchants")
                    .key("sharpness").defValue(List.of("SWORD"))
                    .key("efficiency").defValue(List.of("PICK"))
                    .key("knockback").defValue(List.of("SWORD"))
                    .key("protection").defValue(List.of("HELMET", "BOOT", "CHESTPLATE", "LEGGINGS"))
                    .key("THORNS").defValue(List.of("HELMET", "BOOT", "CHESTPLATE", "LEGGINGS"))
                    .back()
                    .back()
                    .section("automatic-protection")
                    .key("spawner-diameter").defValue(5)
                    .key("team-spawn-diameter").defValue(3)
                    .key("store-diameter").defValue(3)
                    .back()
                    .section("sounds")
                    .key("on_trap_triggered").defValue("ENTITY_ENDER_DRAGON_GROWL")
                    .back()
                    .key("replace-stores-with-npc").defValue(true)
                    .key("replace-stores-with-citizen").defValue(false)
                    .section("update-checker")
                    .key("console").defValue(true)
                    .key("admins").defValue(true)
                    .back()
                    .section("ai")
                    .key("enabled").defValue(false)
                    .key("skins").defValue(List.of("robot", "artificial", "dream", "cloud", "zombie"))
                    .key("delay-in-ticks").defValue(80)
                    .key("use-stores").defValue(false)
                    .key("infinite-material").defValue("OAK_PLANKS")
                    .back();

            generator.saveIfModified();
            if (!node("debug", "enabled").getBoolean()) {
                Logger.setMode(Logger.Level.DISABLED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean trapTitleEnabled() {
        return getBoolean("shop.trap-title", true);
    }

    public boolean trapMessageEnabled() {
        return getBoolean("shop.trap-message", true);
    }

    public TeamStatusConfig teamStatus() {
        return new TeamStatusConfig();
    }

    public class TeamStatusConfig {
        /*
         * .section("team-status")
         * .key("target-destroyed").defValue("§c\u2717")
         * .key("target-exists").defValue("§a\u2713")
         * .key("alive").defValue("%color% %team% §a\u2713 §8%you%")
         * .key("destroyed").defValue("%color% %team% §a§f%players%§8 %you%")
         * .key("eliminated").defValue("%color% %team% §c\u2718 %you%")
         * .back()
         */

        public String targetDestroyed() {
            return getString("team-status.target-destroyed", "§c\u2717");
        }

        public String targetExists() {
            return getString("team-status.target-exists", "§a\u2713");
        }

        public String alive() {
            return getString("team-status.alive", "%color% %team% §a\u2713 §8%you%");
        }

        public String destroyed() {
            return getString("team-status.destroyed", "%color% %team% §a§f%players%§8 %you%");
        }

        public String eliminated() {
            return getString("team-status.eliminated", "%color% %team% §c\u2718 %you%");
        }
    }

    public boolean replaceStoreWithNpc() {
        return node("replace-stores-with-npc").getBoolean(true);
    }

    public boolean replaceStoreWithCitizen() {
        return node("replace-stores-with-citizen").getBoolean(false)
                && SBA.getInstance().citizensFix.canEnable();
    }

    public PartyConfig party() {
        return new PartyConfig();
    }

    public class PartyConfig {
        public boolean enabled() {
            return getBoolean("party.enabled", false);
        }

        public boolean autojoin() {
            return getBoolean("party.leader-autojoin-autoleave", false);
        }

        public int expirationTime() {
            return getInt("party.invite-expiration-time", 60);
        }
        /*
         * .section("party")
         * .key("enabled").defValue(true)
         * .key("leader-autojoin-autoleave").defValue(true)
         * .key("invite-expiration-time").defValue(60)
         * .back()
         */
    }

    public SpectatorConfig spectator() {
        return new SpectatorConfig();
    }

    // "fake-spectator"
    public class SpectatorConfig {
        public boolean adventure() {
            return getBoolean("spectator.adventure-mode", false);
        }

        public TeleporterConfig teleporter() {
            return new TeleporterConfig();
        }

        public class TeleporterConfig {
            public boolean enabled() {
                return getBoolean("spectator.teleporter.enabled", false);
            }

            public String name() {
                return getString("spectator.teleporter.name", "§cP§6l§ea§ay§9e§br§5s");
            }

            public String material() {
                return getString("spectator.teleporter.material", "CAKE");
            }

            public int slot() {
                return getInt("spectator.teleporter.slot", 0);
            }

            public ItemStack get() {
                ItemStack compass = new ItemStack(Material.matchMaterial(material()));
                var meta = compass.getItemMeta();
                meta.setDisplayName(name());
                compass.setItemMeta(meta);
                return compass;
            }

        }

        public TrackerConfig tracker() {
            return new TrackerConfig();
        }

        public class TrackerConfig {
            public boolean enabled() {
                return getBoolean("spectator.tracker.enabled", false);
            }

            public boolean keepOnStart() {
                return getBoolean("spectator.tracker.keep-on-start", true);
            }

            public String name() {
                return getString("spectator.tracker.name", "§cP§6l§ea§ay§9e§br§5s");
            }

            public String material() {
                return getString("spectator.tracker.material", "COMPASS");
            }

            public int slot() {
                return getInt("spectator.tracker.slot", 4);
            }

            public ItemStack get() {
                ItemStack compass = new ItemStack(Material.matchMaterial(material()));
                var meta = compass.getItemMeta();
                meta.setDisplayName(name());
                compass.setItemMeta(meta);
                return compass;
            }

        }

        public LeaveItem leave() {
            return new LeaveItem();
        }

        public class LeaveItem {
            public int position() {
                return Main.getConfigurator().config.getInt("hotbar.leave", 8);
            }

            public ItemStack get() {
                ItemStack leave = Main.getConfigurator().readDefinedItem("leavegame", "SLIME_BALL");
                ItemMeta leaveMeta = leave.getItemMeta();
                leaveMeta.setDisplayName(i18nonly("leave_from_game_item", "Leave game"));
                leave.setItemMeta(leaveMeta);
                return leave;
            }
        }

        public boolean compassWhileSpectator() {
            return getBoolean("spectator.compass-spectator", true);
        }
        /*
         * .section("compass")
         * .key("enabled").defValue(true)
         * .key("name").defValue("Players")
         * .back()
         */
    }

    public UpgradeConfig upgrades() {
        return new UpgradeConfig();
    }

    public class UpgradeConfig {
        /*
         * .section("upgrade-item")
         * .key("leggings").defValue(true)
         * .key("chestplate").defValue(false)
         */
        public boolean boots() {
            return getBoolean("upgrade-item.boots", true);
        }

        public boolean leggings() {
            return getBoolean("upgrade-item.leggings", true);
        }

        public boolean chestplate() {
            return getBoolean("upgrade-item.chestplate", false);
        }

        public boolean helmet() {
            return getBoolean("upgrade-item.helmet", false);
        }

        public EnchantApplyConfig enchants() {
            return new EnchantApplyConfig();
        }

        public class EnchantApplyConfig {
            public List<String> keys() {
                var keys = AddonAPI
                        .getInstance()
                        .getConfigurator().getSubKeys("upgrade-item.enchants");
                return keys;
            }

            public List<String> sharpness() {
                return getStringList("upgrade-item.enchants.sharpness");
            }

            public List<String> knockback() {
                return getStringList("upgrade-item.enchants.knockback");
            }

            public List<String> protection() {
                return getStringList("upgrade-item.enchants.protection");
            }

            public List<String> efficiency() {
                return getStringList("upgrade-item.enchants.efficiency");
            }

            public List<String> of(String s) {
                return getStringList("upgrade-item.enchants." + s);
            }
        }
    }

    public AIConfig ai() {
        return new AIConfig();
    }

    private boolean aiDisabled = false;

    public class AIConfig {

        public boolean enabled() {
            return !aiDisabled && getBoolean("ai.enabled", false);
        }

        public String skin() {
            var lst = new ArrayList<>(getStringList("ai.skins"));
            Collections.shuffle(lst);
            return lst.get(0);
        }

        public long delay() {
            return getInt("ai.delay-in-ticks", 80);
        }

        public boolean useStores() {
            return getBoolean("ai.use-stores", false);
        }

        public @NotNull String infiniteItem() {
            String defaultMaterial = "STONE";
            if (Material.getMaterial("OAK_PLANKS") != null)
                defaultMaterial = "OAK_PLANKS";
            String returnValue =getString("ai.infinite-material", defaultMaterial);
            if(Material.getMaterial(returnValue)==null)
            {
                return defaultMaterial;
            }
            return returnValue;
        }

        public void disable() {
            aiDisabled = true;
        }

    }

    public boolean shouldCheckUpdate() {
        return shouldWarnConsoleAboutUpdate() || shouldWarnPlayerAboutUpdate();
    }

    public boolean shouldWarnConsoleAboutUpdate() {
        return getBoolean("update-checker.console", true);
    }

    public boolean shouldWarnPlayerAboutUpdate() {
        return getBoolean("update-checker.admins", true);
    }

    private void moveFileIfNeeded(String path) {
        var path1 = Bukkit.getPluginManager().getPlugin("SBA").getDataFolder().toPath().resolve("shops/" + path);
        var path2 = SBA.getBedwarsPlugin().getDataFolder().toPath().resolve(path);
        if (path1.toFile().exists())
            if (!path2.toFile().exists() || path1.toFile().lastModified() > path2.toFile().lastModified())
                try {
                    Files.copy(
                            path1,
                            path2,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    Logger.error("Could not copy file {} from SBA/shops/{} to Bedwars/{}", path);
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

    public void saveShop(String fileName, boolean force) {

        var path2 = SBA.getBedwarsPlugin().getDataFolder().toPath().resolve(fileName);

        if (!path2.toFile().exists() || force)
            try (var input = SBAConfig.class.getResourceAsStream("/shops/" + fileName)) {
                System.out.println("Saving shop '" + fileName + "' at '" + path2 + "'");
                try (var output = new FileOutputStream(path2.toFile())) {
                    if (input != null)
                        input.transferTo(output);
                }
            } catch (IOException e) {
                Logger.error("Could not save store {} due to {}", fileName, e);
            }

    }

    @Override
    public void upgrade() {
        try {
            node("version").set(plugin.getDescription().getVersion());
            saveConfig();

            moveFileIfNeeded("shop.yml");
            moveFileIfNeeded("upgradeShop.yml");

            saveShop("shop.yml", true);
            saveShop("upgradeShop.yml", true);

            final var langFiles = langFolder.listFiles();
            if (langFiles != null)
                Arrays.stream(langFiles).forEach(File::delete);

            saveFile("languages/language_en.yml");
            ServiceManager.get(FirstStartConfigReplacer.class).updateBedWarsConfig();
            SBAUtil.reloadPlugin(Main.getInstance(), null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
    public List<String> getSubKeys(String string) {
        try {
            return node((Object[]) string.split("\\.")).childrenMap().keySet().stream().map(o -> o.toString())
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return List.of();
    }

    public void set(String path, Object value) {
        try {
            node(path).set(value);
            generator.saveIfModified();
        } catch (Throwable e) {
            e.printStackTrace();
        }
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

    public org.screamingsandals.lib.item.ItemStack readDefinedItem(ConfigurationNode node, String def) {
        if (!node.empty()) {
            var obj = node.raw();
            return Objects.requireNonNullElse(ItemStackFactory.build(obj), ItemStackFactory.getAir());
        }

        return Objects.requireNonNullElse(ItemStackFactory.build(def), ItemStackFactory.getAir());
    }

}
