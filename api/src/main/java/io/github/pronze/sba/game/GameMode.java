package io.github.pronze.sba.game;

import org.jetbrains.annotations.NotNull;
import java.util.Arrays;

/**
 * Represents the different types of GameMode available.
 */
public enum GameMode {
    SOLOS,
    DOUBLES,
    TRIPLES,
    SQUADS;

    public String strVal() {
        return toString().substring(0, 1).toUpperCase() + toString().substring(1).toLowerCase();
    }

    public static GameMode fromName(@NotNull String name) {
        return Arrays.stream(values())
                .filter(value -> value.toString().equals(name.toUpperCase()))
                .findAny()
                .orElse(GameMode.SOLOS);
    }
}
