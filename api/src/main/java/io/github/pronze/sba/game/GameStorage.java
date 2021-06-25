package io.github.pronze.sba.game;

import org.bukkit.Location;
import org.screamingsandals.bedwars.api.RunningTeam;

public interface GameStorage {

    /**
     *
     * @param rt
     * @return
     */
    Location getTargetBlockLocation(RunningTeam rt);

    /**
     *
     * @param team
     * @return
     */
    Integer getSharpness(String team);

    /**
     *
     * @param team
     * @return
     */
    Integer getProtection(String team);

    /**
     *
     * @param name
     * @return
     */
    Integer getEfficiency(String name);

    /**
     *
     * @param rt
     * @param level
     */
    void setEfficiency(RunningTeam rt, Integer level);

    /**
     *
     * @param rt
     * @param b
     */
    void setTrap(RunningTeam rt, boolean b);

    /**
     *
     * @param rt
     * @param b
     */
    void setPool(RunningTeam rt, boolean b);

    /**
     *
     * @param teamName
     * @param level
     */
    void setSharpness(String teamName, Integer level);

    /**
     *
     * @param teamName
     * @param level
     */
    void setProtection(String teamName, Integer level);

    /**
     *
     * @return
     */
    boolean areTrapsEnabled();

    /**
     *
     * @return
     */
    boolean arePoolEnabled();

    /**
     *
     * @param team
     * @return
     */
    boolean isTrapEnabled(RunningTeam team);

    /**
     *
     * @param team
     * @return
     */
    boolean isPoolEnabled(RunningTeam team);

}
