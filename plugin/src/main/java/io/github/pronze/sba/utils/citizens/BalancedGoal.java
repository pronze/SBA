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

public class BalancedGoal implements FakeDeathTrait.AiGoal {

    BuildBedDefenseGoal bedDefenseGoal;
    AttackOtherGoal attackOtherGoal;

    /**
     * @param fakeDeathTrait
     */
    BalancedGoal(FakeDeathTrait fakeDeathTrait) {
        bedDefenseGoal = new BuildBedDefenseGoal(fakeDeathTrait);
        attackOtherGoal = new AttackOtherGoal(fakeDeathTrait);
    }

    boolean bed = false, atk = false;

    @Override
    public boolean isAvailable() {
        bed=atk=false;
        boolean isBedAvailable = bed = bedDefenseGoal.isAvailable();
        if (isBedAvailable)
            return true;
        if (bedDefenseGoal.isOver())
            return atk = attackOtherGoal.isAvailable();
            return false;
    }

    @Override
    public void doGoal() {
        if (bed)
            bedDefenseGoal.doGoal();
        if (atk)
            attackOtherGoal.doGoal();
    }
}