package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.game.task.GameTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;

import java.util.List;
import java.util.function.Supplier;

public interface GameTaskProvider {

    static GameTaskProvider getInstance() {
        return ServiceManager.get(GameTaskProvider.class);
    }

    void addTask(@NotNull Supplier<GameTask> gameTaskSupplier);

    List<GameTask> getNewTasks();

    @NotNull
    List<Supplier<GameTask>> getAllSuppliers();

    @NotNull
    List<GameTask> startTasks(@NotNull GameWrapper gameWrapper);
}
