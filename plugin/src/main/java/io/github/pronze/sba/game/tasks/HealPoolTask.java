package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.config.SBAConfig;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.screamingsandals.lib.item.meta.PotionEffectHolder;

public class HealPoolTask extends AbstractGameTaskImpl {
    private final int radius;

    public HealPoolTask() {
        radius = (int) Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
    }

    @Override
    public void run() {
        if (!gameWrapper.getStorage().arePoolEnabled()) {
            return;
        }

        final var storage = gameWrapper.getStorage();
        for (var runningTeam : gameWrapper.getRunningTeams()) {
            if (!storage.arePoolEnabled(runningTeam)) {
                continue;
            }

            for (var teamPlayer : runningTeam.getConnectedPlayers()) {
                if (runningTeam.getTargetBlockLocation().isInRange(teamPlayer.getLocation(), radius)) {
                    teamPlayer.addPotionEffect(PotionEffectHolder.of(new PotionEffect(PotionEffectType.REGENERATION, 30, 1)));
                }
            }
        }
    }
}
