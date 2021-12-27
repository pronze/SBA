package io.github.pronze.sba.data;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class ToggleableSetting<T extends Enum<T>> {
    private final Set<T> settingSet = new HashSet<>();

    public void enable(@NotNull T setting) {
        settingSet.add(setting);
    }

    public void disable(@NotNull T setting) {
        settingSet.remove(setting);
    }

    public void toggle(@NotNull T setting) {
        if (settingSet.contains(setting)) {
            settingSet.remove(setting);
        } else {
            settingSet.add(setting);
        }
    }

    @NotNull
    public Set<T> getAll() {
        return Set.copyOf(settingSet);
    }

    public boolean isToggled(@NotNull T status) {
        return settingSet.contains(status);
    }
}
