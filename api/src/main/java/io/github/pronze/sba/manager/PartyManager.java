package io.github.pronze.sba.manager;

import io.github.pronze.sba.events.SBAPlayerPartyCreatedEvent;
import io.github.pronze.sba.party.Party;
import org.jetbrains.annotations.NotNull;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;

import java.util.Optional;
import java.util.UUID;

public interface PartyManager {

    /**
     * Creates a party instance with the specified player as leader.
     * @param leader the leader object that owns the party
     * @return the party instance that has been created, returns an empty Optional if {@link SBAPlayerPartyCreatedEvent} is cancelled
     */
    Optional<Party> createParty(@NotNull SBAPlayerWrapper leader);

    /**
     * Gets an optional containing the party instance, or empty if the query fails to find any matches.
     * @param leader the leader of the party to query
     * @return an optional containing a party which may or may not be empty depending on the query
     */
    Optional<Party> get(@NotNull SBAPlayerWrapper leader);

    /**
     * Gets an optional containing the party instance, or empty if the query fails to find any matches.
     * @param partyUUID the uuid of party to query
     * @return an optional containing a party which may or may not be empty depending on the query
     */
    Optional<Party> get(@NotNull UUID partyUUID);

    /**
     * Gets an optional containing the party instance if present, else creates a new party instance.
     * Optional will be empty if both tasks has failed to do so.
     * @param leader the leader of the newly created party
     * @return
     */
    Optional<Party> getOrCreate(@NotNull SBAPlayerWrapper leader);

    /**
     * Gets an optional containing the party instance, or empty if the query fails to find any matches.
     * @param player the player of the party to query
     * @return an optional containing a party which may or may not be empty depending on the query
     */
    Optional<Party> getPartyOf(@NotNull SBAPlayerWrapper player);

    /**
     * Gets an optional containing the party instance, or empty if the query fails to find any matches.
     * @param player the player which has been invited to the party
     * @return an optional containing a party which may or may not be empty depending on the query
     */
    Optional<Party> getInvitedPartyOf(@NotNull SBAPlayerWrapper player);

    /**
     * Disbands the party and notifies the members.
     * @param partyUUID the uuid of the party to disband
     */
    void disband(@NotNull UUID partyUUID);

    /**
     * Disbands the party and notifies the members.
     * @param leader the leader of the party to disband
     */
    void disband(@NotNull SBAPlayerWrapper leader);
}
