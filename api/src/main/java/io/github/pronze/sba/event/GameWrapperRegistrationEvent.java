package io.github.pronze.sba.event;

import io.github.pronze.sba.game.GameWrapper;
import lombok.Data;
import org.screamingsandals.lib.event.CancellableAbstractEvent;
import org.screamingsandals.lib.event.SCancellableEvent;

@Data
public class GameWrapperRegistrationEvent implements SCancellableEvent {
    private final GameWrapper game;
    private boolean cancelled;
}
