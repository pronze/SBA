package io.github.pronze.sba.game;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.RunningTeam;

import java.util.Optional;

/**
 * Represents a GameStorage implementation.
 */
public interface IGameStorage {

    /**
     * Gets the target block location of the specified team.
     * @param team the team instance to query
     * @return an optional containing the location if team has been registered, empty otherwise
     */
    Optional<Location> getTargetBlockLocation(@NotNull RunningTeam team);

    /**
     * Gets the level of sharpness of the specified team.
     * @param team the team instance to query
     * @return an optional containing the sharpness level if the team has been registered, empty otherwise
     */
    Optional<Integer> getSharpnessLevel(@NotNull RunningTeam team);

    /**
     * Gets the level of protection of the specified team.
     * @param team the team instance to query
     * @return an optional containing the protection level if the team has been registered, empty otherwise
     */
    Optional<Integer> getProtectionLevel(@NotNull RunningTeam team);

     /**
     * Gets the level of protection of the specified team.
     * @param team the team instance to query
     * @return an optional containing the protection level if the team has been registered, empty otherwise
     */
    Optional<Integer> getKnockbackLevel(@NotNull RunningTeam team);

    /**
     * Gets the level of efficiency of the specified team.
     * @param team the team instance to query
     * @return an optional containing the efficiency level if the team has been registered, empty otherwise
     */
    Optional<Integer> getEfficiencyLevel(@NotNull RunningTeam team);
    Optional<Integer> getEnchantLevel(RunningTeam team, String propertyName);
    void setEnchantLevel(RunningTeam team, String propertyName, @NotNull Integer level);
    /**
     *
     * @param team
     * @param level
     */
    void setSharpnessLevel(@NotNull RunningTeam team, @NotNull Integer level);

    /**
     *
     * @param team the team instance to query
     * @param level
     */
    void setProtectionLevel(@NotNull RunningTeam team, @NotNull Integer level);

    /**
     * Sets the level of efficiency of the specified team.
     * @param team the team instance to set
     * @param level the level to set
     */
    void setEfficiencyLevel(@NotNull RunningTeam team, @NotNull Integer level);
 
    /**
     * Sets the level of efficiency of the specified team.
     * @param team the team instance to set
     * @param level the level to set
     */
    void setKnockbackLevel(@NotNull RunningTeam team, @NotNull Integer level);

    /**
     * Sets whether the trap has been enabled for the specified team.
     * @param team the team instance to set
     * @param isBlindTrapEnabled the trap boolean to be set
     */
    void setPurchasedBlindTrap(@NotNull RunningTeam team, boolean isBlindTrapEnabled);

    /**
     * Sets whether the trap has been enabled for the specified team.
     * @param team the team instance to set
     * @param isMinerTrapEnabled the trap boolean to be set
     */
    void setPurchasedMinerTrap(@NotNull RunningTeam team, boolean isMinerTrapEnabled);

    /**
     * Sets whether the pool has been enabled for the specified team.
     * @param team the team instance to set
     * @param isPoolEnabled the pool boolean to be set
     */
    void setPurchasedPool(@NotNull RunningTeam team, boolean isPoolEnabled);

    /**
     * Sets whether the dragons has been enabled for the specified team.
     * @param team the team instance to set
     * @param isDragonsEnabled the dragon boolean to be set
     */
    void setPurchasedDragons(@NotNull RunningTeam team, boolean isDragonsEnabled);

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
    boolean areBlindTrapEnabled(@NotNull RunningTeam team);

    /**
     * Returns a boolean if the traps are enabled for the specified team.
     * @param team the team instance to query
     * @return true if traps are enabled for the specified team, false otherwise
     */
    boolean areMinerTrapEnabled(@NotNull RunningTeam team);

    /**
     * Returns a boolean if the pool are enabled for the specified team.
     * @param team the team instance to query
     * @return true if pools are enabled for the specified team, false otherwise
     */
    boolean arePoolEnabled(@NotNull RunningTeam team);

    /**
     * Returns a boolean if the dragons are enabled for the specified team.
     * @param team the team instance to query
     * @return true if dragons are enabled for the specified team, false otherwise
     */
    boolean areDragonsEnabled(@NotNull RunningTeam team);

    void setPurchasedTrap(RunningTeam team, boolean b, String trap_identifier);

    boolean areTrapEnabled(RunningTeam team, String trap_identifier);

   
}
