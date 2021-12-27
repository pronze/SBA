package io.github.pronze.sba.event;

import io.github.pronze.sba.game.GamePlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.screamingsandals.lib.event.SCancellableEvent;

@EqualsAndHashCode(callSuper = false)
@Data
public class GamesInventoryOpenEvent implements SCancellableEvent {
    private final GamePlayer gamePlayer;
    private final String type;
    private boolean cancelled;
}
