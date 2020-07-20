package org.pronze.hypixelify;

import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.database.PlayerDatabase;

public class PartyTask extends BukkitRunnable {

    public PartyTask(){
        runTaskTimer(Hypixelify.getInstance(), 20L, 20L);
    }

    @Override
    public void run() {
        for(PlayerDatabase playerDatabase : Hypixelify.getInstance().playerData.values()){
            if(playerDatabase != null){
                playerDatabase.updateDatabase();
            }
        }
    }
}
