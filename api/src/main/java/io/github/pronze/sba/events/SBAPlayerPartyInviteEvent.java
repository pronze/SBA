package io.github.pronze.sba.events;

import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class SBAPlayerPartyInviteEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final SBAPlayerWrapper player;
    private final SBAPlayerWrapper invited;
    private boolean cancelled;


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBAPlayerPartyInviteEvent.handlerList;
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
