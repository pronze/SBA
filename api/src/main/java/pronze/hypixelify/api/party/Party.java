package pronze.hypixelify.api.party;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.List;
import java.util.UUID;

public interface Party {

    /**
     * @return a {@link List} of players as {@link PlayerWrapper} objects.
     */
    List<PlayerWrapper> getMembers();

    /**
     * @return a {@link List} of players that are invited to this party as {@link PlayerWrapper} objects.
     */
    List<PlayerWrapper> getInvitedPlayers();

    /**
     *
     * @param message the message object to be sent to all players of this party.
     * @param sender the sender of the message
     */
    void sendMessage(@NotNull Component message, @NotNull PlayerWrapper sender);

    /**
     * Removes the specified player from the party.
     *
     * @param player the player instance to be removed.
     */
    void removePlayer(@NotNull PlayerWrapper player);

    /**
     * Adds the specified player to the party
     *
     * @param player the player instance to be added.
     */
    void addPlayer(@NotNull PlayerWrapper player);

    /**
     * @return the party leader of this party.
     */
    PlayerWrapper getPartyLeader();

    /**
     * @return the unique id associated with this party object.
     */
    UUID getUUID();

    /**
     * @param invitee the player instance to be invited to this party.
     */
    void invitePlayer(@NotNull PlayerWrapper invitee);

    /**
     * @param invitee the player instance to cancel the invite.
     */
    void removeInvitedPlayer(@NotNull PlayerWrapper invitee);
}
