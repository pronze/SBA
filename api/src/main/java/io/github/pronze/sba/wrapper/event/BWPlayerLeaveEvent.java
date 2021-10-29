package io.github.pronze.sba.wrapper.event;

import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.wrapper.RunningTeamWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.AbstractEvent;
import org.screamingsandals.lib.player.PlayerWrapper;

@RequiredArgsConstructor
@Getter
public class BWPlayerLeaveEvent extends AbstractEvent {
    private final GameWrapper game;
    private final PlayerWrapper player;
    private final RunningTeamWrapper team;
}
