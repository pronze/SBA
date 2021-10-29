package io.github.pronze.sba;

import io.github.pronze.sba.config.Configurator;
import io.github.pronze.sba.game.GameStorage;
import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.manager.GameWrapperManager;
import io.github.pronze.sba.manager.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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
     * Returns whether the SBA plugin instance has registered the API to bukkit.
     *
     * @return true if the API has been registered within bukkit's services manager, false otherwise
     */
    static boolean isAPIRegistered() {
        return Bukkit.getServer().getServicesManager().getRegistration(AddonAPI.class) != null;
    }

    /**
     * Returns an Optional containing the GameStorage instance of the provided game, empty otherwise.
     *
     * @param game The game associated with the storage.
     * @return {@link GameStorage} of game if exists, null otherwise
     */
    Optional<GameStorage> getGameStorage(@NotNull GameWrapper game);

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
     * @return The {@link GameWrapperManager} instance that handles the creation or destruction of arenas.
     */
    GameWrapperManager getGameWrapperManager();

    /**
     * Returns the PartyManager instance that is associated with Party related management.
     *
     * @return {@link PartyManager} instance that handles the creation and destruction of parties
     */
    PartyManager getPartyManager();


    /**
     * Look into IConfigurator getter methods to get certain settings from the config.
     *
     * @return an instance of a Configurator which helps in the configuration of file based data
     */
    Configurator getConfigurator();

    /**
     * Returns a boolean indicating if the plugin has recently been upgraded from an older version and is <br>
     * pending upgradation of configuration.
     *
     * @return true if recently has been upgraded, false otherwise
     */
    boolean isPendingUpgrade();

    /**
     *
     * @return an instance of the SBA plugin.
     */
    JavaPlugin getJavaPlugin();

    void unregisterListener(@NotNull Listener listener);

    void registerListener(@NotNull Listener listener);

    /**
     * Returns the wrapper object associated to the player containing additional player data.
     *
     * @param player The player instance to obtain the wrapper from.
     * @return the {@link SBAPlayerWrapper} object linked to the specific player
     */
    SBAPlayerWrapper wrapPlayer(Player player);
}
