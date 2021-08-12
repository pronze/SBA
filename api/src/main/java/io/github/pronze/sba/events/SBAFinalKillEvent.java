package io.github.pronze.sba.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

@RequiredArgsConstructor
@Getter
public class SBAFinalKillEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Game game;
    private final Player victim;
    private final Player killer;

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return SBAFinalKillEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SBAFinalKillEvent.handlers;
    }
}
