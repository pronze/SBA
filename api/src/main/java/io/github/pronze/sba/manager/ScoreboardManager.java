package io.github.pronze.sba.manager;

import org.bukkit.entity.Player;

public interface ScoreboardManager {
    /**
     *
     * @param player
     */
    void createBoard(Player player);

    /**
     *
     * @param player
     */
    void removeBoard(Player player);

    /**
     *
     */
    void destroy();
}
