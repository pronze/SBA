package io.github.pronze.sba.wrapper.event;

import io.github.pronze.sba.wrapper.game.GameWrapper;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.wrapper.team.TeamWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.AbstractEvent;

@RequiredArgsConstructor
@Getter
public class BWPlayerJoinedEvent extends AbstractEvent {
    private final GameWrapper game;
    private final SBAPlayerWrapper player;
    private final TeamWrapper team;
}
