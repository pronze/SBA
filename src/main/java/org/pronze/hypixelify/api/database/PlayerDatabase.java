package org.pronze.hypixelify.api.database;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.api.party.Party;

import java.util.concurrent.CompletableFuture;

public interface PlayerDatabase {


    boolean getPartyChatEnabled();

    boolean isPartyLeader();

    boolean isInParty();

    boolean isInvited();

    void setPartyChatEnabled(boolean b);

    void setIsInParty(boolean b);

    void setInvitedParty(Party party);

    void setInvited(boolean bool);

    void setPlayer(Player player);

    void setPartyLeader(Player player);

    void setExpiredTimeTimeout(int timeout);

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

    String getCompletedBoxes();
}
