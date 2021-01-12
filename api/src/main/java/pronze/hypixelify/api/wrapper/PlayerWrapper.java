package pronze.hypixelify.api.wrapper;
import pronze.hypixelify.api.party.Party;
import org.bukkit.entity.Player;

public interface PlayerWrapper {


    boolean getPartyChatEnabled();

    boolean isPartyLeader();

    boolean isInParty();

    boolean isInvited();

    void setPartyChatEnabled(boolean b);

    void setIsInParty(boolean b);

    void setInvitedParty(Party party);

    void setInvited(boolean bool);

    void setParty(Player player);

    void updateDatabase();

    Player getPartyLeader();

    String getName();

    Party getInvitedParty();

    boolean canShout();

    void shout();

    int getShoutTimeOut();

    int getKills();

    int getBedDestroys();

    int getDeaths();

    double getKD();

    int getXP();

    int getLevel();

    int getWins();

    String getProgress();

    int getIntegerProgress();

    String getCompletedBoxes();

    void sendMessage(String message);

    Party getParty();
}
