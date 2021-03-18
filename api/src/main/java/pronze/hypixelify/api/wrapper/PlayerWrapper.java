package pronze.hypixelify.api.wrapper;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerWrapper {

    /**
     *
     * @return
     */
    String getName();

    /**
     *
     * @return
     */
    boolean canShout();

    /**
     *
     */
    void shout();

    /**
     *
     * @return
     */
    int getShoutTimeOut();

    /**
     *
     * @return
     */
    int getKills();

    /**
     *
     * @return
     */
    int getBedDestroys();

    /**
     *
     * @return
     */
    int getDeaths();

    /**
     *
     * @return
     */
    double getKD();

    /**
     *
     * @return
     */
    int getXP();

    /**
     *
     * @return
     */
    int getLevel();

    /**
     *
     * @return
     */
    int getWins();

    /**
     *
     * @return
     */
    String getProgress();

    /**
     *
     * @return
     */
    int getIntegerProgress();

    /**
     *
     * @return
     */
    String getCompletedBoxes();
    /**
     *
     * @return
     */
    Player getInstance();

    /**
     *
     * @return true if player is in a party, false otherwise.
     */
    boolean isInParty();

    /**
     *
     * @param isInParty
     */
    void setInParty(boolean isInParty);

    /**
     *
     * @return true if player has recently been invited to a party, false otherwise.
     */
    boolean isInvitedToAParty();

    /**
     *
     * @param isInvited
     */
    void setInvitedToAParty(boolean isInvited);

    /**
     *
     * @return
     */
    boolean isPartyChatEnabled();

    /**
     *
     * @param bool
     */
    void setPartyChatEnabled(boolean bool);
}
