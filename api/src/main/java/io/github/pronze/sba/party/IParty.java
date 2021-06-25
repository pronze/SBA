package io.github.pronze.sba.party;

import io.github.pronze.sba.data.PartyInviteData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import io.github.pronze.sba.wrapper.PlayerWrapper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IParty {

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
     *
     * @param player the player instance to be made the leader of this party.
     */
    void setPartyLeader(@NotNull PlayerWrapper player);

    /**
     * @return the unique id associated with this party object.
     */
    UUID getUUID();

    /**
     * @param invited the player instance to be invited to this party.
     * @param player the player who invited.
     */
    void invitePlayer(@NotNull PlayerWrapper invited,
                      @NotNull PlayerWrapper player);

    /**
     *
     * @param player the player instance to check
     * @return true if this player has been invited to the party, false otherwise.
     */
    boolean isInvited(@NotNull PlayerWrapper player);

    /**
     *
     * @return true if party is about to be disbanded, false otherwise.
     */
    boolean shouldDisband();

    /**
     * @param invitee the player instance to cancel the invite.
     */
    void removeInvitedPlayer(@NotNull PlayerWrapper invitee);

    /**
     *
     * @return {@link PartyInviteData} of all players invited to this party.
     */
    Collection<PartyInviteData> getInviteData();

    /**
     *
     * Formatted like [leader="", uuid=""]
     * @return string containing leader name and party UUID,
     */
    String debugInfo();

    /**
     *
     * @return {@link PartySetting} object that manages the settings of this party instance.
     */
    PartySetting getSettings();
}
