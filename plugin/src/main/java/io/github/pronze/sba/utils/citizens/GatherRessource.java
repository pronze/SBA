package io.github.pronze.sba.utils.citizens;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import io.github.pronze.sba.utils.citizens.FakeDeathTrait.AiGoal;

public class GatherRessource implements FakeDeathTrait.AiGoal {
    /**
     *
     */
    private final FakeDeathTrait fakeDeathTrait;

    /**
     * @param fakeDeathTrait
     */
    GatherRessource(FakeDeathTrait fakeDeathTrait) {
        this.fakeDeathTrait = fakeDeathTrait;
    }

    Item target;

    @Override
    public boolean isAvailable() {
        target = null;
        double distance = Double.MAX_VALUE;

        var entities = this.fakeDeathTrait.getNearbyEntities(50);
        for (Entity entity : entities) {
            if (entity instanceof Item) {
                Item possibleTarget = (Item) entity;

                double possibleDistance = possibleTarget.getLocation()
                        .distance(this.fakeDeathTrait.getNPC().getEntity().getLocation());
                if (possibleDistance < distance) {
                    distance = possibleDistance;
                    target = possibleTarget;
                }
            }
        }
        return target != null;
    }

    @Override
    public void doGoal() {
        this.fakeDeathTrait.getNPC().getNavigator().setTarget(target, false);
    }

}