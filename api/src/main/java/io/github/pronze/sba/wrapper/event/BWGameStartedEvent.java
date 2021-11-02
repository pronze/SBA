package io.github.pronze.sba.wrapper.event;

import io.github.pronze.sba.wrapper.game.GameWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.AbstractEvent;

@RequiredArgsConstructor
@Getter
public class BWGameStartedEvent extends AbstractEvent {
    private final GameWrapper game;
}
