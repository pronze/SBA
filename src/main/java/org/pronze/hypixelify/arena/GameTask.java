package org.pronze.hypixelify.arena;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class GameTask extends BukkitRunnable {

    private int time;
    private Game game;
    private Arena arena;

    private Map<Integer, String> Tiers = new HashMap<>();
    private Map<Integer, Integer> tier_timer = new HashMap<>();
    private int tier = 1;
    SimpleDateFormat dateFormat;

    public GameTask(Arena arena){
        this.arena = arena;
        this.game = arena.getGame();
        dateFormat =  new SimpleDateFormat("mm:ss");
        Tiers.put(1, "Diamond-I");
        Tiers.put(2, "Emerald-I");
        Tiers.put(3, "Diamond-II");
        Tiers.put(4, "Emerald-II");
        Tiers.put(5, "Diamond-III");
        Tiers.put(6, "Emerald-III");
        Tiers.put(7, "Diamond-IV");
        Tiers.put(8, "Emerald-IV");
        for(int i = 1; i < 9; i ++){
            tier_timer.put(i, Hypixelify.getConfigurator().config.getInt("upgrades.time." + Tiers.get(i)));
        }
        Tiers.put(9, "Game End");
        tier_timer.put(9, game.getGameTime());
        runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if(game.getStatus() != GameStatus.RUNNING){
            game = null;
            arena = null;
            cancel();
        }

        if (arena.trapInstantiate()) {
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


        if(!Tiers.get(tier).equals(Tiers.get(9))){
            if (time == tier_timer.get(tier)) {
                game.getItemSpawners().forEach(itemSpawner -> {
                    if (tier % 2 == 0) {
                        if (itemSpawner.getItemSpawnerType().getMaterial().equals(Material.DIAMOND))
                            itemSpawner.addToCurrentLevel(0.5);
                    } else {
                        if (itemSpawner.getItemSpawnerType().getMaterial().equals(Material.EMERALD))
                            itemSpawner.addToCurrentLevel(0.5);
                    }
                });
                String MatName = tier % 2 == 0 ? "§aEmerald§6" : "§bDiamond§6";
                game.getConnectedPlayers().forEach(player -> player.sendMessage("{MatName} generator has been upgraded to "
                        .replace("{MatName}" , MatName) + Tiers.get(tier)));
                tier++;
            }
        }

        time++;
    }

    public int getTime(){
        return time;
    }

    public String getFormattedTimeLeft(){
        return dateFormat.format((tier_timer.get(tier) - time) * 1000);
    }

    public String getTier(){
        return Tiers.get(tier);
    }
}