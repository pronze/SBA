package pronze.hypixelify.api.game;
import org.bukkit.scoreboard.Scoreboard;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndEvent;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerKilledEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.api.data.PlayerData;
import pronze.hypixelify.api.manager.ScoreboardManager;

import java.util.List;
import java.util.UUID;

public interface Arena {
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

    /**
     *
     * @param event
     */
    void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent event);

    /**
     *
     * @param event
     */
    void onOver(BedwarsGameEndingEvent event);

    /**
     *
     * @param event
     */
    void onBedWarsPlayerKilled(BedwarsPlayerKilledEvent event);

    /**
     *
     * @return
     */
    ScoreboardManager getScoreboardManager();

    /**
     *
     * @param playerUUID
     * @return
     */
    PlayerData getPlayerData(UUID playerUUID);
}
