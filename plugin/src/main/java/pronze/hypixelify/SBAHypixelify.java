package pronze.hypixelify;

import org.bukkit.event.HandlerList;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.manager.PartyManager;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.BWACommand;
import pronze.hypixelify.commands.PartyCommand;
import pronze.hypixelify.commands.ShoutCommand;
import pronze.hypixelify.inventories.CustomShop;
import pronze.hypixelify.inventories.GamesInventory;
import pronze.hypixelify.game.RotatingGenerators;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import pronze.hypixelify.game.Arena;
import pronze.hypixelify.lib.lang.I18n;
import pronze.hypixelify.placeholderapi.SBAExpansion;
import pronze.hypixelify.service.PlayerWrapperService;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.nms.utils.ClassStorage;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import pronze.hypixelify.listener.*;
import pronze.lib.scoreboards.ScoreboardManager;

import java.util.*;

public class SBAHypixelify extends JavaPlugin implements SBAHypixelifyAPI {
    private static SBAHypixelify plugin;

    public static Optional<GameStorage> getStorage(Game game) {
        if (plugin.arenas.containsKey(game.getName()))
            return Optional.ofNullable(plugin.arenas.get(game.getName()).getStorage());

        return Optional.empty();
    }

    public static Arena getArena(String arenaName) { return plugin.arenas.get(arenaName); }

    public static void addArena(Arena arena) { plugin.arenas.put(arena.getGame().getName(), arena); }

    public static void removeArena(String arenaName) { plugin.arenas.remove(arenaName); }

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }

    public static boolean isProtocolLib() { return plugin.protocolLib; }

    public static PartyManager getPartyManager() {
        return plugin.partyManager;
    }

    public static SBAHypixelify getInstance() {
        return plugin;
    }

    public static GamesInventory getGamesInventory() {
        return plugin.gamesInventory;
    }

    public static PlayerWrapperService getWrapperService() {
        return plugin.playerWrapperService;
    }

    public static boolean isUpgraded() {
        return !Objects.requireNonNull(getConfigurator()
                .config.getString("version")).contains(SBAHypixelify.getInstance().getVersion());
    }

    private CustomShop shop;
    private String version;
    private PlayerWrapperService playerWrapperService;
    private pronze.hypixelify.manager.PartyManager partyManager;
    private Configurator configurator;
    private GamesInventory gamesInventory;
    private boolean debug = false;
    private boolean protocolLib;
    private boolean isSnapshot;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final List<Listener> registeredListeners = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        version = this.getDescription().getVersion();
        isSnapshot = version.toLowerCase().contains("snapshot");
        protocolLib = plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib");

        if (getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (!Main.getVersion().contains("0.3.")) {
            showErrorMessage("You need at least a minimum of 0.3.0 version of Screaming-BedWars to run SBAHypixelify!",
                    "Get the latest version from here: https://ci.screamingsandals.org/job/BedWars-0.x.x/");
            return;
        }

        if (!ClassStorage.IS_SPIGOT_SERVER) {
            showErrorMessage("Did not detect spigot",
                    "Make sure you have spigot installed to run this plugin");
            return;
        }

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9, please upgrade!");
            return;
        }

        /* initialize our custom ScoreboardManager library*/
        ScoreboardManager.init(this);

        UpdateChecker.run(this);

        configurator = new Configurator(this);
        configurator.loadDefaults();

        Logger.init(configurator.config.getBoolean("debug.enabled", false));
        I18n.load(this, configurator.config.getString("locale"));
        
        playerWrapperService = new PlayerWrapperService();
        debug = configurator.config.getBoolean("debug.enabled", false);

        InventoryListener.init(this);
        shop = new CustomShop();
        gamesInventory = new GamesInventory();

        partyManager = new pronze.hypixelify.manager.PartyManager();

        registerCommand("party", new PartyCommand());
        registerCommand("shout", new ShoutCommand());
        registerCommand("bwaddon", new BWACommand());

        registerListener(new BedwarsListener());
        registerListener(new ChatListener());
        registerListener(new PartyListener());
        registerListener(new PlayerListener());
        registerListener(new TeamUpgradeListener());
       // pluginManager.registerEvents(new DragonListener(), this);

        if (configurator.config.getBoolean("main-lobby.enabled", false))
            registerListener(new MainLobbyBoard());
        if (SBAHypixelify.getConfigurator().config.getBoolean("lobby-scoreboard.enabled", true))
            registerListener(new LobbyScoreboard());

        registerListener(gamesInventory);
        registerListener(shop);

        //Do changes for legacy support.
        changeBedWarsConfig();
        final var pluginManager = Bukkit.getServer().getPluginManager();

        try {
            if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
                new SBAExpansion().register();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if(configurator.config.getBoolean("floating-generator.enabled", true)) {
            if(Main.getConfigurator().config.getBoolean("spawner-holograms", true)){
                Main.getConfigurator().config.set("spawner-holograms", false);
                Main.getConfigurator().saveConfig();
                pluginManager.disablePlugin(Main.getInstance());
                pluginManager.enablePlugin(Main.getInstance());
                return;
            }
            RotatingGenerators.format = SBAHypixelify.getConfigurator()
                    .getStringList("floating-generator.holo-text");
            SBAUtil.destroySpawnerArmorStandEntities();
        }

        Logger.trace( "Registering API service provider");
        getServer().getServicesManager()
                .register(SBAHypixelifyAPI.class, this, this, ServicePriority.Normal);
        getLogger().info("Plugin has loaded!");
    }

    public void registerListener(Listener listener){
        final var plugMan = Bukkit.getServer().getPluginManager();
        plugMan.registerEvents(listener, this);
        Logger.trace( "Registered Listener: {}", listener.getClass().getSimpleName());
        registeredListeners.add(listener);
    }

    protected void showErrorMessage(String... messages) {
        getLogger().severe("======PLUGIN ERROR===========");
        getLogger().severe("Plugin: SBAHypixelify is being disabled for the following error:");
        Arrays.stream(messages)
                .filter(Objects::nonNull)
                .forEach(getLogger()::severe);
        getLogger().severe("=============================");
        getServer().getPluginManager().disablePlugin(this);
    }

    public void registerCommand(String commandName, CommandExecutor executor) {
        var pluginCommand = getCommand(commandName);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(executor);
        }
    }

    public void changeBedWarsConfig() {
        //Do changes for legacy support.
        if (Main.isLegacy()) {
            boolean doneChanges = false;
            if (Objects.requireNonNull(Main.getConfigurator().config.getString("items.leavegame")).equalsIgnoreCase("RED_BED")) {
                Main.getConfigurator().config.set("items.leavegame", "BED");
                doneChanges = true;
            }
            if (Objects.requireNonNull(Main.getConfigurator()
                    .config.getString("items.shopcosmetic")).equalsIgnoreCase("GRAY_STAINED_GLASS_PANE")) {
                Main.getConfigurator().config.set("items.shopcosmetic", "STAINED_GLASS_PANE");
                doneChanges = true;
            }

            //TODO: test scoreboard for 1.9.4

            if (doneChanges) {
                getLogger().info("[SBAHypixelify]: Making legacy changes");
                Main.getConfigurator().saveConfig();
                Bukkit.getServer().getPluginManager().disablePlugin(this);
                Bukkit.getServer().getPluginManager().enablePlugin(this);
            }
        }
    }

    @Override
    public void onDisable() {
        if (SBAHypixelify.isProtocolLib()) {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(SBAUtil::removeScoreboardObjective);
        }

        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();

        RotatingGenerators.destroy(RotatingGenerators.cache);

        if (gamesInventory != null)
            gamesInventory.destroy();

        if(shop != null)
            shop.destroy();

        Logger.trace("Cancelling tasks...");
        this.getServer().getScheduler().cancelTasks(plugin);
        this.getServer().getServicesManager().unregisterAll(plugin);
        arenas.clear();
    }

    /*
    ======================
     * API implementations
     =====================
     */

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(String key, T def) {
        try {
            return (T) getConfigurator().config.get(key, def);
        } catch (Throwable t) {
            return def;
        }
    }

    @Override
    public String getVersion() { return plugin.version; }

    @Override
    public boolean isInParty(Player player) {
        return partyManager.isInParty(player);
    }

    @Override
    public Optional<Party> getParty(Player player) {
        return Optional.ofNullable(partyManager.getParty(player));
    }

    @Override
    public Optional<pronze.hypixelify.api.game.GameStorage> getGameStorage(Game game){
        return SBAHypixelify.getStorage(game);
    }

    @Override
    public PlayerWrapper getPlayerWrapper(Player player) {
        return playerWrapperService.getWrapper(player);
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public boolean isSnapshot() {
        return isSnapshot;
    }
}


