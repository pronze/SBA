package pronze.hypixelify.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

@Getter
public class SBAPlayerPartyInviteAcceptEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final PlayerWrapper player;
    private final Party party;
    private boolean cancelled;

    public SBAPlayerPartyInviteAcceptEvent(PlayerWrapper player,
                                           Party party) {
        super(true);
        this.player = player;
        this.party = party;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBAPlayerPartyInviteAcceptEvent.handlerList;
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
