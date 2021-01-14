package pronze.hypixelify.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GamesInventoryOpenEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private boolean isCancelled = false;
    private int mode;

    public GamesInventoryOpenEvent(Player player, int mode){
        this.player = player;
        this.mode = mode;
    }

    public Player getPlayer(){
        return player;
    }

    public int getMode(){
        return mode;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return GamesInventoryOpenEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return GamesInventoryOpenEvent.handlers;
    }
}
