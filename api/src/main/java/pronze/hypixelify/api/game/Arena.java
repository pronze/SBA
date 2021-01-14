package pronze.hypixelify.api.game;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.List;

public interface Arena {

    /**
     *
     * @return
     */
    List<? extends RotatingGenerators> getRotatingGenerators();

    /**
     *
     * @return game storage
     */
    GameStorage getStorage();

    /**
     *
     * @return game object of this arena
     */
    Game getGame();

}
