package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.wrapper.game.GameWrapper;
import io.github.pronze.sba.manager.GameTaskManager;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service(initAnother = {
        GeneratorTask.class,
        HealPoolTask.class,
        MinerTrapTask.class,
        TrapTask.class
})
public class GameTaskManagerImpl implements GameTaskManager {

    public static GameTaskManagerImpl getInstance() {
        return ServiceManager.get(GameTaskManagerImpl.class);
    }

    private final List<GameTask> tasks = new ArrayList<>();

    @Override
    public void addTask(@NotNull GameTask task) {
        if (tasks.contains(task)) {
            throw new UnsupportedOperationException("TaskManager already contains task: " + task.getClass().getSimpleName());
        }
        tasks.add(task);
    }

    @Override
    public void removeTask(@NotNull GameTask task) {
        if (!tasks.contains(task)) {
            return;
        }
        tasks.remove(task);
    }

    @Override
    public List<GameTask> startTasks(@NotNull GameWrapper arena) {
        return tasks.stream()
                .map(task -> task.start(arena))
                .collect(Collectors.toList());
    }
}
