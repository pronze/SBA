package io.github.pronze.sba.game;

import java.util.List;

public enum ArenaType {
    LOBBY,
    GAME,
    OUTSIDE,
    UNKNOWN;

    public static List<ArenaType> VALUES = List.of(values());
}
