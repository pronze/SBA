package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.manager.GameTaskManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTask implements Runnable {
    @NotNull
    protected final Arena arena;

    public AbstractTask(@NotNull Arena arena) {
        this.arena = arena;
        GameTaskManager.getInstance().addTask(this.getClass());
    }
}
