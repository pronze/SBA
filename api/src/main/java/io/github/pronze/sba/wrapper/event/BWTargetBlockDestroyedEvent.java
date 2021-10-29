package io.github.pronze.sba.wrapper.event;

import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.wrapper.RunningTeamWrapper;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.AbstractEvent;

import javax.annotation.Nullable;

@RequiredArgsConstructor
@Getter
public class BWTargetBlockDestroyedEvent extends AbstractEvent {
    private final GameWrapper game;
    @Nullable
    private final SBAPlayerWrapper destroyer;
    private final RunningTeamWrapper destroyedTeam;
}
