package io.github.pronze.sba.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class SBAGamesInventoryOpenEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String mode;
    private boolean isCancelled = false;

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return SBAGamesInventoryOpenEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SBAGamesInventoryOpenEvent.handlers;
    }
}
