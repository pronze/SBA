package io.github.pronze.sba.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.screamingsandals.bedwars.api.Team;

@RequiredArgsConstructor
@Data
public class GameTeamData {
     private int sharpness;
     private int protection;
     private int efficiency;
     private boolean purchasedPool;
     private boolean purchasedTrap;
     private boolean purchasedDragonUpgrade;
     private final Location targetBlockLoc;

     public static GameTeamData from(Team team) {
          return new GameTeamData(team.getTargetBlock());
     }
}
