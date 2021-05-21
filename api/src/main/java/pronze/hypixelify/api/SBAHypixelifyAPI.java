package pronze.hypixelify.api;

import pronze.hypixelify.api.config.IConfigurator;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.lang.ILanguageService;
import pronze.hypixelify.api.manager.IArenaManager;
import pronze.hypixelify.api.manager.IPartyManager;
import pronze.hypixelify.api.service.WrapperService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.Optional;

/**
 * Represents the API for SBAHypixelify
 */
public interface SBAHypixelifyAPI {

    /**
     *
     * @return Registered instance of SBAHypixelify
     */
    static SBAHypixelifyAPI getInstance(){
        var instance = Bukkit.getServer().getServicesManager().getRegistration(SBAHypixelifyAPI.class);
        if (instance == null) {
            throw new UnsupportedOperationException("SBAHypixelify has not been initialized properly yet!");
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
     * @return true if running snapshot versions of SBAHypixelify, false otherwise
     */
    boolean isSnapshot();

    /**
     *
     * @return The version SBAHypixelify is currently running.
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
