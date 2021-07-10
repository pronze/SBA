package io.github.pronze.sba.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ScoreboardManager {

    /**
     * Creates a Scoreboard for the specified player.
     * @param player the player instance to create the scoreboard for
     */
    void createScoreboard(@NotNull Player player);

    /**
     * Removes a scoreboard for the specified player.
     * @param player the player instance to remove the board from
     */
    void removeScoreboard(@NotNull Player player);

    /**
     * Destroys the scoreboard from all viewers.
     */
    void destroy();
}
