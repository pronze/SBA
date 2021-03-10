package pronze.hypixelify.api.wrapper;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerWrapper {

    /**
     *
     * @return
     */
    UUID getUUID();

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
     * @param message
     */
    void sendMessage(String message);

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
}
