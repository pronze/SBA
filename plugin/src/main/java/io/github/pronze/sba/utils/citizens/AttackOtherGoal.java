package io.github.pronze.sba.utils.citizens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;

import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;

public class AttackOtherGoal implements FakeDeathTrait.AiGoal {

    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;
    private Block targetBlock = null;
    private Player targetPlayer = null;

    /**
     * @param fakeDeathTrait
     */
    AttackOtherGoal(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    @Override
    public boolean isAvailable() {
        if (this.fakeDeathTrait.blockPlace() == null)
            return false;
        if (this.fakeDeathTrait.blockPlace().isInNeedOfBlock())
            return false;
        Player aiPlayer = (Player) this.fakeDeathTrait.getNPC().getEntity();
        Location currentLocation = fakeDeathTrait.getNPC().getEntity().getLocation();

        if (targetBlock == null || fakeDeathTrait.blockPlace().isEmpty(targetBlock)) {

            Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
            if (g != null) {
                var team = g.getTeamOfPlayer(aiPlayer);
                if (team != null) {
                    double distance = Double.MAX_VALUE;
                    for (var otherTeam : g.getRunningTeams()) {
                        if (!otherTeam.getName().equals(team.getName())) {
                            if (otherTeam.isTargetBlockExists()) {
                                var distanceToCheck = otherTeam.getTargetBlock().distance(currentLocation);
                                if (distanceToCheck < distance) {
                                    distance = distanceToCheck;
                                    targetBlock = otherTeam.getTargetBlock().getBlock();
                                    Logger.trace("AttackOtherGoal::AI will target {} ", targetBlock);
                                }
                            } 
                        }
                    }
                }
            }
        }
        if (targetBlock == null && targetPlayer == null || (targetPlayer != null
                && (targetPlayer.isDead() || targetPlayer.getGameMode() == GameMode.SPECTATOR))) {
            Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
            if (g != null) {
                var team = g.getTeamOfPlayer(aiPlayer);
                if (team != null) {
                    double distance = Double.MAX_VALUE;
                    for (var otherTeam : g.getRunningTeams()) {
                        if (otherTeam.isAlive() && !otherTeam.getName().equals(team.getName())) {
                            for (Player p : otherTeam.getConnectedPlayers()) {
                                var distanceToCheck = p.getLocation().distance(currentLocation);
                                if (!p.isDead() && p.getGameMode() == GameMode.SURVIVAL && distanceToCheck < distance) {
                                    distance = distanceToCheck;
                                    targetPlayer = p;
                                    Logger.trace("AttackOtherGoal::AI will target {} ", targetPlayer);
                                }
                            }
                        }
                    }
                }
            }
        }

        return targetBlock != null || targetPlayer != null;
    }

    @Override
    public void doGoal() {
        if (targetBlock != null) {
            if(fakeDeathTrait.blockPlace().isEmpty(targetBlock))
            {
                targetBlock = null;
                return;
            }
            
            Player aiPlayer = fakeDeathTrait.getNpcEntity();
            var distance = targetBlock.getLocation().distance(aiPlayer.getLocation());
            if (distance < 3) {
                fakeDeathTrait.blockPlace().breakBlock(targetBlock);
            }
            else
            {
                fakeDeathTrait.getNPC().getNavigator().setTarget(targetBlock.getLocation());
            }
        } else if (targetPlayer != null) {
                fakeDeathTrait.getNPC().getNavigator().setTarget(targetPlayer, true);
        }
    }
}