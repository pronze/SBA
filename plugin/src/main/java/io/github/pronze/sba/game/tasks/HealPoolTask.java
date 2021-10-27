package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.config.SBAConfig;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealPoolTask extends AbstractGameTaskImpl {
    private final double radius;

    public HealPoolTask() {
        radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
    }

    @Override
    public void run() {
        if (!arena.getStorage().arePoolEnabled()) {
            return;
        }

        final var storage = arena.getStorage();
        for (var runningTeam : arena.getGame().getRunningTeams()) {
            if (!storage.arePoolEnabled(runningTeam)) {
                continue;
            }

            for (var teamPlayer : runningTeam.getConnectedPlayers()) {
                if (runningTeam.getTargetBlock().distanceSquared(teamPlayer.getLocation()) <= radius) {
                    teamPlayer.addPotionEffect(new PotionEffect(
                            PotionEffectType.REGENERATION, 30, 1
                    ));
                }
            }
        }
    }
}
