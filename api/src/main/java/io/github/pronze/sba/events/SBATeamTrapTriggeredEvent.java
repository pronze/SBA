package io.github.pronze.sba.events;

import io.github.pronze.sba.game.GameWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.RunningTeam;

@RequiredArgsConstructor
@Getter
public class SBATeamTrapTriggeredEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private final Player trapped;
    private final RunningTeam team;
    private final GameWrapper arena;
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
