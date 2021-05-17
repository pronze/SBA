package io.pronze.hypixelify.api;

import io.pronze.hypixelify.api.game.GameStorage;
import io.pronze.hypixelify.api.wrapper.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.game.Game;

public interface SBAHypixelifyAPI {

    /**
     *
     * @return registered instance of SBAHypixelify
     */
    static SBAHypixelifyAPI getInstance(){
        return Bukkit.getServer().getServicesManager().getRegistration(SBAHypixelifyAPI.class).getProvider();
    }

    /**
     *
     * @param game
     * @return Game storage of game provided, null if not exists
     */
    GameStorage getGameStorage(Game game);

    /**
     *
     * @param player
     * @return returns PlayerWrapper object of specified player
     */
    PlayerWrapper getPlayerWrapper(Player player);


}
