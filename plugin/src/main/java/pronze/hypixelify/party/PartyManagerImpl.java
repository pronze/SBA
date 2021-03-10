package pronze.hypixelify.party;

import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.manager.PartyManager;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PartyManagerImpl implements PartyManager {
    private final Map<UUID, Party> partyMap = new HashMap<>();

    @Override
    public Party createParty(@NotNull PlayerWrapper leader) {
        return null;
    }

    @Override
    public Optional<Party> get(@NotNull PlayerWrapper leader) {
        return Optional.empty();
    }

    @Override
    public Optional<Party> get(@NotNull UUID partyUUID) {
        return Optional.empty();
    }
}
