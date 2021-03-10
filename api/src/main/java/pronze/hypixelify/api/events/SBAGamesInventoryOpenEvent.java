package pronze.hypixelify.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class SBAGamesInventoryOpenEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private boolean isCancelled = false;
    private int mode;

    public SBAGamesInventoryOpenEvent(Player player, int mode){
        this.player = player;
        this.mode = mode;
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
        return SBAGamesInventoryOpenEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SBAGamesInventoryOpenEvent.handlers;
    }
}
