package pronze.hypixelify.party;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyCreatedEvent;
import pronze.hypixelify.api.manager.PartyManager;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.game.PlayerWrapperImpl;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManagerImpl implements PartyManager {
    private final Map<UUID, Party> partyMap = new ConcurrentHashMap<>();

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
    public Optional<Party> getOrCreate(@NotNull PlayerWrapper player) {
        return getPartyOf(player).or(() -> createParty(player));
    }

    @Override
    public Optional<Party> getPartyOf(@NotNull PlayerWrapper player) {
        return partyMap.values()
                .stream()
                .filter(party -> party.getMembers().contains(player))
                .findAny();
    }

    @Override
    public Optional<Party> getInvitedPartyOf(@NotNull PlayerWrapper player) {
        return partyMap.values()
                .stream()
                .filter(party -> party.isInvited(player))
                .findAny();
    }

    @Override
    public void disband(@NotNull UUID partyUUID) {
        if (partyMap.containsKey(partyUUID)) {
            final var party = partyMap.get(partyUUID);
            disband(party);
        }
    }

    @Override
    public void disband(@NotNull PlayerWrapper leader) {
        getPartyOf(leader).ifPresent(this::disband);
    }

    private void disband(@NotNull Party party) {
        Logger.trace("Disbandoning party: {}", party.debugInfo());
        final var disbandMessage = SBAHypixelify
                .getConfigurator()
                .getStringList("party.message.disband");

        party.getMembers().forEach(member -> {
            member.setInParty(false);
            party.removePlayer(member);
            final var wrapperImpl = PlayerMapper
                    .wrapPlayer(member.getInstance())
                    .as(PlayerWrapperImpl.class);
            disbandMessage
                    .forEach(wrapperImpl::sendMessage);
        });

        party.getInvitedPlayers().forEach(invitedPlayer -> {
            invitedPlayer.setInvitedToAParty(false);
            party.removeInvitedPlayer(invitedPlayer);
            final var wrapperImpl = PlayerMapper
                    .wrapPlayer(invitedPlayer.getInstance())
                    .as(PlayerWrapperImpl.class);
            SBAHypixelify
                    .getConfigurator()
                    .getStringList("party.message.expired")
                    .forEach(wrapperImpl::sendMessage);
        });

        party.getInviteData().forEach(data -> {
            final var inviteTask = data.getInviteTask();
            SBAUtil.cancelTask(inviteTask);
        });

        partyMap.remove(party.getUUID());
    }
}
