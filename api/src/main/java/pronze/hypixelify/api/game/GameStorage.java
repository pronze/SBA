package pronze.hypixelify.api.game;

import org.bukkit.Location;
import org.screamingsandals.bedwars.api.RunningTeam;

public interface GameStorage {

    Location getTargetBlockLocation(RunningTeam rt);

    Integer getSharpness(String team);

    Integer getProtection(String team);

    void setTrap(RunningTeam rt, boolean b);

    void setPool(RunningTeam rt, boolean b);

    void setSharpness(String teamName, Integer level);

    void setProtection(String teamName, Integer level);

    void setTargetBlockLocation(RunningTeam rt);

    boolean areTrapsEnabled();

    boolean arePoolEnabled();

    boolean isTrapEnabled(RunningTeam team);

    boolean isPoolEnabled(RunningTeam team);
}
