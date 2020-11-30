package io.pronze.hypixelify.api.party;
import org.bukkit.entity.Player;
import java.util.List;

public interface PartyManager {

    void disband(Player player);

    List<Party> getParties();

    boolean isInParty(Player player);

    void addToParty(Player player, Party party);

    void removeFromParty(Player player, Party party);

    void kickFromParty(Player player);

    Party getParty(Player player);

    void warpPlayersToLeader(Player player);

    Party createParty(Player player);

    void removeParty(Player leader);

    void removeParty(Party party);

    void removeFromInvitedParty(Player player);

    void databaseDeletionFromParty(Player player, Player partyLeader);
}
