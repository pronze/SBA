package pronze.hypixelify.party;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyCreatedEvent;
import pronze.hypixelify.api.manager.IPartyManager;
import pronze.hypixelify.api.party.IParty;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.utils.SBAUtil;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.utils.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AutoInitialize
public class PartyManager implements IPartyManager {
    private final Map<UUID, IParty> partyMap = new ConcurrentHashMap<>();

    public PartyManager() {
        Logger.trace("IPartyManager has been initialized!");
    }

    @Override
    public Optional<IParty> createParty(@NotNull PlayerWrapper leader) {
        final var party = new Party(leader);
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
    public Optional<IParty> get(@NotNull PlayerWrapper leader) {
        return partyMap
                .values()
                .stream()
                .filter(p -> p.getPartyLeader().equals(leader))
                .findAny();
    }

    @Override
    public Optional<IParty> get(@NotNull UUID partyUUID) {
        if (!partyMap.containsKey(partyUUID)) {
            return Optional.empty();
        }
        return Optional.of(partyMap.get(partyUUID));
    }

    @Override
    public Optional<IParty> getOrCreate(@NotNull PlayerWrapper player) {
        return getPartyOf(player).or(() -> createParty(player));
    }

    @Override
    public Optional<IParty> getPartyOf(@NotNull PlayerWrapper player) {
        return partyMap.values()
                .stream()
                .filter(party -> party.getMembers().contains(player))
                .findAny();
    }

    @Override
    public Optional<IParty> getInvitedPartyOf(@NotNull PlayerWrapper player) {
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

    private void disband(@NotNull IParty party) {
        Logger.trace("Disbandoning party: {}", party.debugInfo());
        final var disbandMessage = SBAHypixelify
                .getInstance()
                .getConfigurator()
                .getStringList("party.message.disband");

        party.getMembers().forEach(member -> {
            member.setInParty(false);
            party.removePlayer(member);
            final var wrapperImpl = PlayerMapper
                    .wrapPlayer(member.getInstance())
                    .as(PlayerWrapper.class);
            disbandMessage
                    .forEach(wrapperImpl::sendMessage);
        });

        party.getInvitedPlayers().forEach(invitedPlayer -> {
            invitedPlayer.setInvitedToAParty(false);
            party.removeInvitedPlayer(invitedPlayer);
            final var wrapperImpl = PlayerMapper
                    .wrapPlayer(invitedPlayer.getInstance())
                    .as(PlayerWrapper.class);
            SBAHypixelify
                    .getInstance()
                    .getConfigurator()
                    .getStringList("party.message.invite-expired")
                    .forEach(wrapperImpl::sendMessage);
        });

        party.getInviteData().forEach(data -> {
            final var inviteTask = data.getInviteTask();
            SBAUtil.cancelTask(inviteTask);
        });

        partyMap.remove(party.getUUID());
    }
}
