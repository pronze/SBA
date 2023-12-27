package io.github.pronze.sba.specials;

import io.github.pronze.sba.utils.Logger;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.util.Vector;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.tasker.DefaultThreads;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// TODO: make this configurable
@Data
@RequiredArgsConstructor
public class PopupTower {

    private final static Map<BlockFace, Byte> faceToByte = Map.of(
            BlockFace.EAST, (byte) 5,
            BlockFace.WEST, (byte) 4,
            BlockFace.SOUTH, (byte) 3,
            BlockFace.NORTH, (byte) 2
    );

    private final static List<BlockFace> pillarSides = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH);

    private final Game game;
    private final Material material;
    private final byte legacyData;
    private final Location centerPoint;
    private final BlockFace placementFace;
    private final List<Location> enterancelocation = new ArrayList<>();

    private List<Location> targetBlocks;

    public void createTower() {
        targetBlocks = game.getRunningTeams()
                .stream()
                .map(RunningTeam::getTargetBlock)
                .collect(Collectors.toList());

        final var bottomRel = this.centerPoint.getBlock().getRelative(placementFace.getOppositeFace(), 2).getRelative(BlockFace.UP);
        enterancelocation.add(bottomRel.getLocation());
        enterancelocation.add(bottomRel.getRelative(BlockFace.UP).getLocation());

        Logger.trace("Placement face: {}", placementFace);

        placeAnimated(BlockFace.NORTH, BlockFace.WEST);
        placeAnimated(BlockFace.SOUTH, BlockFace.WEST);
        placeAnimated(BlockFace.WEST, BlockFace.SOUTH);
        placeAnimated(BlockFace.EAST, BlockFace.SOUTH);

        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
            // second platform
            final Block secondPlatform = centerPoint.getBlock().getRelative(BlockFace.UP, 5);
            placeBlock(secondPlatform.getLocation(), material, legacyData);
            pillarSides.forEach(blockFace -> placeBlock(secondPlatform.getRelative(blockFace).getLocation(), material, legacyData));

            placeBlock(secondPlatform.getRelative(BlockFace.NORTH_WEST).getLocation(), material, legacyData);
            placeBlock(secondPlatform.getRelative(BlockFace.NORTH_EAST).getLocation(), material, legacyData);
            placeBlock(secondPlatform.getRelative(BlockFace.SOUTH_WEST).getLocation(), material, legacyData);
            placeBlock(secondPlatform.getRelative(BlockFace.SOUTH_EAST).getLocation(), material, legacyData);

            final var northWestCornerBlock = secondPlatform.getRelative(BlockFace.NORTH_WEST, 2).getRelative(BlockFace.UP);
            placeBlock(northWestCornerBlock.getRelative(BlockFace.DOWN).getLocation(), material, legacyData);
            placeBlock(northWestCornerBlock.getRelative(BlockFace.WEST).getLocation(), material, legacyData);
            placeBlock(northWestCornerBlock.getRelative(BlockFace.NORTH).getLocation(), material, legacyData);
            placeBlock(northWestCornerBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getLocation(), material, legacyData);
            placeBlock(northWestCornerBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getLocation(), material, legacyData);

            final var northEastCornerBlock = secondPlatform.getRelative(BlockFace.NORTH_EAST, 2).getRelative(BlockFace.UP);
            placeBlock(northEastCornerBlock.getRelative(BlockFace.DOWN).getLocation(), material, legacyData);
            placeBlock(northEastCornerBlock.getRelative(BlockFace.EAST).getLocation(), material, legacyData);
            placeBlock(northEastCornerBlock.getRelative(BlockFace.NORTH).getLocation(), material, legacyData);
            placeBlock(northEastCornerBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getLocation(), material, legacyData);
            placeBlock(northEastCornerBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getLocation(), material, legacyData);

            final var southWestCornerBlock = secondPlatform.getRelative(BlockFace.SOUTH_WEST, 2).getRelative(BlockFace.UP);
            placeBlock(southWestCornerBlock.getRelative(BlockFace.DOWN).getLocation(), material, legacyData);
            placeBlock(southWestCornerBlock.getRelative(BlockFace.WEST).getLocation(), material, legacyData);
            placeBlock(southWestCornerBlock.getRelative(BlockFace.SOUTH).getLocation(), material, legacyData);
            placeBlock(southWestCornerBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getLocation(), material, legacyData);
            placeBlock(southWestCornerBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getLocation(), material, legacyData);

            final var southEastCornerBlock = secondPlatform.getRelative(BlockFace.SOUTH_EAST, 2).getRelative(BlockFace.UP);
            placeBlock(southEastCornerBlock.getRelative(BlockFace.DOWN).getLocation(), material, legacyData);
            placeBlock(southEastCornerBlock.getRelative(BlockFace.EAST).getLocation(), material, legacyData);
            placeBlock(southEastCornerBlock.getRelative(BlockFace.SOUTH).getLocation(), material, legacyData);
            placeBlock(southEastCornerBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getLocation(), material, legacyData);
            placeBlock(southEastCornerBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getLocation(), material, legacyData);

            // connection blocks
            placeRowAnimated(3, northWestCornerBlock.getRelative(BlockFace.NORTH).getLocation(), BlockFace.EAST, 1);
            placeRowAnimated(3, southWestCornerBlock.getRelative(BlockFace.SOUTH).getLocation(), BlockFace.EAST, 1);
            placeRowAnimated(4, southWestCornerBlock.getRelative(BlockFace.SOUTH_WEST).getLocation(), BlockFace.NORTH, 1);
            placeRowAnimated(4, southEastCornerBlock.getRelative(BlockFace.EAST).getLocation(), BlockFace.NORTH, 1);

            placeBlock(secondPlatform.getRelative(placementFace, 3).getRelative(BlockFace.UP, 2).getLocation(), material, legacyData);
            placeBlock(secondPlatform.getRelative(placementFace.getOppositeFace(), 3).getRelative(BlockFace.UP, 2).getLocation(), material, legacyData);

            final Location firstLadderBlock = centerPoint.getBlock().getRelative(placementFace).getLocation();
            placeLadderRow(5, firstLadderBlock, BlockFace.UP, placementFace.getOppositeFace());
        }, 40L, TaskerTime.TICKS);
    }

    public void placeAnimated(BlockFace direction, BlockFace start) {
        final var p1 = centerPoint.getBlock().getRelative(direction, 2).getRelative(start, 2).getLocation();
        placeRowAndColumn(3, 5, p1, start.getOppositeFace());
    }

    public void placeRowAnimated(int length, Location loc, BlockFace face, int delay) {
        Location lastLoc = loc;
        for (int i = 0; i < length; i++) {
            lastLoc = lastLoc.getBlock().getRelative(face).getLocation();
            Location finalLastLoc = lastLoc;
            Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> placeBlock(finalLastLoc, material, legacyData), (delay += 1), TaskerTime.TICKS);
        }
    }

    public void placeRowAndColumn(int length, int height, Location loc, BlockFace face) {
        int sepTickedPlacement = 1;
        for (int i = 0; i < height; i++) {
            loc = loc.clone().add(0, 1, 0);
            Location finalLoc = loc;
            for (int j = 0; j < length; j++) {
                placeRowAnimated(length, finalLoc, face, sepTickedPlacement);
                sepTickedPlacement += 2;
            }
        }
    }

    private boolean isTargetBlockNear(List<Location> targetBlocks, Location loc) {
        return targetBlocks.contains(loc) || Arrays.stream(BlockFace.values())
                .anyMatch(blockFace -> targetBlocks.contains(loc.getBlock().getRelative(blockFace, 1).getLocation()));
    }

    public void placeLadderRow(int length, Location loc, BlockFace face, BlockFace ladderFace) {
        if (game.getStatus() != GameStatus.RUNNING) {
            return;
        }

        Location lastLoc = loc;
        for (int i = 0; i < length; i++) {
            lastLoc = lastLoc.getBlock().getRelative(face).getLocation();
            final Block ladder = lastLoc.getBlock();
            if (!isLocationSafe(lastLoc)) {
                continue;
            }
            ladder.setType(Material.LADDER, false);
            game.getRegion().removeBlockBuiltDuringGame(lastLoc);
            game.getRegion().addBuiltDuringGame(lastLoc);
            if (!Main.isLegacy()) {
                BlockData blockData = ladder.getBlockData();
                if (blockData instanceof Directional) {
                    ((Directional) blockData).setFacing(ladderFace);
                    ladder.setBlockData(blockData);
                }
            } else {
                Reflect.getMethod(ladder, "setData", byte.class).invoke(faceToByte.get(ladderFace));
            }
            Sounds.playSound(loc, "BLOCK_STONE_PLACE", Sounds.BLOCK_STONE_BREAK, 1, 1);
        }
    }

    public void placeBlock(Location loc, Material mat, byte legacyData) {
        if (!isLocationSafe(loc) || !game.isLocationInArena(loc)) {
            return;
        }
        if (game.getStatus() != GameStatus.RUNNING) {
            return;
        }
        game.getRegion().removeBlockBuiltDuringGame(loc);
        loc.getBlock().setType(mat);

        if (Main.isLegacy() && legacyData != 0) {
            var block = loc.getBlock();
            Reflect.getMethod(block, "setData", byte.class).invoke(legacyData);
        }

        game.getRegion().addBuiltDuringGame(loc);
        Sounds.playSound(loc, "BLOCK_STONE_PLACE", Sounds.BLOCK_STONE_BREAK, 1, 1);
    }

    public boolean isLocationSafe(Location location) {
        final var locBlock = location.getBlock();
        return !SpawnerProtection.getInstance().isProtected(game, location) && (locBlock.getType() == Material.AIR || Main.isBreakableBlock(location.getBlock().getType()) || game.getRegion().isBlockAddedDuringGame(location)) && !isTargetBlockNear(targetBlocks, location) && !isEntranceLocation(location);
    }

    public boolean isEntranceLocation(Location toCheck) {
        return enterancelocation.contains(toCheck);
    }
}
