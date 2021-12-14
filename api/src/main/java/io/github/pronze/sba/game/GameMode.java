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

    public int intVal() {
        switch (this) {
            case SOLOS:
                return 1;
            case DOUBLES:
                return 2;
            case TRIPLES:
                return 3;
            case SQUADS:
                return 4;
        }
        return 1;
    }

    public static GameMode fromName(@NotNull String name) {
        return Arrays.stream(values())
                .filter(value -> value.toString().equals(name.toUpperCase()))
                .findAny()
                .orElse(GameMode.SOLOS);
    }

    public static GameMode fromInt(@NotNull int value) {
        switch (value) {
            case 1:
                return GameMode.SOLOS;
            case 2:
                return GameMode.DOUBLES;
            case 3:
                return GameMode.TRIPLES;
            case 4:
                return GameMode.SQUADS;
        }
        return GameMode.SOLOS;
    }
}
