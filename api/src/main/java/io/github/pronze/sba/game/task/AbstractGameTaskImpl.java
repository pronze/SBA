package io.github.pronze.sba.game.task;

import io.github.pronze.sba.game.GameWrapper;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskState;
import org.screamingsandals.lib.tasker.task.TaskerTask;
import java.util.UUID;

@Data
public abstract class AbstractGameTaskImpl implements GameTask {
    private final UUID uuid;
    protected GameWrapper gameWrapper;
    private long duration;
    private TaskerTime timeUnit;
    private TaskerTask task;

    public AbstractGameTaskImpl() {
        this.uuid = UUID.randomUUID();
        this.timeUnit = TaskerTime.SECONDS;
        this.duration = 1L;
    }

    @NotNull
    public GameTask start(@NotNull GameWrapper gameWrapper) {
        this.gameWrapper = gameWrapper;
        if (task != null) {
            task.cancel();
        }

        task = Tasker
                .build(this::loopLogic)
                .repeat(duration, timeUnit)
                .start();

        return this;
    }

    public void stop() {
        if (task != null
                && task.getState() != TaskState.CANCELLED) {
            task.cancel();
        }
        task = null;
    }

    public boolean isRunning() {
        return task != null
                && task.getState() == TaskState.RUNNING;
    }

    protected void loopLogic() {
        if (gameWrapper.getGame().getStatus() != GameStatus.RUNNING) {
            task.cancel();
            task = null;
            return;
        }
        run();
    }

    protected abstract void run();
}