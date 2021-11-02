package io.github.pronze.sba.events;

import io.github.pronze.sba.wrapper.game.GameWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.event.AbstractEvent;
import org.screamingsandals.lib.player.PlayerWrapper;

@RequiredArgsConstructor
@Getter
public class SBAFinalKillEvent extends AbstractEvent {
    private final GameWrapper game;
    private final PlayerWrapper victim;
    private final PlayerWrapper killer;
}
