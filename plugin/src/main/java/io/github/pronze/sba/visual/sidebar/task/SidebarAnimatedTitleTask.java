package io.github.pronze.sba.visual.sidebar.task;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.tasker.task.TaskBase;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class SidebarAnimatedTitleTask implements Runnable {
    private final Sidebar sidebar;
    private final List<Component> lines;
    private final Supplier<Boolean> conditionalFunction;
    private final TaskBase taskBase;
    private int index = 0;

    @Override
    public void run() {
        if (!conditionalFunction.get()) {
            taskBase.cancel();
            return;
        }

        if (index >= lines.size()) {
            index = 0;
        }

        sidebar.title(lines.get(index));
        index += 1;
    }
}
