package io.github.pronze.sba.utils.citizens;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.pronze.sba.utils.Logger;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.CurrentLocation;

public class BridgePillarTrait extends Trait {

    public BridgePillarTrait() {
        super("BridgePillar");

    }

    private boolean isTracking = false;
    private BedwarsBlockPlace blockPlace;
    private LinkedList<Location> locations = new LinkedList<>();
    @Getter
    @Setter
    private double treashold = 1;

    @Override
    public void onSpawn() {
        isTracking = true;
        npc.getTraits().forEach(t -> {
            if (t instanceof BedwarsBlockPlace)
                blockPlace = (BedwarsBlockPlace) t;
        });
    }

    @Override
    public void onDespawn() {
        isTracking = false;
    }

    @Override
    public void onRemove() {
        isTracking = false;
    }

    int timer = 0;

    private boolean isEmpty(Block testBlock) {
        return testBlock.getType() == Material.AIR || testBlock.getType() == Material.LAVA
                || testBlock.getType() == Material.WATER;
    }

    private Location tryFindingJump(Location currentLocation, Location target) {
        Block b = currentLocation.getBlock();
        Block toPlace = null;
        double testDistance = Double.MAX_VALUE;
        for (Block testBlock : List.of(
                b.getRelative(BlockFace.EAST),
                b.getRelative(BlockFace.WEST),
                b.getRelative(BlockFace.NORTH),
                b.getRelative(BlockFace.SOUTH))) {
            var distanceToTarget = testBlock.getLocation().distance(target);
            if (isEmpty(testBlock) && isEmpty(testBlock.getRelative(BlockFace.DOWN))
                    && isEmpty(testBlock.getRelative(BlockFace.UP))) {
                if (distanceToTarget < testDistance) {
                    testDistance = distanceToTarget;
                    toPlace = testBlock;
                }
            }
        }
        if (toPlace != null) {
            if (blockPlace.placeBlockIfPossible(toPlace.getLocation())) {
                npc.getEntity().teleport(toPlace.getLocation().clone().add(0, 1, 0));
            }
            return toPlace.getLocation();
        }
        
        return null;
    }

    @Override
    public void run() {
        if (timer-- <= 0) {
            timer = 4;
            if (isTracking && npc.getNavigator().isNavigating()) {
                var currentLocation = npc.getOrAddTrait(CurrentLocation.class).getLocation();
                locations.add(currentLocation);

                if (locations.size() > 5)
                    locations.removeFirst();

                double distance = 0;
                if (locations.size() == 1)
                    distance = Double.MAX_VALUE;
                var it = locations.iterator();
                Location current = null;
                if (it.hasNext()) {
                    current = it.next();
                    while (it.hasNext()) {
                        Location tmp = it.next();
                        distance += tmp.distance(current);
                        current = tmp;
                    }
                }
               

                if (distance < treashold && !blockPlace.isBreaking()) {
                    // Stuck
                    var target = npc.getNavigator().getTargetAsLocation();
                    var horizontal = target.clone();
                    horizontal.setY(currentLocation.getY());

                    if (target.getBlockY() > currentLocation.getBlockY()) {
                        // Try building up
                        if (blockPlace.placeBlockIfPossible(currentLocation)) {
                            Player aiPlayer = (Player) npc.getEntity();
                            Vector v = aiPlayer.getVelocity().setY(0.5);
                            aiPlayer.setVelocity(v);
                            locations.clear();
                        }
                    } else if (target.getBlockY() < currentLocation.getBlockY() - 3 && horizontal.distance(currentLocation) < 3) {
                        // Try building up

                        Block standingOn = currentLocation.getBlock().getRelative(BlockFace.DOWN);
                        Logger.trace("standingOn {}", standingOn);
                        if (blockPlace.isBreakableBlock(standingOn)) {
                            Player aiPlayer = (Player) npc.getEntity();
                            aiPlayer.teleport(standingOn.getLocation().toBlockLocation().add(0.5, 1, 0.5));
                            blockPlace.breakBlock(standingOn);
                            Logger.trace("starting breaking of {}", standingOn);
                        } else {
                            Player aiPlayer = (Player) npc.getEntity();
                            Location l = tryFindingJump(currentLocation, target);
                            aiPlayer.teleport(l);
                        }
                    } else {
                        Block b = currentLocation.getBlock().getRelative(BlockFace.DOWN);
                        Block toPlace = null;
                        double testDistance = Double.MAX_VALUE;
                        for (Block testBlock : List.of(
                                b.getRelative(BlockFace.EAST),
                                b.getRelative(BlockFace.WEST),
                                b.getRelative(BlockFace.NORTH),
                                b.getRelative(BlockFace.SOUTH))) {
                            var distanceToTarget = testBlock.getLocation().distance(target);
                            if (testBlock.getType() == Material.AIR || testBlock.getType() == Material.LAVA
                                    || testBlock.getType() == Material.WATER) {
                                if (distanceToTarget < testDistance) {
                                    testDistance = distanceToTarget;
                                    toPlace = testBlock;
                                }
                            }
                        }
                        if (toPlace != null) {
                            if (blockPlace.placeBlockIfPossible(toPlace.getLocation())) {
                                npc.getEntity().teleport(toPlace.getLocation().toBlockLocation().clone().add(0.5, 1, 0.5));
                            }
                        }
                    }
                }
            }
        }
    }
}
