package io.pronze.hypixelify.game;

import org.bukkit.Location;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStorage implements io.pronze.hypixelify.api.game.GameStorage {

    private final Map<String, TeamData> teamDataMap = new HashMap<>();

    public GameStorage(Game game){
        game.getRunningTeams().forEach(team->{
                    teamDataMap.put(team.getName(),
                            new TeamData(0, 0, false, false, false,
                            team.getTargetBlock())); });
    }


    public Location getTargetBlockLocation(RunningTeam rt) {
        return teamDataMap.get(rt.getName()).getTargetBlockLoc();
    }

    public Integer getSharpness(String team) {
        return teamDataMap.get(team).getSharpness();
    }

    public Integer getProtection(String team) {
        return teamDataMap.get(team).getProtection();
    }

    public void setTrap(RunningTeam rt, boolean b) {
        final var data = teamDataMap.get(rt.getName());
        data.setPurchasedTrap(b);
    }

    public void setPool(RunningTeam rt, boolean b) {
        final var data = teamDataMap.get(rt.getName());
        data.setPurchasedPool(b);
    }

    public void setDragon(RunningTeam rt, boolean b) {
        final var data = teamDataMap.get(rt.getName());
        data.setPurchasedDragonUpgrade(b);
    }

    public void setSharpness(String teamName, Integer level) {
        final var data = teamDataMap.get(teamName);
        data.setSharpness(level);
    }

    public void setProtection(String teamName, Integer level) {
        final var data = teamDataMap.get(teamName);
        data.setProtection(level);
    }

    public void setTargetBlockLocation(RunningTeam rt) {
        final var data = teamDataMap.get(rt.getName());
        data.setTargetBlockLoc(rt.getTargetBlock());
    }

    public boolean areDragonsEnabled() {
        for(var data : teamDataMap.values()) {
            if (data == null) continue;
            if (data.isPurchasedDragonUpgrade()) {
                return true;
            }
        }

        return false;
    }

    public boolean areTrapsEnabled() {
        for (TeamData data : teamDataMap.values()) {
            if (data == null) continue;
            if (data.isPurchasedTrap()) {
                return true;
            }
        }
        return false;
    }

    public boolean arePoolEnabled() {
        for (var data : teamDataMap.values()) {
            if (data == null) continue;
            if (data.isPurchasedPool()) {
                return true;
            }
        }
        return false;
    }

    public boolean isTrapEnabled(RunningTeam team) {
        final var data = teamDataMap.get(team.getName());
        return data.isPurchasedTrap();
    }

    public boolean isPoolEnabled(RunningTeam team) {
        final var data = teamDataMap.get(team.getName());
        return data.isPurchasedPool();
    }

    public boolean isDragonEnabled(RunningTeam team) {
        final var data = teamDataMap.get(team.getName());
        return data.isPurchasedDragonUpgrade();
    }


}
