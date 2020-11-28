package io.pronze.hypixelify.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import io.pronze.hypixelify.arena.Arena;
import org.screamingsandals.bedwars.api.RunningTeam;

public class TeamTrapTriggeredEvent extends Event implements Cancellable {
    private static HandlerList handlerList = new HandlerList();

    private Player trapped;
    private RunningTeam team;
    private Arena arena;
    private boolean cancelled = false;

    public TeamTrapTriggeredEvent(Player trapped,
                                  RunningTeam team,
                                  Arena arena) {
        this.trapped = trapped;
        this.team = team;
        this.arena = arena;
    }

    public static HandlerList getHandlerList() {
        return TeamTrapTriggeredEvent.handlerList;
    }

    public Player getTrapped() {
        return trapped;
    }

    public Arena getArena() {
        return arena;
    }

    public RunningTeam getTeam() {
        return team;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return TeamTrapTriggeredEvent.handlerList;
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
