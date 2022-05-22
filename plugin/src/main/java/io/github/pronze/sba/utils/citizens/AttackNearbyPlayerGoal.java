package io.github.pronze.sba.utils.citizens;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;

import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;
import io.github.pronze.sba.utils.citizens.FakeDeathTrait.Strategy;

public class AttackNearbyPlayerGoal implements FakeDeathTrait.AiGoal {
    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;

    /**
     * @param fakeDeathTrait
     */
    AttackNearbyPlayerGoal(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    Player target;

    @Override
    public boolean isAvailable() {
        target = null;
        double distance = Double.MAX_VALUE;

        int range = 25;
        if(fakeDeathTrait.getStrategy() == Strategy.AGRESSIVE)
            range = 5;
        var entities = this.fakeDeathTrait.getNearbyEntities(range);
        for (Entity entity : entities) {
            if (entity instanceof Player && !entity.equals(this.fakeDeathTrait.getNPC().getEntity())) {
                Player possibleTarget = (Player) entity;
                if (possibleTarget.getGameMode() != GameMode.SURVIVAL)
                    continue;
                Game targetGame = Main.getInstance().getGameOfPlayer(possibleTarget);
                if (targetGame == null)
                    continue;
                var targetTeam = targetGame.getTeamOfPlayer(possibleTarget);
                if (targetTeam == null)
                    continue;
                var ourTeam = targetGame.getTeamOfPlayer((Player) this.fakeDeathTrait.getNPC().getEntity());
                if (ourTeam == null)
                    continue;
                if (!ourTeam.getName().equals(targetTeam.getName())) {
                    double possibleDistance = possibleTarget.getLocation()
                            .distance(this.fakeDeathTrait.getNPC().getEntity().getLocation());
                    if (possibleDistance < distance) {
                        distance = possibleDistance;
                        target = possibleTarget;
                    }
                }
            }
        }
        Player aiPlayer = (Player) this.fakeDeathTrait.getNPC().getEntity();
        Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
        if (g != null && target == null) {
            var team = g.getTeamOfPlayer(aiPlayer);
            if (team != null) {
                Block targetBlock = team.getTargetBlock().getBlock();
                targetBlock.getWorld().getNearbyLivingEntities(targetBlock.getLocation(), range);
                for (Entity entity : entities) {
                    if (entity instanceof Player && !entity.equals(this.fakeDeathTrait.getNPC().getEntity())) {
                        Player possibleTarget = (Player) entity;
                        if (possibleTarget.getGameMode() != GameMode.SURVIVAL)
                            continue;
                        Game targetGame = Main.getInstance().getGameOfPlayer(possibleTarget);
                        if (targetGame == null)
                            continue;
                        var targetTeam = targetGame.getTeamOfPlayer(possibleTarget);
                        if (targetTeam == null)
                            continue;
                        var ourTeam = targetGame.getTeamOfPlayer((Player) this.fakeDeathTrait.getNPC().getEntity());
                        if (ourTeam == null)
                            continue;
                        if (!ourTeam.getName().equals(targetTeam.getName())) {
                            double possibleDistance = possibleTarget.getLocation()
                                    .distance(this.fakeDeathTrait.getNPC().getEntity().getLocation());
                            if (possibleDistance < distance) {
                                distance = possibleDistance;
                                target = possibleTarget;
                            }
                        }
                    }
                }
            }
        }
        return target != null;
    }

    @Override
    public void doGoal() {
        this.fakeDeathTrait.getNPC().getNavigator().setTarget(target, true);
    }
}