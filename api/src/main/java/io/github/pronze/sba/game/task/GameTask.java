package io.github.pronze.sba.game.task;

import io.github.pronze.sba.game.GameWrapper;
import org.jetbrains.annotations.NotNull;

public interface GameTask {

    @NotNull
    GameTask start(@NotNull GameWrapper gameWrapper);

    void stop();

    boolean isRunning();
}
