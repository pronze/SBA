package pronze.hypixelify.party;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyImpl implements Party {
    private final UUID uuid = UUID.randomUUID();
    private final PlayerWrapper leader;
    private final List<PlayerWrapper> members = new ArrayList<>();
    private final List<PlayerWrapper> invitedPlayers = new ArrayList<>();

    public PartyImpl(@NotNull PlayerWrapper leader) {
        this.leader = leader;
        leader.setInParty(true);
    }

    @Override
    public List<PlayerWrapper> getMembers() {
        return List.copyOf(members);
    }

    @Override
    public List<PlayerWrapper> getInvitedPlayers() {
        return List.copyOf(invitedPlayers);
    }

    @Override
    public void sendMessage(@NotNull Component message, @NotNull PlayerWrapper sender) {
        final var formattedMessage = SBAHypixelify
                .getConfigurator()
                .getString("party.chat.format")
                .replace("%name%", sender.getName())
                .replace("%message%", AdventureHelper.toLegacy(message));
        members.forEach(player -> PlayerMapper.wrapPlayer(player.getInstance()).sendMessage(formattedMessage));
    }

    @Override
    public void addPlayer(@NotNull PlayerWrapper player) {
        members.add(player);
        player.setInParty(true);
    }

    @Override
    public void removePlayer(@NotNull PlayerWrapper player) {
        members.remove(player);
        player.setInParty(false);
    }

    @Override
    public PlayerWrapper getPartyLeader() {
        return leader;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void invitePlayer(@NotNull PlayerWrapper invitee) {
        invitedPlayers.add(invitee);
        invitee.setInvitedToAParty(true);
    }

    @Override
    public void removeInvitedPlayer(@NotNull PlayerWrapper invitee) {
        if (!invitedPlayers.contains(invitee)) {
            return;
        }

        invitedPlayers.remove(invitee);
        invitee.setInvitedToAParty(false);
    }
}
