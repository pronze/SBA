package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GameTeamData;
import org.bukkit.Location;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.HashMap;
import java.util.Map;

public class GameStorage {
    private final Map<String, GameTeamData> teamDataMap = new HashMap<>();

    public GameStorage(Game game) {
        game.getRunningTeams().forEach(team -> teamDataMap.put(team.getName(), GameTeamData.from(team)));
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

    public Integer getEfficiency(String team) { return teamDataMap.get(team).getEfficiency(); }

    public void setEfficiency(RunningTeam rt, Integer level) {
        teamDataMap.get(rt.getName()).setEfficiency(level);
    }

    public void setTrap(RunningTeam rt, boolean b) {
        if (!teamDataMap.containsKey(rt.getName())) return;
        final var data = teamDataMap.get(rt.getName());
        data.setPurchasedTrap(b);
    }

    public void setPool(RunningTeam rt, boolean b) {
        if (!teamDataMap.containsKey(rt.getName())) return;
        final var data = teamDataMap.get(rt.getName());
        if (data != null) data.setPurchasedPool(b);
    }

    public void setDragon(RunningTeam rt, boolean b) {
        if (!teamDataMap.containsKey(rt.getName())) return;
        final var data = teamDataMap.get(rt.getName());
        data.setPurchasedDragonUpgrade(b);
    }

    public void setSharpness(String teamName, Integer level) {
        if (!teamDataMap.containsKey(teamName)) return;
        final var data = teamDataMap.get(teamName);
        data.setSharpness(level);
    }

    public void setProtection(String teamName, Integer level) {
        if (!teamDataMap.containsKey(teamName)) return;
        final var data = teamDataMap.get(teamName);
        data.setProtection(level);
    }

    public boolean areDragonsEnabled() {
        return teamDataMap
                .values()
                .stream()
                .anyMatch(GameTeamData::isPurchasedDragonUpgrade);
    }

    public boolean areTrapsEnabled() {
        return teamDataMap
                .values()
                .stream()
                .anyMatch(GameTeamData::isPurchasedTrap);
    }

    public boolean arePoolEnabled() {
        return teamDataMap
                .values()
                .stream()
                .anyMatch(GameTeamData::isPurchasedPool);
    }

    public boolean isTrapEnabled(RunningTeam team) {
        if (!teamDataMap.containsKey(team.getName())) return false;
        final var data = teamDataMap.get(team.getName());
        return data.isPurchasedTrap();
    }

    public boolean isPoolEnabled(RunningTeam team) {
        if (!teamDataMap.containsKey(team.getName())) return false;
        final var data = teamDataMap.get(team.getName());
        return data.isPurchasedPool();
    }

    public boolean isDragonEnabled(RunningTeam team) {
        if (!teamDataMap.containsKey(team.getName())) return false;
        final var data = teamDataMap.get(team.getName());
        return data.isPurchasedDragonUpgrade();
    }
}
