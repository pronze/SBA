package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.wrapper.game.GameWrapper;
import lombok.Data;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskerTask;

import java.util.UUID;

@Data
public abstract class AbstractGameTaskImpl implements GameTask {
    private final UUID uuid;
    protected GameWrapper gameWrapper;
    private long duration;
    private TaskerTime timeUnit;
    private TaskerTask task;
    private boolean started;

    public AbstractGameTaskImpl() {
        this.uuid = UUID.randomUUID();
        this.timeUnit = TaskerTime.SECONDS;
        this.duration = 1L;
        GameTaskManagerImpl.getInstance().addTask(this);
    }

    public AbstractGameTaskImpl start(GameWrapper gameWrapper) {
        this.gameWrapper = gameWrapper;
        if (task != null) {
            task.cancel();
        }
        task = Tasker.build(this::loopLogic).repeat(duration, timeUnit).start();
        return this;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        task = null;
    }

    protected void loopLogic() {
        if (gameWrapper.getStatus() != GameStatus.RUNNING) {
            task.cancel();
            task = null;
            return;
        }
        run();
    }

    protected abstract void run();
}
