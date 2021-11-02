package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GameTeamData;
import io.github.pronze.sba.wrapper.game.GameWrapper;
import io.github.pronze.sba.wrapper.team.RunningTeamWrapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class GameStorageImpl implements GameStorage {
    private final Map<RunningTeamWrapper, GameTeamData> teamDataMap = new HashMap<>();
    private final GameWrapper gameWrapper;

    @Override
    public void start() {
        gameWrapper.getRunningTeams().forEach(team -> teamDataMap.put(team, GameTeamData.of(team)));
    }

    @Override
    public void clear() {
        teamDataMap.clear();
    }

    @Override
    public Optional<GameTeamData> getTeamData(@NotNull RunningTeamWrapper team) {
        return Optional.ofNullable(teamDataMap.get(team));
    }

    @Override
    public Optional<Integer> getSharpnessLevel(@NotNull RunningTeamWrapper team) {
        if (!teamDataMap.containsKey(team)) {
            return Optional.empty();
        }
        return Optional.of(teamDataMap.get(team).getSharpness());
    }

    @Override
    public Optional<Integer> getProtectionLevel(@NotNull RunningTeamWrapper team) {
        if (!teamDataMap.containsKey(team)) {
            return Optional.empty();
        }
        return Optional.of(teamDataMap.get(team).getProtection());
    }

    @Override
    public Optional<Integer> getEfficiencyLevel(@NotNull RunningTeamWrapper team) {
        if (!teamDataMap.containsKey(team)) {
            return Optional.empty();
        }
        return Optional.of(teamDataMap.get(team).getEfficiency());
    }

    @Override
    public void setSharpnessLevel(@NotNull RunningTeamWrapper team, @NotNull Integer level) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setSharpness(level);
    }

    @Override
    public void setProtectionLevel(@NotNull RunningTeamWrapper team, @NotNull Integer level) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setProtection(level);
    }

    @Override
    public void setEfficiencyLevel(@NotNull RunningTeamWrapper team, @NotNull Integer level) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setEfficiency(level);
    }

    @Override
    public void setPurchasedBlindTrap(@NotNull RunningTeamWrapper team, boolean isBlindTrapEnabled) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedBlindTrap(isBlindTrapEnabled);
    }

    @Override
    public void setPurchasedMinerTrap(@NotNull RunningTeamWrapper team, boolean isMinerTrapEnabled) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedBlindTrap(isMinerTrapEnabled);
    }

    @Override
    public void setPurchasedPool(@NotNull RunningTeamWrapper team, boolean isPoolEnabled) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedPool(isPoolEnabled);
    }

    @Override
    public void setPurchasedDragons(@NotNull RunningTeamWrapper team, boolean isDragonEnabled) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        teamDataMap.get(team).setPurchasedDragonUpgrade(isDragonEnabled);
    }

    @Override
    public boolean areBlindTrapEnabled(@NotNull RunningTeamWrapper team) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        return teamDataMap.get(team).isPurchasedBlindTrap();
    }

    @Override
    public boolean areMinerTrapEnabled(@NotNull RunningTeamWrapper team) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        return teamDataMap.get(team).isPurchasedMinerTrap();
    }

    @Override
    public boolean arePoolEnabled(@NotNull RunningTeamWrapper team) {
        if (!teamDataMap.containsKey(team)) {
            throw new UnsupportedOperationException("Team: " + team.getName() + " has not been registered yet!");
        }
        return teamDataMap.get(team).isPurchasedPool();
    }

    @Override
    public boolean areDragonsEnabled(@NotNull RunningTeamWrapper team) {
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
