package pronze.hypixelify.api.events;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

@Getter
public class SBAPlayerPartyPromoteEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final PlayerWrapper player;
    private final PlayerWrapper toPromote;
    private boolean cancelled;

    public SBAPlayerPartyPromoteEvent(PlayerWrapper player,
                                      PlayerWrapper toPromote) {
        super(true);
        this.player = player;
        this.toPromote = toPromote;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBAPlayerPartyPromoteEvent.handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
