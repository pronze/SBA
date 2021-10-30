package io.github.pronze.sba.game;

import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the different types of GameMode available.
 */
public enum GameMode {
    UNKNOWN,
    SOLOS,
    DOUBLES,
    TRIPLES,
    SQUADS;

    private static final List<GameMode> VALUES = List.of(values());

    public String strVal() {
        return toString().substring(0, 1).toUpperCase() + toString().substring(1).toLowerCase();
    }

    public static GameMode fromName(@NotNull String name) {
        return VALUES
                .stream()
                .filter(value -> value.toString().equals(name.toUpperCase()))
                .findAny()
                .orElse(GameMode.SOLOS);
    }

    public static GameMode fromInt(int mode) {
        return VALUES
                .stream()
                .filter(value -> value.ordinal() == mode)
                .findAny()
                .orElse(GameMode.UNKNOWN);
    }
}
