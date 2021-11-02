package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.wrapper.game.GameWrapper;

public interface GameTask {
    GameTask start(GameWrapper arena);

    void stop();
}
