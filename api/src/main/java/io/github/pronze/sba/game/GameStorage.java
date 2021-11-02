package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GameTeamData;
import io.github.pronze.sba.wrapper.team.RunningTeamWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a GameStorage implementation.
 */
public interface GameStorage {

    /**
     * Gets the level of sharpness of the specified team.
     * @param team the team instance to query
     * @return an optional containing the sharpness level if the team has been registered, empty otherwise
     */
    Optional<Integer> getSharpnessLevel(@NotNull RunningTeamWrapper team);

    /**
     * Gets the level of protection of the specified team.
     * @param team the team instance to query
     * @return an optional containing the protection level if the team has been registered, empty otherwise
     */
    Optional<Integer> getProtectionLevel(@NotNull RunningTeamWrapper team);

    /**
     * Gets the level of efficiency of the specified team.
     * @param team the team instance to query
     * @return an optional containing the efficiency level if the team has been registered, empty otherwise
     */
    Optional<Integer> getEfficiencyLevel(@NotNull RunningTeamWrapper team);

    /**
     *
     * @param team
     * @param level
     */
    void setSharpnessLevel(@NotNull RunningTeamWrapper team, @NotNull Integer level);

    /**
     *
     * @param team the team instance to query
     * @param level
     */
    void setProtectionLevel(@NotNull RunningTeamWrapper team, @NotNull Integer level);

    /**
     * Sets the level of efficiency of the specified team.
     * @param team the team instance to set
     * @param level the level to set
     */
    void setEfficiencyLevel(@NotNull RunningTeamWrapper team, @NotNull Integer level);

    /**
     * Sets whether the trap has been enabled for the specified team.
     * @param team the team instance to set
     * @param isBlindTrapEnabled the trap boolean to be set
     */
    void setPurchasedBlindTrap(@NotNull RunningTeamWrapper team, boolean isBlindTrapEnabled);

    /**
     * Sets whether the trap has been enabled for the specified team.
     * @param team the team instance to set
     * @param isMinerTrapEnabled the trap boolean to be set
     */
    void setPurchasedMinerTrap(@NotNull RunningTeamWrapper team, boolean isMinerTrapEnabled);

    /**
     * Sets whether the pool has been enabled for the specified team.
     * @param team the team instance to set
     * @param isPoolEnabled the pool boolean to be set
     */
    void setPurchasedPool(@NotNull RunningTeamWrapper team, boolean isPoolEnabled);

    /**
     * Sets whether the dragons has been enabled for the specified team.
     * @param team the team instance to set
     * @param isDragonsEnabled the dragon boolean to be set
     */
    void setPurchasedDragons(@NotNull RunningTeamWrapper team, boolean isDragonsEnabled);

    /**
     * Returns a boolean if the blindness trap are enabled for any teams in the game.
     * @return true if traps are enabled for any teams in the game, false otherwise
     */
    boolean areBlindTrapEnabled();

    /**
     * Returns a boolean if the miner trap are enabled for any teams in the game.
     * @return true if traps are enabled for any teams in the game, false otherwise
     */
    boolean areMinerTrapEnabled();

    /**
     * Returns a boolean if the pools are enabled for any teams in the game.
     * @return true if pools are enabled for any teams in the game, false otherwise
     */
    boolean arePoolEnabled();

    /**
     * Returns a boolean if the dragons are enabled for any teams in the game.
     * @return true if dragons are enabled for any teams in the game, false otherwise
     */
    boolean areDragonsEnabled();

    /**
     * Returns a boolean if the blindness trap are enabled for the specified team.
     * @param team the team instance to query
     * @return true if traps are enabled for the specified team, false otherwise
     */
    boolean areBlindTrapEnabled(@NotNull RunningTeamWrapper team);

    /**
     * Returns a boolean if the traps are enabled for the specified team.
     * @param team the team instance to query
     * @return true if traps are enabled for the specified team, false otherwise
     */
    boolean areMinerTrapEnabled(@NotNull RunningTeamWrapper team);

    /**
     * Returns a boolean if the pool are enabled for the specified team.
     * @param team the team instance to query
     * @return true if pools are enabled for the specified team, false otherwise
     */
    boolean arePoolEnabled(@NotNull RunningTeamWrapper team);

    /**
     * Returns a boolean if the dragons are enabled for the specified team.
     * @param team the team instance to query
     * @return true if dragons are enabled for the specified team, false otherwise
     */
    boolean areDragonsEnabled(@NotNull RunningTeamWrapper team);

    void start();

    void clear();

    Optional<GameTeamData> getTeamData(@NotNull RunningTeamWrapper team);
}
