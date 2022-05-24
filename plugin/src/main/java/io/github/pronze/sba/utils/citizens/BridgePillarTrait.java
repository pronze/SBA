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
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.NPC;
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
    private double treashold = 2.3;

    @Override
    public void onSpawn() {
        isTracking = true;
        npc.getTraits().forEach(t -> {
            if (t instanceof BedwarsBlockPlace)
                blockPlace = (BedwarsBlockPlace) t;
        });
        npc.getNavigator().getLocalParameters().stuckAction(
            new StuckAction() {
                @Override
                public boolean run(NPC arg0, Navigator arg1) {
                        return unstuck(arg0.getEntity().getLocation());
                }
        }
        );
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

    public boolean isEmpty(Block testBlock) {
        return testBlock.getType() == Material.AIR || testBlock.getType() == Material.LAVA
                || testBlock.getType() == Material.WATER;
    }

    public boolean canBuildUp(Location loc) {
        Block testBlock = loc.getBlock();
        return (isEmpty(testBlock) && isEmpty(testBlock.getRelative(BlockFace.DOWN))
                && isEmpty(testBlock.getRelative(BlockFace.UP)));
    }

    public boolean canMove(Location loc) {
        Block testBlock = loc.getBlock();
        return (isEmpty(testBlock.getRelative(BlockFace.DOWN))
                && isEmpty(testBlock.getRelative(BlockFace.UP)));
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

    public boolean teleport(Player aiPlayer, Location l) {
       return blockPlace.teleport(aiPlayer,l);
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
                    unstuck(currentLocation);
                }
            }
        }
    }
    public Location blockLocation(Location l)
    {
        return blockPlace.blockLocation(l);
    }

    public boolean unstuck(Location currentLocation) {
        Logger.trace("NPC IS STUCK {}", getNPC().getName());

        // Stuck
        var target = npc.getNavigator().getTargetAsLocation();
        var horizontal = target.clone();
        horizontal.setY(currentLocation.getY());

        if (target.getBlockY() > currentLocation.getBlockY()
                && blockPlace.isJumpPlacable(currentLocation)) {
            // Try building up
            if (blockPlace.placeBlockIfPossible(currentLocation)) {
                Player aiPlayer = (Player) npc.getEntity();
                teleport(aiPlayer, blockLocation(currentLocation).add(0.5, 1, 0.5));
                return true;
            }
        } else if (target.getBlockY() < currentLocation.getBlockY() - 2
                && horizontal.distance(currentLocation) < 2) {
            // Try building up

            Block standingOn = currentLocation.getBlock().getRelative(BlockFace.DOWN);
            Logger.trace("standingOn {}", standingOn);
            if (blockPlace.isBreakableBlock(standingOn)) {
                Player aiPlayer = (Player) npc.getEntity();
                teleport(aiPlayer, blockLocation(standingOn.getLocation()).add(0.5, 1, 0.5));
                blockPlace.breakBlock(standingOn);
                Logger.trace("starting breaking of {}", standingOn);
                return false;
            } else {
                Player aiPlayer = (Player) npc.getEntity();
                Location l = tryFindingJump(currentLocation, target);
                teleport(aiPlayer, l);
                return true;
            }
        } else {
            Block b = currentLocation.getBlock().getRelative(BlockFace.DOWN);
            Block toPlace = null;
            double testDistance = Double.MAX_VALUE;
            Location toMove = null;
            for (Block testBlock : List.of(
                    b.getRelative(BlockFace.EAST),
                    b.getRelative(BlockFace.WEST),
                    b.getRelative(BlockFace.NORTH),
                    b.getRelative(BlockFace.SOUTH))) {
                if (!isEmpty(testBlock.getRelative(BlockFace.DOWN)) && canMove(testBlock.getLocation())) {
                    var distanceToTarget = testBlock.getLocation().distance(target);
                    if (distanceToTarget < testDistance
                            && blockPlace.isPlacable(testBlock.getLocation())) {
                        testDistance = distanceToTarget;
                        toMove = testBlock.getLocation().clone().add(0.5, 0, 0.5);
                    }
                }
            }
            for (Block testBlock : List.of(
                    b.getRelative(BlockFace.EAST),
                    b.getRelative(BlockFace.WEST),
                    b.getRelative(BlockFace.NORTH),
                    b.getRelative(BlockFace.SOUTH))) {
                var distanceToTarget = testBlock.getLocation().distance(target);

                if (canBuildUp(testBlock.getLocation())) {
                    if (distanceToTarget < testDistance && blockPlace.isPlacable(testBlock.getLocation())) {
                        testDistance = distanceToTarget;
                        toMove = null;
                        toPlace = testBlock;
                    }
                }
            }
            if (toMove != null) {
                Player aiPlayer = (Player) npc.getEntity();
                teleport(aiPlayer, toMove);
                return true;
            } else if (toPlace != null) {
                if (blockPlace.placeBlockIfPossible(toPlace.getLocation())) {
                    Player aiPlayer = (Player) npc.getEntity();
                    teleport(aiPlayer, blockLocation(toPlace.getLocation()).clone().add(0.5, 1, 0.5));
                    return true;
                }
            } else {

                // Is the path blocked
                b = currentLocation.getBlock();
                testDistance = Double.MAX_VALUE;
                Block toBreak = null;

                for (Block testBlock : List.of(
                        b.getRelative(BlockFace.EAST),
                        b.getRelative(BlockFace.WEST),
                        b.getRelative(BlockFace.NORTH),
                        b.getRelative(BlockFace.SOUTH),
                        b.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN),
                        b.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN),
                        b.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN),
                        b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN),
                        b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getRelative(BlockFace.UP),
                        b.getRelative(BlockFace.UP).getRelative(BlockFace.UP))) {
                    var distanceToTarget = testBlock.getLocation().distance(target);
                    if (blockPlace.isBreakableBlock(testBlock)) {
                        if (distanceToTarget < testDistance
                                && blockPlace.isPlacable(testBlock.getLocation())) {
                            testDistance = distanceToTarget;
                            toBreak = testBlock;
                            toMove = null;
                        }
                    }
                }

                if (toBreak != null) {
                    blockPlace.breakBlock(toBreak);
                    return false;
                }
            }
        }
        return false;
    }
}
