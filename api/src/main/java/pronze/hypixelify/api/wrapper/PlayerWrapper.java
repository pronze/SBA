package pronze.hypixelify.api.wrapper;

import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.UUID;

public interface PlayerWrapper {

    /**
     *
     * @return A string instance containing the player's name
     */
    String getName();

    /**
     *
     * @return true if player can shout and is not in cool down, false otherwise
     */
    boolean canShout();

    /**
     * Shouts a message to all the players of a particular game.
     */
    void shout(String message, Game game);

    /**
     *
     * @return
     */
    int getShoutTimeOut();

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

    /**
     *
     * @return
     */
    String getStringProgress();
}
