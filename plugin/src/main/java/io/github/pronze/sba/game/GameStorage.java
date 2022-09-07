package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GameTeamData;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GameStorage implements IGameStorage {
    private final Map<RunningTeam, GameTeamData> teamDataMap = new HashMap<>();

    public GameStorage(Game game) {
        game.getRunningTeams().forEach(team -> teamDataMap.put(team, GameTeamData.of(team)));
    }

    @Override
    public Optional<Location> getTargetBlockLocation(@NotNull RunningTeam team) {
        if (team == null)
            return Optional.empty();
        if (!teamDataMap.containsKey(team)) {
            teamDataMap.put(team, GameTeamData.of(team));
        }
        return Optional.ofNullable(teamDataMap.get(team).getTargetBlockLoc());
    }

    @Override
    public Optional<Integer> getSharpnessLevel(@NotNull RunningTeam team) {
        if (team == null)
            return Optional.empty();
        if (!teamDataMap.containsKey(team)) {
            teamDataMap.put(team, GameTeamData.of(team));
        }
        return Optional.of(teamDataMap.get(team).getSharpness());
    }

    @Override
    public Optional<Integer> getProtectionLevel(@NotNull RunningTeam team) {
        if (team == null)
            return Optional.empty();
        if (!teamDataMap.containsKey(team)) {
            teamDataMap.put(team, GameTeamData.of(team));
        }
        return Optional.of(teamDataMap.get(team).getProtection());
    }

    @Override
    public Optional<Integer> getEfficiencyLevel(@NotNull RunningTeam team) {
        if (team == null)
            return Optional.empty();
        if (!teamDataMap.containsKey(team)) {
            teamDataMap.put(team, GameTeamData.of(team));
        }
        return Optional.of(teamDataMap.get(team).getEfficiency());
    }

    @Override
    public Optional<Integer> getKnockbackLevel(@NotNull RunningTeam team) {
        if (team == null)
            return Optional.empty();
        if (!teamDataMap.containsKey(team)) {
            teamDataMap.put(team, GameTeamData.of(team));
        }
        return Optional.of(teamDataMap.get(team).getKnockback());
    }

    @Override
    public Optional<Integer> getEnchantLevel(RunningTeam team, String propertyName) {
        if (team == null)
            return Optional.empty();
        if (!teamDataMap.containsKey(team)) {
            teamDataMap.put(team, GameTeamData.of(team));
        }
        return Optional.of(teamDataMap.get(team).get(propertyName));
    }

    @Override
    public void setEnchantLevel(RunningTeam team, String propertyName, @NotNull Integer level) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).set(propertyName,level);
    }

    @Override
    public void setSharpnessLevel(@NotNull RunningTeam team, @NotNull Integer level) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setSharpness(level);
    }

    @Override
    public void setProtectionLevel(@NotNull RunningTeam team, @NotNull Integer level) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setProtection(level);
    }

    @Override
    public void setEfficiencyLevel(@NotNull RunningTeam team, @NotNull Integer level) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setEfficiency(level);
    }

    @Override
    public void setKnockbackLevel(@NotNull RunningTeam team, @NotNull Integer level) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setKnockback(level);
    }

    @Override
    public void setPurchasedBlindTrap(@NotNull RunningTeam team, boolean isBlindTrapEnabled) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedBlindTrap(isBlindTrapEnabled);
    }

    @Override
    public void setPurchasedMinerTrap(@NotNull RunningTeam team, boolean isMinerTrapEnabled) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedBlindTrap(isMinerTrapEnabled);
    }

    @Override
    public void setPurchasedPool(@NotNull RunningTeam team, boolean isPoolEnabled) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedPool(isPoolEnabled);
    }

    @Override
    public void setPurchasedDragons(@NotNull RunningTeam team, boolean isDragonEnabled) {
        if (team == null)
            return;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedDragonUpgrade(isDragonEnabled);
    }

    @Override
    public boolean areBlindTrapEnabled(@NotNull RunningTeam team) {
        if (team == null)
            return false;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        return teamDataMap.get(team).isPurchasedBlindTrap();
    }

    @Override
    public boolean areMinerTrapEnabled(@NotNull RunningTeam team) {
        if (team == null)
            return false;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        return teamDataMap.get(team).isPurchasedMinerTrap();
    }

    @Override
    public boolean arePoolEnabled(@NotNull RunningTeam team) {
        if (team == null)
            return false;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        return teamDataMap.get(team).isPurchasedPool();
    }

    @Override
    public boolean areDragonsEnabled(@NotNull RunningTeam team) {
        if (team == null)
            return false;
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        return teamDataMap.get(team).isPurchasedDragonUpgrade();
    }

    @Override
    public boolean areDragonsEnabled() {
        return teamDataMap
                .values()
                .stream()
                .anyMatch(GameTeamData::isPurchasedDragonUpgrade);
    }

    @Override
    public boolean areBlindTrapEnabled() {
        return teamDataMap
                .values()
                .stream()
                .anyMatch(GameTeamData::isPurchasedBlindTrap);
    }

    @Override
    public boolean areMinerTrapEnabled() {
        return teamDataMap
                .values()
                .stream()
                .anyMatch(GameTeamData::isPurchasedMinerTrap);
    }

    @Override
    public boolean arePoolEnabled() {
        return teamDataMap
                .values()
                .stream()
                .anyMatch(GameTeamData::isPurchasedPool);
    }

}
