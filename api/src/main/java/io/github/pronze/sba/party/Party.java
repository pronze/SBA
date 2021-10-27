package io.github.pronze.sba.party;

import io.github.pronze.sba.data.PartyInviteData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface Party {

    /**
     * @return a {@link List} of players as {@link SBAPlayerWrapper} objects.
     */
    List<SBAPlayerWrapper> getMembers();

    /**
     * @return a {@link List} of players that are invited to this party as {@link SBAPlayerWrapper} objects.
     */
    List<SBAPlayerWrapper> getInvitedPlayers();

    /**
     *
     * @param message the message object to be sent to all players of this party.
     * @param sender the sender of the message
     */
    void sendMessage(@NotNull Component message, @NotNull SBAPlayerWrapper sender);

    /**
     * Removes the specified player from the party.
     *
     * @param player the player instance to be removed.
     */
    void removePlayer(@NotNull SBAPlayerWrapper player);

    /**
     * Adds the specified player to the party
     *
     * @param player the player instance to be added.
     */
    void addPlayer(@NotNull SBAPlayerWrapper player);

    /**
     * @return the party leader of this party.
     */
    SBAPlayerWrapper getPartyLeader();

    /**
     *
     * @param player the player instance to be made the leader of this party.
     */
    void setPartyLeader(@NotNull SBAPlayerWrapper player);

    /**
     * @return the unique id associated with this party object.
     */
    UUID getUUID();

    /**
     * @param invited the player instance to be invited to this party.
     * @param player the player who invited.
     */
    void invitePlayer(@NotNull SBAPlayerWrapper invited,
                      @NotNull SBAPlayerWrapper player);

    /**
     *
     * @param player the player instance to check
     * @return true if this player has been invited to the party, false otherwise.
     */
    boolean isInvited(@NotNull SBAPlayerWrapper player);

    /**
     *
     * @return true if party is about to be disbanded, false otherwise.
     */
    boolean shouldDisband();

    /**
     * @param invitee the player instance to cancel the invite.
     */
    void removeInvitedPlayer(@NotNull SBAPlayerWrapper invitee);

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
