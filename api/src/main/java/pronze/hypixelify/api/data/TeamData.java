package pronze.hypixelify.api.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.screamingsandals.bedwars.api.Team;

@RequiredArgsConstructor
@Data
public class TeamData {
     private int sharpness;
     private int protection;
     private int efficiency;
     private boolean purchasedPool;
     private boolean purchasedTrap;
     private boolean purchasedDragonUpgrade;
     private final Location targetBlockLoc;

     public static TeamData from(Team team) {
          return new TeamData(team.getTargetBlock());
     }
}
