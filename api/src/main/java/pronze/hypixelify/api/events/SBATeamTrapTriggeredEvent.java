package pronze.hypixelify.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.game.IArena;
import org.screamingsandals.bedwars.api.RunningTeam;

@RequiredArgsConstructor
@Getter
public class SBATeamTrapTriggeredEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private final Player trapped;
    private final RunningTeam team;
    private final IArena arena;
    private boolean cancelled = false;

    public static HandlerList getHandlerList() {
        return SBATeamTrapTriggeredEvent.handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return SBATeamTrapTriggeredEvent.handlerList;
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
