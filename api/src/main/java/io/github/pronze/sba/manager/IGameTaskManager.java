package io.github.pronze.sba.manager;

import io.github.pronze.sba.game.IArena;
import io.github.pronze.sba.game.tasks.BaseGameTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an GameTaskManager implementation.
 * All external tasks for a running game is managed by this GameTaskManager.
 */
public interface IGameTaskManager {

    /**
     * Adds a task to the task queue. This task will be run for each game.
     * @param task the task to be queued <b>Note: The only constructor parameter this class should have is a Arena parameter.</b>
     */
    void addTask(@NotNull BaseGameTask task);

    /**
     * Removes a task from the task queue.
     * @param task the task to remove
     */
    void removeTask(@NotNull BaseGameTask task);

    /**
     * Starts the tasks for the given arena instance.
     * @param arena arena instance to construct the tasks
     * @return A list containing all the tasks initialized for the game
     */
    List<BaseGameTask> startTasks(@NotNull IArena arena);
}
