package io.github.pronze.sba.events;

import io.github.pronze.sba.wrapper.game.GameWrapper;
import io.github.pronze.sba.wrapper.team.RunningTeamWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.CancellableAbstractEvent;
import org.screamingsandals.lib.player.PlayerWrapper;

@RequiredArgsConstructor
@Getter
public class SBATeamTrapTriggeredEvent extends CancellableAbstractEvent {
    private final PlayerWrapper trapped;
    private final RunningTeamWrapper team;
    private final GameWrapper game;
}
