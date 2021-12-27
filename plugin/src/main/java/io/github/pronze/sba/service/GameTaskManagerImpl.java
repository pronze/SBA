package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.game.task.GeneratorTask;
import io.github.pronze.sba.game.task.GameTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameTaskManagerImpl implements GameTaskManager {
    private final List<Class<? extends GameTask>> tasks = new ArrayList<>();

    public GameTaskManagerImpl() {
        addTask(GeneratorTask.class);
    }

    @Override
    public void addTask(@NotNull Class<? extends GameTask> taskClass) {
        if (tasks.contains(taskClass)) {
            throw new UnsupportedOperationException("TaskManager already contains task: " + taskClass.getSimpleName());
        }
        tasks.add(taskClass);
    }

    @Override
    public void removeTask(@NotNull Class<? extends GameTask> taskClass) {
        tasks.remove(taskClass);
    }

    @NotNull
    @Override
    public List<GameTask> startTasks(@NotNull GameWrapper gameWrapper) {
        return tasks.stream()
                .map(task -> (GameTask) Reflect.construct(task))
                .map(task -> task.start(gameWrapper))
                .peek(gameWrapper::registerGameTask)
                .collect(Collectors.toList());
    }
}
