package io.github.pronze.sba;

import io.github.pronze.sba.config.IConfigurator;
import io.github.pronze.sba.game.GameStorage;
import io.github.pronze.sba.lang.ILanguageService;
import io.github.pronze.sba.manager.IArenaManager;
import io.github.pronze.sba.manager.IPartyManager;
import io.github.pronze.sba.service.WrapperService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;
import io.github.pronze.sba.wrapper.PlayerWrapper;

import java.util.Optional;

/**
 * Represents the API for SBA
 */
public interface AddonAPI {

    /**
     *
     * @return Registered instance of SBA
     */
    static AddonAPI getInstance(){
        var instance = Bukkit.getServer().getServicesManager().getRegistration(AddonAPI.class);
        if (instance == null) {
            throw new UnsupportedOperationException("SBA has not been initialized properly yet!");
        }
        return instance.getProvider();
    }

    /**
     *
     * @param game The game associated with the storage.
     * @return {@link GameStorage} of game if exists, null otherwise
     */
    Optional<GameStorage> getGameStorage(Game game);

    /**
     *
     * @param player The player instance to obtain the wrapper from.
     * @return the {@link PlayerWrapper} object linked to the specific player
     */
    PlayerWrapper getPlayerWrapper(Player player);

    /**
     *
     * @return true if debug is enabled, false otherwise
     */
    boolean isDebug();

    /**
     *
     * @return true if running snapshot versions of SBA, false otherwise
     */
    boolean isSnapshot();

    /**
     *
     * @return The version SBA is currently running.
     */
    String getVersion();

    /**
     *
     * @return The {@link IArenaManager} instance that handles the creation or destruction of arenas.
     */
    IArenaManager getArenaManager();

    /**
     *
     * <b>NOTE: This class is thread safe</b>
     * @return {@link IPartyManager} instance that handles the creation and destruction of parties
     */
    IPartyManager getPartyManager();

    /**
     *
     * @return an instance of the PlayerWrapperService that is associated with wrapping player
     * instances into objects that contain additional data
     */
    WrapperService<Player, PlayerWrapper> getPlayerWrapperService();

    /**
     * Look into IConfigurator getter methods to get certain settings from the config.
     * @return an instance of an Configurator which helps in the configuration of file based data.
     */
    IConfigurator getConfigurator();

    /**
     *
     * @return true if recently has been upgraded, false otherwise.
     */
    boolean isPendingUpgrade();

    /**
     *
     * @return The language service instance for retrieving language related information.
     */
    ILanguageService getLanguageService();
}
