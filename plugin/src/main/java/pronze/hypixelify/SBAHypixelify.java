package pronze.hypixelify;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.ext.pronze.scoreboards.ScoreboardManager;
import org.screamingsandals.bedwars.lib.nms.utils.ClassStorage;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import pronze.hypixelify.api.config.IConfigurator;
import pronze.hypixelify.api.exception.ExceptionHandler;
import pronze.hypixelify.api.manager.IArenaManager;
import pronze.hypixelify.api.manager.IPartyManager;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.exception.ExceptionManager;
import pronze.hypixelify.game.ArenaManager;
import pronze.hypixelify.party.PartyManager;
import pronze.hypixelify.lib.lang.I18n;
import pronze.hypixelify.service.PlayerWrapperService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.lib.core.Core;
import pronze.lib.core.utils.Logger;

import java.util.*;

import static pronze.hypixelify.utils.MessageUtils.showErrorMessage;

public class SBAHypixelify extends JavaPlugin implements SBAHypixelifyAPI {
    private static SBAHypixelify plugin;
    private ExceptionManager exceptionManager;
    private String version;
    private Configurator configurator;
    private boolean debug;
    private boolean isSnapshot;

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
        plugin = this;
        version = this.getDescription().getVersion();
        isSnapshot = version.toLowerCase().contains("snapshot");
        exceptionManager = new ExceptionManager();

        if (getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (!Main.getVersion().contains("0.3.0")) {
            showErrorMessage("You need ScreamingBedWars v0.3.0 to run SBAHypixelify v2.0!",
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

        ScoreboardManager.init(this);

        configurator = new Configurator(this);
        configurator.loadDefaults();

        debug = configurator.config.getBoolean("debug.enabled", false);
        Core.init(this, debug);
        I18n.load(this, configurator.config.getString("locale"));

        Logger.trace("Registering API service provider");
        getServer().getServicesManager().register(SBAHypixelifyAPI.class, this, this, ServicePriority.Normal);
        getLogger().info("Plugin has loaded!");
    }

    @Override
    public void onDisable() {
        Logger.trace("Cancelling tasks...");
        this.getServer().getScheduler().cancelTasks(plugin);
        this.getServer().getServicesManager().unregisterAll(plugin);
        Core.destroy();
        Logger.trace("Successfully shutdown SBAHypixelify instance");
    }
    /*
     * API implementations
     */

    @Override
    public IConfigurator getConfigurator0() {
        return configurator;
    }

    @Override
    public IArenaManager getArenaManager() {
        return Core.getObjectFromClass(ArenaManager.class);
    }

    @Override
    public void setExceptionHandler(@NotNull ExceptionHandler handler) {
        exceptionManager.setExceptionHandler(handler);
    }

    @Override
    public IPartyManager getPartyManager() {
        return Core.getObjectFromClass(PartyManager.class);
    }

    @Override
    public WrapperService<Player, ? extends PlayerWrapper> getPlayerWrapperService() {
        return Core.getObjectFromClass(PlayerWrapperService.class);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Optional<pronze.hypixelify.api.game.GameStorage> getGameStorage(Game game) {
        return getArenaManager().getGameStorage(game.getName());
    }

    @Override
    public PlayerWrapper getPlayerWrapper(Player player) {
        return getPlayerWrapperService().get(player).orElseThrow();
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
    public boolean isPendingUpgrade() {
        return !version.contains(configurator.getString("version"));
    }
}


