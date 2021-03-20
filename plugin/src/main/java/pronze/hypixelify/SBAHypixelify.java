package pronze.hypixelify;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.ext.bstats.bukkit.Metrics;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.ScoreboardManager;
import org.screamingsandals.bedwars.lib.nms.utils.ClassStorage;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import pronze.hypixelify.api.config.ConfiguratorAPI;
import pronze.hypixelify.api.exception.ExceptionHandler;
import pronze.hypixelify.api.manager.ArenaManager;
import pronze.hypixelify.api.manager.PartyManager;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.exception.ExceptionManager;
import pronze.hypixelify.game.ArenaManagerImpl;
import pronze.hypixelify.party.PartyManagerImpl;
import pronze.hypixelify.listener.ShopInventoryListener;
import pronze.hypixelify.inventories.GamesInventory;
import pronze.hypixelify.lib.lang.I18n;
import pronze.hypixelify.listener.*;
import pronze.hypixelify.placeholderapi.SBAExpansion;
import pronze.hypixelify.scoreboard.LobbyScoreboardManagerImpl;
import pronze.hypixelify.scoreboard.MainLobbyScoreboardImpl;
import pronze.hypixelify.service.PlayerWrapperService;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;

import java.util.*;

import static pronze.hypixelify.utils.MessageUtils.showErrorMessage;

public class SBAHypixelify extends JavaPlugin implements SBAHypixelifyAPI {
    private static SBAHypixelify plugin;
    private final List<Listener> registeredListeners = new ArrayList<>();
    private ExceptionManager exceptionManager;
    private String version;
    private ArenaManagerImpl arenaManager;
    private PlayerWrapperService playerWrapperService;
    private Configurator configurator;
    private PartyManagerImpl partyManager;
    private boolean debug = false;
    private boolean isSnapshot;
    private GamesInventory gamesInventory;

    public static SBAHypixelify getInstance() {
        return plugin;
    }

    public static Configurator getConfigurator() {
        return plugin.configurator;
    }

    public static ExceptionManager getExceptionManager() {
        return plugin.exceptionManager;
    }

    @Override
    public void onEnable() {
        exceptionManager = new ExceptionManager();
        plugin = this;
        version = this.getDescription().getVersion();
        isSnapshot = version.toLowerCase().contains("snapshot");
        Logger.init(false);

        if (getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (!Main.getVersion().contains("0.3.")) {
            showErrorMessage("You need at least a minimum of 0.3.0 snapshot 709+ version" +
                            " of Screaming-BedWars to run SBAHypixelify v2.0!",
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

        new CommandManager().init(this);
        debug = configurator.config.getBoolean("debug.enabled", false);
        Logger.init(debug);
        I18n.load(this, configurator.config.getString("locale"));

        playerWrapperService = new PlayerWrapperService();
        partyManager = new PartyManagerImpl();


        gamesInventory = new GamesInventory();
        gamesInventory.loadInventory();

        arenaManager = new ArenaManagerImpl();

        registerListener(new ShopInventoryListener());
        registerListener(new BedWarsListener());
        registerListener(new PlayerListener());
        registerListener(new TeamUpgradeListener());

        if (configurator.config.getBoolean("main-lobby.enabled", false))
            registerListener(new MainLobbyScoreboardImpl());
        if (SBAHypixelify.getConfigurator().config.getBoolean("lobby-scoreboard.enabled", true))
            registerListener(new LobbyScoreboardManagerImpl());

        final var pluginManager = Bukkit.getServer().getPluginManager();
        try {
            if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
                new SBAExpansion().register();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        new Metrics(this, 79505);
        Logger.trace("Registering API service provider");

        getServer().getServicesManager().register(
                SBAHypixelifyAPI.class,
                this,
                this,
                ServicePriority.Normal
        );
        getLogger().info("Plugin has loaded!");
    }

    public void registerListener(Listener listener) {
        final var plugMan = Bukkit.getServer().getPluginManager();
        plugMan.registerEvents(listener, this);
        Logger.trace("Registered Listener: {}", listener.getClass().getSimpleName());
        registeredListeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        if (registeredListeners.contains(listener)) {
            HandlerList.unregisterAll(listener);
            registeredListeners.remove(listener);
        }
    }

    @Override
    public void onDisable() {
        if (SBAHypixelify.getInstance().getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(SBAUtil::removeScoreboardObjective);
        }

        getRegisteredListeners().forEach(this::unregisterListener);

        Logger.trace("Cancelling tasks...");
        this.getServer().getScheduler().cancelTasks(plugin);
        this.getServer().getServicesManager().unregisterAll(plugin);
        Logger.trace("Successfully shutdown SBAHypixelify instance");
    }

    public GamesInventory getGamesInventory() {
        return gamesInventory;
    }

    /*
     * API implementations
     */

    @Override
    public ConfiguratorAPI getConfigurator0() {
        return configurator;
    }

    @Override
    public List<Listener> getRegisteredListeners() {
        return List.copyOf(registeredListeners);
    }

    @Override
    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    @Override
    public void setExceptionHandler(@NotNull ExceptionHandler handler) {
        exceptionManager.setExceptionHandler(handler);
    }

    @Override
    public PartyManager getPartyManager() {
        return partyManager;
    }

    @Override
    public WrapperService<Player, ? extends PlayerWrapper> getPlayerWrapperService() {
        return playerWrapperService;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Optional<pronze.hypixelify.api.game.GameStorage> getGameStorage(Game game) {
        return arenaManager.getGameStorage(game.getName());
    }

    @Override
    public PlayerWrapper getPlayerWrapper(Player player) {
        return playerWrapperService.get(player).orElseThrow();
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public boolean isSnapshot() {
        return isSnapshot;
    }

    @Override
    public boolean isUpgraded() {
        return !Objects.requireNonNull(plugin.configurator.config.getString("version"))
                .contains(SBAHypixelify.getInstance().getVersion());
    }
}


