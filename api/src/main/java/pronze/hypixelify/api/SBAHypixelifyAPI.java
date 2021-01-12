package pronze.hypixelify.api;

import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.manager.PartyManager;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the API for SBAHypixelify
 */
public interface SBAHypixelifyAPI {

    /**
     *
     * @return registered instance of SBAHypixelify
     */
    static SBAHypixelifyAPI getInstance(){
        return Objects.requireNonNull(Bukkit.getServer()
                .getServicesManager().getRegistration(SBAHypixelifyAPI.class)).getProvider();
    }

    /**
     *
     * @param game the game associated with the storage
     * @return {@link GameStorage} of game if exists, null otherwise
     */
    Optional<GameStorage> getGameStorage(Game game);

    /**
     *
     * @param player the player instance to obtain the wrapper from
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
     * @return the version SBAHypixelify is currently running
     */
    String getVersion();

    /**
     *
     * @param player the player object to lookup
     * @return true if player is present in any party, false otherwise
     */
    boolean isInParty(Player player);

    /**
     * Return the party instance of which the player currently is in
     * @see SBAHypixelifyAPI#isInParty(Player) 
     * @param player the player object to lookup
     * @return {@link Party} if player is in a party, null otherwise
     */
    Optional<Party> getParty(Player player);

    /**
     *
     * @param key the key to search from the config
     * @param def the default value to be returned if the key does not contain a mapped value or if it does not exist
     * @param <T> the type of the object to return
     * @return the object that has been searched using the key, returns def argument if key does not exist
     */
    <T> T getObject(String key, T def);
}
