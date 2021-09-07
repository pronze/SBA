package io.github.pronze.sba.specials;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class PopupTower {

    private final static Map<BlockFace, Byte> faceToByte = Map.of(
            BlockFace.EAST,  (byte) 5,
            BlockFace.WEST,  (byte) 4,
            BlockFace.SOUTH, (byte) 3,
            BlockFace.NORTH, (byte) 2
    );

    private final static List<BlockFace> pillarSides = List.of(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH);

    private final Game game;
    private final Material material;
    private final Location location;
    private int height;
    private int width;
    private List<Location> targetBlocks;

    public void createTower(boolean floor, BlockFace structureFace) {
        targetBlocks = game.getRunningTeams()
                .stream()
                .map(RunningTeam::getTargetBlock)
                .collect(Collectors.toList());

        if (height < 4) {
            height = 4;
        }
        if (width < 1) {
            width = 1;
        }

        final Location mainBlock = location.getBlock().getRelative(BlockFace.DOWN).getLocation();
        placeBlock(mainBlock, material);
        
        pillarSides.forEach(blockFace -> {
            if (floor) {
                for (int i = 0; i < (width + 2); i++) {
                    List<BlockFace> direction = pillarSides.stream().filter(face -> face != blockFace).filter(face -> face != blockFace.getOppositeFace()).collect(Collectors.toList());
                    int finalI = i;
                    if (finalI == 2) {
                        direction.forEach(face -> placeRow(finalI, mainBlock.getBlock().getRelative(blockFace, finalI + 1).getLocation(), face));
                        continue;
                    }
                    direction.forEach(face -> placeRow(finalI + 1, mainBlock.getBlock().getRelative(blockFace, finalI + 1).getLocation(), face));
                }
                placeRow(width + 2, mainBlock, blockFace);
            }
            final Block pillarBase = mainBlock.getBlock().getRelative(blockFace, 3);
            if (structureFace == blockFace) {
                final Block lastBlock = mainBlock.getBlock().getRelative(blockFace, 3).getRelative(BlockFace.UP, 2);
                placeRow(height - 3, lastBlock.getLocation(), BlockFace.UP);
            } else {
                placeRow(height - 1, pillarBase.getLocation(), BlockFace.UP);
            }
            for (int i = 0; i < height; i++) {
                List<BlockFace> direction = pillarSides.stream().filter(face -> face != blockFace).filter(face -> face != blockFace.getOppositeFace()).collect(Collectors.toList());
                int finalI = i;
                direction.forEach(face -> placeRow(width, pillarBase.getRelative(BlockFace.UP, finalI).getLocation(), face));
            }
        });

        final Block secondPlatform = mainBlock.getBlock().getRelative(BlockFace.UP, height);
        placeBlock(secondPlatform.getLocation(), material);
        placeRow(height, mainBlock, BlockFace.UP);
        pillarSides.forEach(blockFace -> {
            for (int i = 0; i < (width + 1); i++) {
                List<BlockFace> direction = pillarSides.stream().filter(face -> face != blockFace).filter(face -> face != blockFace.getOppositeFace()).collect(Collectors.toList());
                int finalI = i;
                if (i == (width - 1)) {
                    direction.forEach(face -> placeRow(finalI, secondPlatform.getRelative(blockFace, finalI + 1).getLocation(), face));
                    direction.forEach(face -> placeRow(finalI, secondPlatform.getRelative(BlockFace.UP).getRelative(blockFace, finalI + 1).getLocation(), face));
                    direction.forEach(face -> placeRow(finalI, secondPlatform.getRelative(BlockFace.UP, width - 1).getRelative(blockFace, finalI + 1).getLocation(), face));
                    continue;
                }
                direction.forEach(face -> placeRow(finalI + 1, secondPlatform.getRelative(blockFace, finalI + 1).getLocation(), face));
            }
            placeRow(width + 1, secondPlatform.getLocation(), blockFace);
        });

        final var relative = secondPlatform.getRelative(structureFace);
        if (game.getRegion().isBlockAddedDuringGame(relative.getLocation())) {
            relative.setType(Material.AIR);
            game.getRegion().removeBlockBuiltDuringGame(relative.getLocation());
        }

        final Location firstLadderBlock = mainBlock.getBlock().getRelative(structureFace).getLocation();
        placeLadderRow(height, firstLadderBlock, BlockFace.UP, structureFace);
    }

    public void placeRow(int length, Location loc, BlockFace face) {
        Location lastLoc = loc;
        for (int i = 0; i < length; i++) {
            lastLoc = lastLoc.getBlock().getRelative(face).getLocation();
            placeBlock(lastLoc, material);
        }
    }

    private boolean isTargetBlockNear(List<Location> targetBlocks, Location loc) {
        return targetBlocks.contains(loc) || Arrays.stream(BlockFace.values())
                .anyMatch(blockFace -> targetBlocks.contains(loc.getBlock().getRelative(blockFace, 1).getLocation()));
    }

    public void placeLadderRow(int length, Location loc, BlockFace face, BlockFace ladderFace) {
        Location lastLoc = loc;
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                if ((!game.getRegion().isBlockAddedDuringGame(lastLoc) && (lastLoc.getBlock().getType() != Material.AIR && lastLoc.getBlock().getType() != Material.GRASS)) || isTargetBlockNear(targetBlocks, lastLoc)) {
                    continue;
                }
            }
            lastLoc = lastLoc.getBlock().getRelative(face).getLocation();
            final Block ladder = lastLoc.getBlock();
            if (ladder.getType() != Material.AIR && !game.getRegion().isBlockAddedDuringGame(ladder.getLocation())) {
                continue;
            }
            ladder.setType(Material.LADDER, false);
            game.getRegion().addBuiltDuringGame(lastLoc);
            if (!Main.isLegacy()) {
                BlockData blockData = ladder.getBlockData();
                if (blockData instanceof Directional) {
                    ((Directional) blockData).setFacing(ladderFace);
                    ladder.setBlockData(blockData);
                }
            } else {
                Reflect.getMethod(ladder, "setData", Byte.class)
                        .invoke(ladder, faceToByte.get(ladderFace));
            }
            Objects.requireNonNull(loc.getWorld()).playSound(loc, Sound.BLOCK_STONE_PLACE, 1, 1);
        }
    }

    public void placeBlock(Location loc, Material mat) {
        if ((!game.getRegion().isBlockAddedDuringGame(loc) && (loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.GRASS)) || isTargetBlockNear(targetBlocks, loc)) {
            return;
        }
        loc.getBlock().setType(mat);
        game.getRegion().addBuiltDuringGame(loc);
        Objects.requireNonNull(loc.getWorld()).playSound(loc, Sound.BLOCK_STONE_PLACE, 1, 1);
    }
}
