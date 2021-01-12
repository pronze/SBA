package pronze.hypixelify.api.game;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.List;

public interface Arena {

    /*
        why ? extends RotatingGenerators?, because we need the impl itself not the API in example
        using the destroy method, or clearing the RotatingGenerator cache.
        Not necessary but makes it simpler for now
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
