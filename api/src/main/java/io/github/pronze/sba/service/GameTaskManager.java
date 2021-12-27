package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.game.task.GameTask;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Represents an GameTaskManager implementation.
 * All external tasks for a running game is managed by this GameTaskManager.
 */
public interface GameTaskManager {

    /**
     * Adds a task to the task queue. This task will be run for each game.
     * @param taskClass the task to be queued
     */
    void addTask(@NotNull Class<? extends GameTask> taskClass);

    /**
     * Removes a task from the task queue.
     * @param taskClass the task to remove
     */
    void removeTask(@NotNull Class<? extends GameTask> taskClass);

    /**
     * Starts the tasks for the given arena instance.
     * @param arena arena instance to construct the tasks
     * @return A list containing all the tasks initialized for the game
     */
    @NotNull
    List<GameTask> startTasks(@NotNull GameWrapper gameWrapper);
}