package org.pronze.hypixelify.listener;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;

public abstract class AbstractListener implements Listener {

    public boolean isInGame(Player player){
        return BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player);
    }

    public Game getGame(Player player){return BedwarsAPI.getInstance().getGameOfPlayer(player);}


    public abstract void onDisable();
}
