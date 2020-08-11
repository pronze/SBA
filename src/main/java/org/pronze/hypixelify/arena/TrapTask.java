package org.pronze.hypixelify.arena;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class TrapTask extends BukkitRunnable {

    private Game game;
    private Arena arena;

    public TrapTask(Arena arena) {
        if(arena == null) return;
        this.game = arena.getGame();
        runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (game == null || game.getStatus() != GameStatus.RUNNING) {
            game = null;
            arena = null;
            cancel();
        }

        if (!arena.trapInstantiate()) return;

        for (Player player : game.getConnectedPlayers()) {
            for (RunningTeam rt : arena.purchasedTrap.keySet()) {
                if (!arena.purchasedTrap.get(rt)) continue;
                if (rt.isPlayerInTeam(player)) continue;

                if (rt.getTargetBlock().distanceSquared(player.getLocation()) <= arena.radius) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 2));
                    arena.purchasedTrap.put(rt, false);
                    rt.getConnectedPlayers().forEach(pl -> sendTitle(pl, "§cTrap Triggered!", "§eSomeone has entered your base!",
                            20, 40, 20));
                }
            }
        }

    }
}
