package io.pronze.hypixelify.api;

import io.pronze.hypixelify.api.game.GameStorage;
import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.api.game.Game;

public interface SBAapi {

    //TODO: work on this

    static SBAapi getInstance(){
        return (SBAapi) Bukkit.getServer()
                .getPluginManager().getPlugin("SBAHypixelify");
    }

    GameStorage getGameStorage(Game game);



}
