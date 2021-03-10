package pronze.hypixelify.party;

import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyCreatedEvent;
import pronze.hypixelify.api.manager.PartyManager;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PartyManagerImpl implements PartyManager {
    private final Map<UUID, Party> partyMap = new HashMap<>();

    public PartyManagerImpl() {
        Logger.trace("PartyManager has been initialized!");
    }

    @Override
    public Optional<Party> createParty(@NotNull PlayerWrapper leader) {
        final var party = new PartyImpl(leader);
        final var partyCreateEvent = new SBAPlayerPartyCreatedEvent(leader, party);
        SBAHypixelify
                .getInstance()
                .getServer()
                .getPluginManager()
                .callEvent(partyCreateEvent);
        if (partyCreateEvent.isCancelled()) return Optional.empty();

        partyMap.put(party.getUUID(), party);
        return Optional.of(party);
    }

    @Override
    public Optional<Party> get(@NotNull PlayerWrapper leader) {
        return partyMap
                .values()
                .stream()
                .filter(p -> p.getPartyLeader().equals(leader))
                .findAny();
    }

    @Override
    public Optional<Party> get(@NotNull UUID partyUUID) {
        if (!partyMap.containsKey(partyUUID)) {
            return Optional.empty();
        }
        return Optional.of(partyMap.get(partyUUID));
    }

    @Override
    public Optional<Party> getPartyOf(@NotNull PlayerWrapper player) {
        return partyMap.values()
                .stream()
                .filter(party -> party.getMembers().contains(player))
                .findAny();
    }
}
