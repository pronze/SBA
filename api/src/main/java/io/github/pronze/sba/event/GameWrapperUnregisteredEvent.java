package io.github.pronze.sba.event;

import io.github.pronze.sba.game.GameWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.screamingsandals.lib.event.AbstractEvent;
import org.screamingsandals.lib.event.SCancellableEvent;

@EqualsAndHashCode(callSuper = false)
@Data
public class GameWrapperUnregisteredEvent implements SCancellableEvent {
    private final GameWrapper game;
    private boolean cancelled;
}
