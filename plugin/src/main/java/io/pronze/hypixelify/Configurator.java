package io.pronze.hypixelify;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import io.pronze.hypixelify.listener.LobbyScoreboard;
import org.screamingsandals.bedwars.Main;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Configurator {

    public static HashMap<String, List<String>> Scoreboard_Lines;
    public static List<String> overstats_message;
    public static List<String> gamestart_message;
    public static HashMap<String, Integer> game_size;
    public static String date;
    public static boolean tag_health;
    public final File dataFolder;
    public final SBAHypixelify main;
    public File file, shopFile, upgradeShop, legacyShop, legacyUpgradeShop;
    public FileConfiguration config;

    public Configurator(SBAHypixelify main) {
        this.dataFolder = main.getDataFolder();
        this.main = main;
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

    public void loadDefaults() {
        dataFolder.mkdirs();

        file = new File(dataFolder, "bwaconfig.yml");
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


        shopFile = new File(dataFolder, "shop.yml");
        upgradeShop = new File(dataFolder, "upgradeShop.yml");
        legacyShop = new File(dataFolder, "legacy-shop.yml");
        legacyUpgradeShop = new File(dataFolder, "legacy-upgradeShop.yml");

        if (!shopFile.exists()) {
            main.saveResource("shop.yml", false);
        }

        if (!upgradeShop.exists()) {
            main.saveResource("upgradeShop.yml", false);
        }

        if (!legacyShop.exists()) {
            main.saveResource("legacy-shop.yml", false);
        }

        if(!legacyUpgradeShop.exists()){
            main.saveResource("legacy-upgradeShop.yml", false);
        }

        AtomicBoolean modify = new AtomicBoolean(false);

        checkOrSetConfig(modify, "debug.enabled", false);

        checkOrSetConfig(modify, "permanent-items", true);

        checkOrSetConfig(modify, "store.replace-store-with-hypixelstore", true);
        checkOrSetConfig(modify, "running-generator-drops", Arrays.asList("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT"));
        checkOrSetConfig(modify, "block-item-drops", true);
        checkOrSetConfig(modify, "allowed-item-drops", Arrays.asList("DIAMOND", "IRON_INGOT", "EMERALD", "GOLD_INGOT", "GOLDEN_APPLE", "ENDER_PEAL", "OBSIDIAN", "TNT"));
        checkOrSetConfig(modify, "give-killer-resources", true);
        checkOrSetConfig(modify, "remove-sword-on-upgrade", true);
        checkOrSetConfig(modify, "block-players-putting-certain-items-onto-chest", true);
        checkOrSetConfig(modify, "disable-armor-inventory-movement", true);
        checkOrSetConfig(modify, "version", SBAHypixelify.getVersion());
        checkOrSetConfig(modify, "autoset-bw-config", true);
        checkOrSetConfig(modify, "floating-generator.enabled", true);
        checkOrSetConfig(modify, "floating-generator.holo-height", 2.0);
        checkOrSetConfig(modify, "floating-generator.item-height", 0.25);

        checkOrSetConfig(modify, "upgrades.timer-upgrades-enabled", true);
        checkOrSetConfig(modify, "upgrades.show-upgrade-message", true);
        checkOrSetConfig(modify, "upgrades.trap-detection-range", 7);
        checkOrSetConfig(modify, "upgrades.multiplier", 0.25);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-I", 4);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-II", 8);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-III", 12);
        checkOrSetConfig(modify, "upgrades.prices.Sharpness-Prot-IV", 16);
        checkOrSetConfig(modify, "upgrades.time.Diamond-I", 120);
        checkOrSetConfig(modify, "upgrades.time.Emerald-I", 200);
        checkOrSetConfig(modify, "upgrades.time.Diamond-II", 400);
        checkOrSetConfig(modify, "upgrades.time.Emerald-II", 520);
        checkOrSetConfig(modify, "upgrades.time.Diamond-III", 700);
        checkOrSetConfig(modify, "upgrades.time.Emerald-III", 900);
        checkOrSetConfig(modify, "upgrades.time.Diamond-IV", 1100);
        checkOrSetConfig(modify, "upgrades.time.Emerald-IV", 1200);
        checkOrSetConfig(modify, "date.format", "MM/dd/yy");
        checkOrSetConfig(modify, "lobby-scoreboard.solo-prefix", "Solo");
        checkOrSetConfig(modify, "lobby-scoreboard.doubles-prefix", "Doubles");
        checkOrSetConfig(modify, "lobby-scoreboard.triples-prefix", "Triples");
        checkOrSetConfig(modify, "lobby-scoreboard.squads-prefix", "Squads");
        checkOrSetConfig(modify, "lobby-scoreboard.enabled", true);
        checkOrSetConfig(modify, "lobby-scoreboard.interval", 2);
        checkOrSetConfig(modify, "lobby-scoreboard.state.waiting", "&fWaiting...");
        checkOrSetConfig(modify, "first_start", true);
        checkOrSetConfig(modify, "shout.time-out", 60);
        checkOrSetConfig(modify, "message.waiting", "&fWaiting...");
        checkOrSetConfig(modify, "message.game-starts-in", "&eThe game starts in &c{seconds} &eseconds");
        checkOrSetConfig(modify, "message.bed-destroyed.title", "&c&lBED DESTROYED!");
        checkOrSetConfig(modify, "message.bed-destroyed.sub-title", "You will no longer respawn!");
        checkOrSetConfig(modify, "message.already-purchased", "&c&lYou already purchased the same {thing}");
        checkOrSetConfig(modify, "message.wait-trap", "&cYou already purchased this upgrade!, wait for it to wear out");
        checkOrSetConfig(modify, "message.upgrade-team-protection", "&o&c{player}&e has upgraded team protection");
        checkOrSetConfig(modify, "message.error-occured", "&c&lAN ERROR HAS OCCURED");
        checkOrSetConfig(modify, "message.greatest-enchantment", "&c&lYou Already have the greatest enchantment");
        checkOrSetConfig(modify, "message.generator-upgrade", "{MatName} generator has been upgraded to {tier}");
        checkOrSetConfig(modify, "message.shout-format", "&6[SHOUT] {color}[{team}]&r {player}&7: &r{message}");
        checkOrSetConfig(modify, "message.trap-triggered.title", "&cTrap Triggered!");
        checkOrSetConfig(modify, "message.trap-triggered.sub-title", "&eSomeone has entered your base!");
        checkOrSetConfig(modify, "message.blindness-trap-purchased-title", "&6Blindness Trap has been purchased!");
        checkOrSetConfig(modify, "message.purchase-heal-pool", "&bHeal Pool has been purchased by: {player}");
        checkOrSetConfig(modify, "message.cannot-buy", "&cYou don't have enough {price}");
        checkOrSetConfig(modify, "message.purchase", "&aYou purchased &e{item}");
        checkOrSetConfig(modify, "message.respawn-title", "&cYOU DIED!");
        checkOrSetConfig(modify, "message.respawn-subtitle", "&eYou will respawn in &c%time% &eseconds");
        checkOrSetConfig(modify, "message.respawned-title", "&eYou have respawned");
        checkOrSetConfig(modify, "message.cannot-put-item-on-chest", "You cannot put this item onto this chest.");
        checkOrSetConfig(modify, "disable-sword-armor-damage", true);
        checkOrSetConfig(modify, "shop-name", "[SBAHypixelify] shop");
        checkOrSetConfig(modify, "games-inventory.enabled", true);
        checkOrSetConfig(modify, "games-inventory.stack-material", "PAPER");
        checkOrSetConfig(modify, "games-inventory.stack-lore",
                Arrays.asList("&8{mode}", "", "&7Available Servers: &a1", "&7Status: &a{status}"
                        , "&7Players:&a {players}", "", "&aClick to play", "&eRight click to toggle favorite!"));
        checkOrSetConfig(modify, "message.upgrade", "Upgrade: ");
        checkOrSetConfig(modify, "games-inventory.gui.solo-prefix", "Bed Wars Solo");
        checkOrSetConfig(modify, "games-inventory.gui.double-prefix", "Bed Wars Doubles");
        checkOrSetConfig(modify, "games-inventory.gui.triple-prefix", "Bed Wars Triples");
        checkOrSetConfig(modify, "games-inventory.gui.squad-prefix", "Bed Wars Squads");
        checkOrSetConfig(modify, "games-inventory.back-item.name", "&aGo Back");
        checkOrSetConfig(modify, "games-inventory.back-item.lore", Arrays.asList("&7To Play Bed Wars"));
        checkOrSetConfig(modify, "games-inventory.firework-name", "&aRandom Map");
        checkOrSetConfig(modify, "games-inventory.firework-lore", Arrays.asList("&8{mode}", "", "&7Map Selections: &a{games}", "", "&aClick to Play"));

        checkOrSetConfig(modify, "games-inventory.diamond-name", "&aRandom Favorite");
        checkOrSetConfig(modify, "games-inventory.oak_sign-name", "&aMap Selector ({mode})");
        checkOrSetConfig(modify, "games-inventory.oak_sign-lore", Arrays.asList(
                "&7Pick which map you want to play"
                , "&7from a list of available servers."
                , " "
                , "&eClick to browse!"));
        checkOrSetConfig(modify, "games-inventory.barrier-name", "&cExit");
        checkOrSetConfig(modify, "games-inventory.ender_pearl-name", "&cClick here to rejoin!");
        checkOrSetConfig(modify, "games-inventory.ender_pearl-lore", Arrays.asList("&7Click here to rejoin the lastly joined game"));

        checkOrSetConfig(modify, "games-inventory.bed-name", "&aBed Wars ({mode})");
        checkOrSetConfig(modify, "games-inventory.bed-lore", Arrays.asList("&7Play Bed Wars {mode}", " ", "&eClick to play!"));
        for (String game : Main.getGameNames()) {
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
        checkOrSetConfig(modify, "scoreboard.you", "&7YOU");
        checkOrSetConfig(modify, "scoreboard.lines.default", Arrays.asList(
                "&7{date}"
                , ""
                , "{tier}"
                , ""
                , "{team_status}"
                , ""
                , "&fKills: &a{kills}"
                , "&fBed Broken: &a{beds}"
                , ""
                , "&ewww.minecraft.net"
        ));
        checkOrSetConfig(modify, "scoreboard.lines.5", Arrays.asList(
                "&7{date}"
                , ""
                , "{tier}"
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
                , "&fMode: &a{mode}"
                , "&fVersion: &7v1.1"
                , ""
                , "&ewww.sample.net"
        ));

        checkOrSetConfig(modify, "party.leader-autojoin-autoleave", true);
        checkOrSetConfig(modify, "party.size", 4);
        checkOrSetConfig(modify, "party.message.cannotinvite", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou cannot invite this player to your party!",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.no-other-commands", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou cannot do other commands now",
                "&6-----------------------------------------------------"
        ));
        checkOrSetConfig(modify, "party.message.leader-join-leave", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou have been teleported by the party leader",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.expired", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cThe party invite has been expired",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.invalid-command", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cInvalid command, do /p help for more.",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.access-denied", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou cannot access this command",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.notinparty", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou are currently not in a party!",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.invited", Arrays.asList(
                "&6-----------------------------------------------------",
                "&eYou have invited {player}&e to your party!",
                "&ewait for them to accept it",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.alreadyInvited", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cThis player has already had pending invites!",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.warp", Arrays.asList(
                "&6-----------------------------------------------------",
                "&eYou have been warped by the leader",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.warping", Arrays.asList(
                "&6-----------------------------------------------------",
                "&eWarping players..",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.invite", Arrays.asList(
                "&6-----------------------------------------------------",
                "{player}&e has invited you to join their party!",
                "&eType /party accept to join. You have 60 seconds to accept.",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.accepted", Arrays.asList(
                "&6-----------------------------------------------------",
                "{player} &ajoined the party!",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.offline-left", Arrays.asList(
                "&6-----------------------------------------------------",
                "{player} &aleft the party due to inactivity",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.left", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou left the party!",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.offline-quit", Arrays.asList(
                "&6-----------------------------------------------------",
                "{player} &cleft the party",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.declined", Arrays.asList(
                "&6-----------------------------------------------------",
                "{player} &chas declined this party invite",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.kicked", Arrays.asList(
                "&6-----------------------------------------------------",
                "{player} &cHas been kicked from party",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.disband-inactivity", Arrays.asList(
                "&6-----------------------------------------------------",
                "&aParty has been disbanded due to inactivity",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.disband", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cParty has been disbanded by the leader.",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.player-not-found", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cCould not find Player!",
                "&6-----------------------------------------------------"));
        checkOrSetConfig(modify, "party.message.cannot-blank-yourself", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou can't {blank} yourself.",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.cannot-invite-yourself", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou can't invite yourself.",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.not-invited", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou are not invited to any party",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.got-kicked", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou have been kicked from party",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.decline-inc", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cTo invite, you must decline current invites.",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.declined-user", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou declined the invite!",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "message.not-in-game", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou are not in a game to do this command!",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "message.shout-wait", Arrays.asList(
                "&6-----------------------------------------------------",
                "&cYou have to wait {seconds} seconds before doing this command!",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.chat-enable-disabled", Arrays.asList(
                "&6-----------------------------------------------------",
                "&aParty chat has been {mode}",
                "&6-----------------------------------------------------"));

        checkOrSetConfig(modify, "party.message.help", Arrays.asList(
                "&1-----------------------------------------------------",
                "&6Party commands",
                "&e/p accept <player>&7 - Accept a party invite from a player",
                "&e/p invite <player>&7 - Invite another player to your party",
                "&e/p list&7 - Lists the players in your current party",
                "&e/p leave&7 - Leaves your current party",
                "&1-----------------------------------------------------"));
        checkOrSetConfig(modify, "main-lobby.enabled", false);
        checkOrSetConfig(modify, "main-lobby.custom-chat", true);
        checkOrSetConfig(modify, "main-lobby.chat-format", "{color}[{level}✫] {name}: {message}");
        checkOrSetConfig(modify, "main-lobby.lines", Arrays.asList(
                "",
                "Your Level: &a{level}",
                "",
                "Progress: {progress}",
                "{bar}",
                "",
                "Loot Chests: &60",
                "",
                "Total Kills: &a{kills}",
                "Total Wins: &a{deaths}",
                "",
                "K/D ratio: &6{k/d}",
                "",
                "&ewww.minecraft.net"
        ));

        checkOrSetConfig(modify, "commands.invalid-command",
                "[SBAHypixelify] &cUnknown command, do /bwaddon help for more.");

        checkOrSetConfig(modify, "commands.player-only", "[SBAHypixelify] &cOnly players can use this command!");
        checkOrSetConfig(modify, "commands.no-permissions", "[SBAHypixelify]&cYou do not have permissions to do this command!");

        if (modify.get()) {
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String str = config.getString("games-inventory.stack-material");
            assert str != null;
            if (str.toLowerCase().contains("bed") || str.toLowerCase().contains("rocket") || str.toLowerCase().contains("sign")
                    || str.toLowerCase().contains("pearl")) {
                config.set("games-inventory.stack-material", "PAPER");
                saveConfig();
            }
            Material mat = Material.valueOf(config.getString("games-inventory.stack-material"));
        } catch (Exception ignored) {
            config.set("games-inventory.stack-material", "PAPER");
            saveConfig();
        }

        date = config.getString("date.format");
        Scoreboard_Lines = new HashMap<>();
        for (String key : Objects.requireNonNull(config.getConfigurationSection("scoreboard.lines")).getKeys(false))
            Scoreboard_Lines.put(key,
                    LobbyScoreboard.listColor(getStringList("scoreboard.lines." + key)));

        game_size = new HashMap<>();
        for (String s : Main.getGameNames()) {
            int size = config.getInt("lobby-scoreboard.player-size.games." + s, 4);
            game_size.put(s, size);
        }

        tag_health = SBAHypixelify.getConfigurator().config.getBoolean("tag_health");
        overstats_message = LobbyScoreboard.listColor(getStringList("overstats.message"));
        gamestart_message = LobbyScoreboard.listColor(getStringList("game-start.message"));


        if (config.getBoolean("first_start")) {
            Bukkit.getLogger().info("[SBAHypixelify]: &a Detected first start");
            upgradeCustomFiles();
            config.set("first_start", false);
            saveConfig();
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("BedWars");
            if (plugin != null) {
                Bukkit.getServer().getPluginManager().disablePlugin(plugin);
                Bukkit.getServer().getPluginManager().enablePlugin(plugin);
                Bukkit.getLogger().info("[SBAHypixelify]: &aMade changes to the config.yml file!");
            }
        }
    }

    public void upgradeCustomFiles() {

        config.set("version", SBAHypixelify.getVersion());
        config.set("autoset-bw-config", false);
        saveConfig();

        File file2 = new File(dataFolder, "config.yml");
        main.saveResource("config.yml", true);
        main.saveResource("shop.yml", true);
        main.saveResource("upgradeShop.yml", true);
        main.saveResource("legacy-shop.yml", true);
        main.saveResource("legacy-upgradeShop.yml", true);
        try {
            Main.getConfigurator().config.load(file2);
            Main.getConfigurator().saveConfig();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }


        Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
        Bukkit.getServer().getPluginManager().enablePlugin(Main.getInstance());

    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<String> getStringList(String string) {
        List<String> list = new ArrayList<>();
        for (String s : config.getStringList(string)) {
            s = ChatColor.translateAlternateColorCodes('&', s);
            list.add(s);
        }
        return list;
    }

    public String getString(String path) {
        if (config.isSet(path) && config.getString(path) != null) {
            return ChatColor.translateAlternateColorCodes('&', config.getString(path));
        }


        return null;
    }

    public String getString(String path, String def) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, def));
    }


    private void checkOrSetConfig(AtomicBoolean modify, String path, Object value) {
        checkOrSet(modify, this.config, path, value);
    }


}