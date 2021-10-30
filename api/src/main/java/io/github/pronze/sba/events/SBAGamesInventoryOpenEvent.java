package io.github.pronze.sba.events;

import io.github.pronze.sba.game.GameMode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.CancellableAbstractEvent;
import org.screamingsandals.lib.player.PlayerWrapper;

@RequiredArgsConstructor
@Getter
public class SBAGamesInventoryOpenEvent extends CancellableAbstractEvent {
    private final PlayerWrapper player;
    private final GameMode mode;
}
