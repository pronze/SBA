package pronze.hypixelify.api.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

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
}
