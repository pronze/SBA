package io.pronze.hypixelify.api.game;

import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.List;

public interface Arena {

    /*
        why ? extends RotatingGenerators?, because we need the impl itself not the API in example
        using the destroy method, or clearing the RotatingGenerator cache.
        Not necessary but makes it simpler for now
     */
    List<? extends RotatingGenerators> getRotatingGenerators();

    GameStorage getStorage();

    Game getGame();

    void onOver(BedwarsGameEndingEvent e);

    void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e);

    void onGameStarted(BedwarsGameStartedEvent e);

}
