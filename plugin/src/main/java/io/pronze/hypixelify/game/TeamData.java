package io.pronze.hypixelify.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;

@AllArgsConstructor
@Data
public class TeamData {
     private int sharpness;
     private int protection;
     private boolean purchasedPool;
     private boolean purchasedTrap;
     private boolean purchasedDragonUpgrade;
     private Location targetBlockLoc;
}
