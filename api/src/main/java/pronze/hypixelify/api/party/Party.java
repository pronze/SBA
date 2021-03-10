package pronze.hypixelify.api.party;

import net.kyori.adventure.text.Component;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.List;
import java.util.UUID;

public interface Party {

    /**
     *
     * @return a {@link List} of players as {@link PlayerWrapper} objects
     */
    List<PlayerWrapper> getMembers();

    /**
     * Sends a message to all players of the party
     * @param message the message object to be received by the members of this party.
     * @param sender the player who sent the message.
     */
    void sendMessage(String message, PlayerWrapper sender);

    /**
     * Removes the specified player from the party.
     * @param player the player instance to be removed.
     */
    void removePlayer(PlayerWrapper player);

    /**
     * Adds the specified player to the party
     * @param player the player instance to be added.
     */
    void addPlayer(PlayerWrapper player);

    /**
     *
     * @return the party leader of this party.
     */
    PlayerWrapper getPartyLeader();

    /**
     *
     * @return the unique id associated with this party object.
     */
    UUID getUUID();
}
