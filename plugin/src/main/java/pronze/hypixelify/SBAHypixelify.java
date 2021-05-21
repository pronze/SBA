package pronze.hypixelify;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import pronze.hypixelify.api.config.IConfigurator;
import pronze.hypixelify.api.exception.ExceptionHandler;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.lang.ILanguageService;
import pronze.hypixelify.api.manager.IArenaManager;
import pronze.hypixelify.api.manager.IPartyManager;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.game.ArenaManager;
import pronze.hypixelify.inventories.CustomShop;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import pronze.hypixelify.placeholderapi.SBAExpansion;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.lib.sgui.listeners.InventoryListener;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;

import java.util.Objects;
import java.util.Optional;

import static pronze.hypixelify.utils.MessageUtils.showErrorMessage;

@Plugin(
        id = "SBAHypixelify",
        authors = "pronze",
        loadTime = Plugin.LoadTime.POSTWORLD,
        version = "1.5.0"
)
@PluginDependencies(platform = PlatformType.BUKKIT, softDependencies =
        "PlaceholderAPI"
)
@Init(services = {
        ArenaManager.class,
        CustomShop.class,
        CommandManager.class
})

public class SBAHypixelify extends PluginContainer implements SBAHypixelifyAPI {
    private static SBAHypixelify instance;

    public static SBAHypixelify getInstance() {
        return instance;
    }

    public static JavaPlugin getPluginInstance() {
        return instance.getPluginDescription().as(JavaPlugin.class);
    }

    @Override
    public void enable() {
        instance = this;

        if (Bukkit.getServer().getServicesManager().getRegistration(BedwarsAPI.class) == null) {
            showErrorMessage("Could not find Screaming-BedWars plugin!, make sure " +
                    "you have the right one installed, and it's enabled properly!");
            return;
        }

        if (!Main.getVersion().contains("0.3.0")) {
            showErrorMessage("You need ScreamingBedWars v0.2.15 to run SBAHypixelify v1.0",
                    "Get the latest version from here: https://www.spigotmc.org/resources/screaming-bedwars-1-9-1-16.63714/");
            return;
        }

        if (Main.getVersionNumber() < 109) {
            showErrorMessage("Minecraft server is running versions below 1.9, please upgrade!");
            return;
        }

        InventoryListener.init(getPluginInstance());

        //Do changes for legacy support.
        changeBedWarsConfig();

        try {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new SBAExpansion().register();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Bukkit.getServer().getServicesManager().register(SBAHypixelifyAPI.class, this, getPluginInstance(), ServicePriority.Normal);
        getLogger().info("Plugin has loaded");
    }


    public void changeBedWarsConfig() {
        //Do changes for legacy support.
        if (Main.isLegacy()) {
            boolean doneChanges = false;
            if (Objects.requireNonNull(Main.getConfigurator()
                    .config.getString("items.leavegame")).equalsIgnoreCase("RED_BED")) {
                Main.getConfigurator().config.set("items.leavegame", "BED");
                doneChanges = true;
            }
            if (Objects.requireNonNull(Main.getConfigurator()
                    .config.getString("items.shopcosmetic")).equalsIgnoreCase("GRAY_STAINED_GLASS_PANE")) {
                Main.getConfigurator().config.set("items.shopcosmetic", "STAINED_GLASS_PANE");
                doneChanges = true;
            }

            if (Main.getConfigurator().config.getBoolean("scoreboard.enable", true)
            || Main.getConfigurator().config.getBoolean("lobby-scoreboard.enabled", true)) {
                Main.getConfigurator().config.set("scoreboard.enable", false);
                Main.getConfigurator().config.set("lobby-scoreboard.enabled", false);
                doneChanges = true;
            }

            if (doneChanges) {
                getLogger().info("[SBAHypixelify]: Making legacy changes");
                Main.getConfigurator().saveConfig();
                Bukkit.getServer().getPluginManager().disablePlugin(getPluginInstance());
                Bukkit.getServer().getPluginManager().enablePlugin(getPluginInstance());
            }

        }
    }

    @Override
    public void disable() {
        getLogger().info("Cancelling current tasks....");
        Bukkit.getServer().getScheduler().cancelTasks(getPluginInstance());
        Bukkit.getServer().getServicesManager().unregisterAll(getPluginInstance());
    }

    @Override
    public Optional<GameStorage> getGameStorage(Game game) {
        return Optional.empty();
    }

    @Override
    public PlayerWrapper getPlayerWrapper(Player player) {
        return null;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public boolean isSnapshot() {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public IArenaManager getArenaManager() {
        return null;
    }

    @Override
    public void setExceptionHandler(@NotNull ExceptionHandler handler) {

    }

    @Override
    public IPartyManager getPartyManager() {
        return null;
    }

    @Override
    public WrapperService<Player, PlayerWrapper> getPlayerWrapperService() {
        return null;
    }

    @Override
    public IConfigurator getConfigurator() {
        return null;
    }

    @Override
    public boolean isPendingUpgrade() {
        return false;
    }

    @Override
    public ILanguageService getLanguageService() {
        return null;
    }
}


