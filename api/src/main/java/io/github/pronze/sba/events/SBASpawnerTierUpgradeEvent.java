package io.github.pronze.sba.events;

import io.github.pronze.sba.game.RotatingGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

@RequiredArgsConstructor
@Getter
public class SBASpawnerTierUpgradeEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Game game;
    private final RotatingGenerator generator;
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return SBASpawnerTierUpgradeEvent.handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBASpawnerTierUpgradeEvent.handlerList;
    }
}
