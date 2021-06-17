package pronze.sba.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pronze.sba.party.IParty;
import pronze.sba.wrapper.PlayerWrapper;

@RequiredArgsConstructor
@Getter
public class SBAPlayerPartyInviteDeclineEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final PlayerWrapper player;
    private final IParty party;
    private boolean cancelled;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBAPlayerPartyInviteDeclineEvent.handlerList;
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
