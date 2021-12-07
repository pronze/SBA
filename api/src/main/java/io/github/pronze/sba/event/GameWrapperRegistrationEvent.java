package io.github.pronze.sba.event;

import io.github.pronze.sba.game.GameWrapper;
import lombok.Data;
import org.screamingsandals.lib.event.CancellableAbstractEvent;

@Data
public class GameWrapperRegistrationEvent extends CancellableAbstractEvent {
    private final GameWrapper game;
}
