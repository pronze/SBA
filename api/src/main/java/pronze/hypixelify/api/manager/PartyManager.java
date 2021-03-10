package pronze.hypixelify.api.manager;

import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.Optional;
import java.util.UUID;

public interface PartyManager {

    /**
     *
     * @param leader the leader object that owns the party.
     * @return the party instance that has been created.
     */
    Party createParty(@NotNull PlayerWrapper leader);

    /**
     *
     * @param leader
     * @return
     */
    Optional<Party> get(@NotNull PlayerWrapper leader);

    /**
     *
     * @param partyUUID
     * @return
     */
    Optional<Party> get(@NotNull UUID partyUUID);
}
