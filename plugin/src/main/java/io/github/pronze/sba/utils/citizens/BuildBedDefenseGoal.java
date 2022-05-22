package io.github.pronze.sba.utils.citizens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;

import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;

public class BuildBedDefenseGoal implements FakeDeathTrait.AiGoal {

    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;

    /**
     * @param fakeDeathTrait
     */
    BuildBedDefenseGoal(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    Block targetBlock = null;
    List<Block> blockToBuild = new ArrayList<>();

    public void floodFill(Block b, int depth) {
        if (depth <= 2) {
            if (!blockToBuild.contains(b))
                blockToBuild.add(b);

            int cost = 1;

            floodFill(b.getRelative(BlockFace.EAST), depth + cost);
            floodFill(b.getRelative(BlockFace.WEST), depth + cost);
            floodFill(b.getRelative(BlockFace.NORTH), depth + cost);
            floodFill(b.getRelative(BlockFace.SOUTH), depth + cost);
            floodFill(b.getRelative(BlockFace.UP), depth + cost);
        }
    }

    public Block findSecondBedBlock(Block b) {
        for (Block testBlock : List.of(
                b.getRelative(BlockFace.EAST),
                b.getRelative(BlockFace.WEST),
                b.getRelative(BlockFace.NORTH),
                b.getRelative(BlockFace.SOUTH))) {
            Logger.trace("Testing {} for second bed block", testBlock);
            if (testBlock.getType().toString().toUpperCase().contains("BED"))
                return testBlock;
        }
        return b;
    }

    @Override
    public boolean isAvailable() {
        if (this.fakeDeathTrait.blockPlace() == null)
            return false;
        if (this.fakeDeathTrait.blockPlace().isInNeedOfBlock())
            return false;
        if (targetBlock == null) {
            Player aiPlayer = (Player) this.fakeDeathTrait.getNPC().getEntity();
            Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
            if (g != null) {
                var team = g.getTeamOfPlayer(aiPlayer);
                if (team != null) {
                    targetBlock = team.getTargetBlock().getBlock();

                    floodFill(targetBlock, 0);
                    if (targetBlock.getType().toString().toUpperCase().contains("BED")) {
                        floodFill(findSecondBedBlock(targetBlock), 0);
                    }
                }
            }
        } else {
            if (this.fakeDeathTrait.blockPlace().isEmpty(targetBlock)) {
                return false;
            }

            if (blockToBuild.stream()
                    .anyMatch(b -> this.fakeDeathTrait.blockPlace().isEmpty(b)
                            && this.fakeDeathTrait.blockPlace().isPlacable(b.getLocation()))) {
                return true;
            }
        }

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void doGoal() {
        // TODO Auto-generated method stub

        Block toPlace = blockToBuild.stream()
                .filter(b -> this.fakeDeathTrait.blockPlace().isEmpty(b)
                        && this.fakeDeathTrait.blockPlace().isPlacable(b.getLocation()))
                .findFirst().orElse(null);

        if (toPlace != null) {
            this.fakeDeathTrait.getNPC().getNavigator().setTarget(toPlace.getLocation());
            if (toPlace.getLocation().distance(this.fakeDeathTrait.getNPC().getEntity().getLocation()) < 4) {
                this.fakeDeathTrait.blockPlace().placeBlockIfPossible(toPlace.getLocation());
                if (toPlace.getLocation().distance(this.fakeDeathTrait.getNPC().getEntity().getLocation()) < 2) {
                    this.fakeDeathTrait.getNPC().getEntity()
                            .teleport(toPlace.getLocation().toBlockLocation().add(0.5, 1, 0.5));
                }
            }
        }
    }

    public boolean isOver() {
        return blockToBuild.stream().allMatch(b -> !this.fakeDeathTrait.blockPlace().isEmpty(b));
    }
}