package io.github.pronze.sba.manager;
import io.github.pronze.sba.data.GameTaskData;
import io.github.pronze.sba.game.IArena;
import io.github.pronze.sba.game.tasks.GeneratorTask;
import io.github.pronze.sba.game.tasks.HealPoolTask;
import io.github.pronze.sba.game.tasks.TrapTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameTaskManager implements IGameTaskManager {

    public static GameTaskManager getInstance() {
        return ServiceManager.get(GameTaskManager.class);
    }

    private final List<Class<? extends Runnable>> tasks = new ArrayList<>();

    public GameTaskManager() {
        addTask(GeneratorTask.class);
        addTask(HealPoolTask.class);
        addTask(TrapTask.class);
    }
    @Override
    public void addTask(@NotNull Class<? extends Runnable> task) {
        if (tasks.contains(task)) {
            throw new UnsupportedOperationException("TaskManager already contains task: " + task.getSimpleName());
        }
        tasks.add(task);
    }

    @Override
    public void removeTask(@NotNull Class<? extends Runnable> task) {
        if (!tasks.contains(task)) {
            return;
        }
        tasks.remove(task);
    }

    @Override
    public List<GameTaskData<?>> startTasks(@NotNull IArena arena) {
        return tasks.stream()
                .map(taskClass -> new GameTaskData<>(taskClass, arena))
                .collect(Collectors.toList());
    }
}
