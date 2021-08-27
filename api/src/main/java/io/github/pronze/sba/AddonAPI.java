package io.github.pronze.sba;

import io.github.pronze.sba.config.IConfigurator;
import io.github.pronze.sba.game.IGameStorage;
import io.github.pronze.sba.lang.ILanguageService;
import io.github.pronze.sba.manager.IArenaManager;
import io.github.pronze.sba.manager.IPartyManager;
import io.github.pronze.sba.service.WrapperService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;

import java.util.Optional;

/**
 * Represents the API for SBA
 */
public interface AddonAPI {

    /**
     * Returns the registered instance of SBA, throws {@link UnsupportedOperationException} if plugin has not yet been registered.
     *
     * @throws UnsupportedOperationException if the plugin instance has not yet been registered in Bukkit's ServiceManager.
     * @return registered instance of SBA
     */
    static AddonAPI getInstance(){
        var instance = Bukkit.getServer().getServicesManager().getRegistration(AddonAPI.class);
        if (instance == null) {
            throw new UnsupportedOperationException("SBA has not been initialized properly yet!");
        }
        return instance.getProvider();
    }

    /**
     * Returns an Optional containing the GameStorage instance of the provided game, empty otherwise.
     *
     * @param game The game associated with the storage.
     * @return {@link IGameStorage} of game if exists, null otherwise
     */
    Optional<IGameStorage> getGameStorage(@NotNull Game game);

    /**
     *  Returns the wrapper object associated to the player containing additional player data.
     *
     * @param player The player instance to obtain the wrapper from.
     * @return the {@link SBAPlayerWrapper} object linked to the specific player
     */
    SBAPlayerWrapper getPlayerWrapper(Player player);

    /**
     * Returns a boolean indicating if the plugin is running in debug mode.
     *
     * @return true if debug is enabled, false otherwise
     */
    boolean isDebug();

    /**
     * Returns a boolean indicating if the plugin is a snapshot version.
     *
     * @return true if running snapshot versions of SBA, false otherwise
     */
    boolean isSnapshot();

    /**
     *  Returns the version string of the plugin. Ex:- 1.5.5-SNAPSHOT
     *
     * @return The version SBA is currently running.
     */
    String getVersion();

    /**
     * Returns the ArenaManager instance that is associated with the arena handling.
     *
     * @return The {@link IArenaManager} instance that handles the creation or destruction of arenas.
     */
    IArenaManager getArenaManager();

    /**
     * Returns the PartyManager instance that is associated with Party related management.
     *
     * @return {@link IPartyManager} instance that handles the creation and destruction of parties
     */
    IPartyManager getPartyManager();

    /**
     *
     * @return an instance of the PlayerWrapperService that is associated with wrapping player
     * instances into objects that contain additional data
     */
    WrapperService<Player, SBAPlayerWrapper> getPlayerWrapperService();

    /**
     * Look into IConfigurator getter methods to get certain settings from the config.
     *
     * @return an instance of a Configurator which helps in the configuration of file based data
     */
    IConfigurator getConfigurator();

    /**
     * Returns a boolean indicating if the plugin has recently been upgraded from an older version and is <br>
     * pending upgradation of configuration.
     *
     * @return true if recently has been upgraded, false otherwise
     */
    boolean isPendingUpgrade();

    /**
     *
     * @return The language service instance for retrieving language related information
     */
    ILanguageService getLanguageService();

    /**
     *
     * @return an instance of the SBA plugin.
     */
    JavaPlugin getJavaPlugin();
}
