package io.github.pronze.sba.manager;

import io.github.pronze.sba.events.SBAPlayerPartyCreatedEvent;
import io.github.pronze.sba.party.IParty;
import org.jetbrains.annotations.NotNull;
import io.github.pronze.sba.wrapper.PlayerWrapper;

import java.util.Optional;
import java.util.UUID;

public interface IPartyManager {

    /**
     *
     * @param leader the leader object that owns the party.
     * @return the party instance that has been created, returns {@link Optional#empty()} if {@link SBAPlayerPartyCreatedEvent} is cancelled.
     */
    Optional<IParty> createParty(@NotNull PlayerWrapper leader);

    /**
     *
     * @param leader
     * @return
     */
    Optional<IParty> get(@NotNull PlayerWrapper leader);

    /**
     *
     * @param partyUUID
     * @return
     */
    Optional<IParty> get(@NotNull UUID partyUUID);

    /**
     *
     * @param leader
     * @return
     */
    Optional<IParty> getOrCreate(@NotNull PlayerWrapper leader);

    /**
     *
     * @param player
     * @return
     */
    Optional<IParty> getPartyOf(@NotNull PlayerWrapper player);

    /**
     *
     * @param player
     * @return
     */
    Optional<IParty> getInvitedPartyOf(@NotNull PlayerWrapper player);

    /**
     *
     * @param partyUUID
     */
    void disband(@NotNull UUID partyUUID);

    /**
     *
     * @param leader
     */
    void disband(@NotNull PlayerWrapper leader);
}
