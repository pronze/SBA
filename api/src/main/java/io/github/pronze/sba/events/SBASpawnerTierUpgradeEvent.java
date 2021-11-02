package io.github.pronze.sba.events;

import io.github.pronze.sba.game.RotatingGenerator;
import io.github.pronze.sba.wrapper.game.GameWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.CancellableAbstractEvent;

@RequiredArgsConstructor
@Getter
public class SBASpawnerTierUpgradeEvent extends CancellableAbstractEvent {
    private final GameWrapper game;
    private final RotatingGenerator generator;
}
