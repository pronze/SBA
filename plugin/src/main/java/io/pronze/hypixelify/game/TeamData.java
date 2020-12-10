package io.pronze.hypixelify.game;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@RequiredArgsConstructor
@Data
public class TeamData {

    @NonNull private int sharpness;
    @NonNull private int protection;
    @NonNull private boolean purchasedPool;
    @NonNull private boolean purchasedTrap;
    @NonNull private Location targetBlockLoc;

}
