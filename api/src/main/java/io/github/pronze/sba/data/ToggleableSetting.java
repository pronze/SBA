package io.github.pronze.sba.data;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ToggleableSetting<T extends Enum<T>> {

    public static <T extends Enum<T>> ToggleableSetting<T> of(Class<T> settingClass) {
        return new ToggleableSetting<>();
    }

    private final Set<T> settingSet = new HashSet<>();

    public ToggleableSetting<T> enable(@NotNull T setting) {
        settingSet.add(setting);
     
        return this;
    }

    public ToggleableSetting<T> disable(@NotNull T setting) {
        settingSet.remove(setting);
     
        return this;
    }

    public ToggleableSetting<T> toggle(@NotNull T setting) {
        if (settingSet.contains(setting)) {
            disable(setting);
        } else {
            enable(setting);
        }
        return this;
    }

    public Set<T> getAll() {
        return Set.copyOf(settingSet);
    }

    public boolean isToggled(@NotNull T status) {
        return settingSet.contains(status);
    }
}
