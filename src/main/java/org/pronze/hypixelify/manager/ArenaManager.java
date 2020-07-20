package org.pronze.hypixelify.manager;

import org.pronze.hypixelify.arena.Arena;

import java.util.HashMap;
import java.util.Map;

public class ArenaManager {
    private Map<String, Arena> arenas = new HashMap<>();

    public void addArena(String game, Arena arena) {
        arenas.put(game, arena);
    }

    public void removeArena(String game) {
        arenas.remove(game);
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }
}