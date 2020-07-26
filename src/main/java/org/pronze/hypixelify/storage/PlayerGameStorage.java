package org.pronze.hypixelify.storage;

import org.screamingsandals.bedwars.api.game.Game;
import java.util.HashMap;
import java.util.Map;

public class PlayerGameStorage {
    private Game game;

    private Map<String, Integer> totalkills;

    private Map<String, Integer> kills;

    private Map<String, Integer> finalkills;

    private Map<String, Integer> dies;

    private Map<String, Integer> beds;


    public PlayerGameStorage(Game game) {
        this.game = game;
        totalkills = new HashMap<>();
        kills = new HashMap<>();
        finalkills = new HashMap<>();
        dies = new HashMap<>();
        beds = new HashMap<>();
    }

    public Game getGame() {
        return game;
    }

    public Map<String, Integer> getPlayerTotalKills() {
        return totalkills;
    }

    public Map<String, Integer> getPlayerKills() {
        return kills;
    }

    public Map<String, Integer> getPlayerFinalKills() {
        return finalkills;
    }

    public Map<String, Integer> getPlayerDies() {
        return dies;
    }

    public Map<String, Integer> getPlayerBeds() {
        return beds;
    }

}
