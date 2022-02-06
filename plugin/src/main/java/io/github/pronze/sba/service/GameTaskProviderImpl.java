package io.github.pronze.sba.service;

import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.game.task.GameTask;
import io.github.pronze.sba.game.task.GeneratorTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public final class GameTaskProviderImpl implements GameTaskProvider {
    private final List<Supplier<GameTask>> suppliers = new ArrayList<>();

    public GameTaskProviderImpl() {
        // default tasks
        suppliers.add(GeneratorTask::new);
    }

    public void addTask(@NotNull Supplier<GameTask> gameTaskSupplier) {
        if (suppliers.contains(gameTaskSupplier)) {
            return;
        }
        suppliers.add(gameTaskSupplier);
    }

    @NotNull
    public List<GameTask> getNewTasks() {
        return suppliers
                .stream()
                .map(Supplier::get)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<GameTask> startTasks(@NotNull GameWrapper gameWrapper) {
        return suppliers
                .stream()
                .map(Supplier::get)
                .map(gameTask -> gameTask.start(gameWrapper))
                .collect(Collectors.toList());
    }

    @Provider
    @NotNull
    public List<Supplier<GameTask>> getAllSuppliers() {
        return List.copyOf(suppliers);
    }
}
