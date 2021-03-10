package pronze.hypixelify.party;
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

    public PartyImpl(PlayerWrapper leader) {
        this.leader = leader;
        leader.setInParty(true);
    }

    @Override
    public List<PlayerWrapper> getMembers() {
        return List.copyOf(members);
    }

    @Override
    public void sendMessage(String message, PlayerWrapper sender) {
        final var formattedMessage = SBAHypixelify
                .getConfigurator()
                .getString("party.chat.format")
                .replace("%name%", sender.getName())
                .replace("%message%", message);
        members.forEach(player -> player.sendMessage(formattedMessage));
    }

    @Override
    public void removePlayer(PlayerWrapper player) {
        members.remove(player);
        player.setInParty(false);
    }

    @Override
    public void addPlayer(PlayerWrapper player) {
        members.add(player);
        player.setInParty(true);
    }

    @Override
    public PlayerWrapper getPartyLeader() {
        return leader;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
