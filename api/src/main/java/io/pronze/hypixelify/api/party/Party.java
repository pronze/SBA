package io.pronze.hypixelify.api.party;
import org.bukkit.entity.Player;
import java.util.List;

public interface Party {

    Player getLeader();

    void disband();

    void addMember(Player player);

    void removeMember(Player player);

    void addInvitedMember(Player player);

    void removeInvitedMember(Player player);

    List<Player> getInvitedMembers();

    List<Player> getPlayers();

    List<Player> getAllPlayers();

    int getSize();

    int getCompleteSize();

    boolean canAnyoneInvite();

    boolean shouldDisband();

    void sendChat(Player sender, String message);


}
