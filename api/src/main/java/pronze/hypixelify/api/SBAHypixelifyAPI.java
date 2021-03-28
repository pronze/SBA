package pronze.hypixelify.api;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.config.ConfiguratorAPI;
import pronze.hypixelify.api.exception.ExceptionHandler;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.manager.ArenaManager;
import pronze.hypixelify.api.manager.PartyManager;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
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
        return Objects.requireNonNull(Bukkit.getServer()
                .getServicesManager().getRegistration(SBAHypixelifyAPI.class)).getProvider();
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
     * @return The {@link ArenaManager} instance that handles the creation or destruction of arenas.
     */
    ArenaManager getArenaManager();

    /**
     *
     * @param handler The {@link ExceptionHandler} instance that will handle exceptions thrown by the plugin.
     */
    void setExceptionHandler(@NotNull ExceptionHandler handler);

    /**
     *
     * <b>NOTE: This class is thread safe</b>
     * @return {@link PartyManager} instance that handles the creation and destruction of parties
     */
    PartyManager getPartyManager();

    /**
     *
     * @return an instance of the PlayerWrapperService that is associated with wrapping player
     * instances into objects that contain additional data
     */
    WrapperService<Player, ? extends PlayerWrapper> getPlayerWrapperService();

    /**
     * Look into ConfiguratorAPI getter methods to get certain settings from the config.
     * @return an instance of an Configurator which helps in the configuration of file based data.
     */
    ConfiguratorAPI getConfigurator0();

    /**
     *
     * @return true if recently has been upgraded, false otherwise.
     */
    boolean isUpgraded();

    /**
     *
     * @return an {@link SimpleDateFormat} instance that has been configured from the bwaconfig.yml
     */
    SimpleDateFormat getSimpleDateFormat();

    /**
     *
     * @return A formatted Date instance from {@link SimpleDateFormat} instance.
     */
    String getFormattedDate();
}
