package io.github.pronze.sba.data;

import io.github.pronze.sba.game.IArena;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskerTask;
import org.screamingsandals.lib.utils.BasicWrapper;
import org.screamingsandals.lib.utils.reflect.Reflect;

@Getter
public class GameTaskData<T extends Runnable> extends BasicWrapper<T> {
    private final Class<T> taskClass;
    private final TaskerTask taskerTask;

    @SuppressWarnings("unchecked")
    public GameTaskData(@NotNull Class<T> taskClass, @NotNull IArena arena) {
        super((T) Reflect.constructor(taskClass, IArena.class).construct(arena));
        this.taskClass = taskClass;
        this.taskerTask = Tasker.build(as(Runnable.class)).repeat(1L, TaskerTime.SECONDS).start();
    }
}
