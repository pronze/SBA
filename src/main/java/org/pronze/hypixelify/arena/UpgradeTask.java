package org.pronze.hypixelify.arena;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class UpgradeTask extends BukkitRunnable {

    private BukkitTask task;
    private int time;
    private Game game;
    private Map<Integer, String> Tiers = new HashMap<>();
    private Map<Integer, Integer> tier_timer = new HashMap<>();
    private int tier = 1;
    SimpleDateFormat dateFormat;

    public UpgradeTask(Game game){
        this.game = game;
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
            cancel();
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

    public void cancelProcess(){
        task.cancel();
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
