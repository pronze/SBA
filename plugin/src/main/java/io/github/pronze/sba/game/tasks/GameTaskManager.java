package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.IArena;
import io.github.pronze.sba.manager.IGameTaskManager;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service(dependsOn = SBAConfig.class)
public class GameTaskManager implements IGameTaskManager {

    public static GameTaskManager getInstance() {
        return ServiceManager.get(GameTaskManager.class);
    }
    @Override
    public List<BaseGameTask> startTasks(@NotNull IArena arena) {
        List<BaseGameTask> l = new ArrayList<>(4);
        l.add(new GeneratorTask());
        l.add(new HealPoolTask());
        l.add(new TrapTask());
        l.add(new MinerTrapTask());

        return l.stream()
                .map(task -> task.start(arena))
                .collect(Collectors.toList());
    }
}
