package org.pronze.hypixelify;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.database.PlayerDatabase;

import java.util.ArrayList;
import java.util.List;

public class PartyTask extends BukkitRunnable {

    public PartyTask(){
        runTaskTimer(Hypixelify.getInstance(), 20L, 20L);
    }

    @Override
    public void run() {

        List<PlayerDatabase> toBeRemoved = null;
        if( Hypixelify.getInstance().playerData == null || Hypixelify.getInstance().playerData.isEmpty() ) return;

        for(PlayerDatabase playerDatabase : Hypixelify.getInstance().playerData.values()){
            if(playerDatabase != null){
                playerDatabase.updateDatabase();
                if(playerDatabase.toBeRemoved()){
                    toBeRemoved = new ArrayList<>();
                    toBeRemoved.add(playerDatabase);
                }
            }
        }

        if(toBeRemoved != null && !toBeRemoved.isEmpty()){
            for(PlayerDatabase db : toBeRemoved){
                if(db != null){
                    Hypixelify.getInstance().playerData.remove(db.getPlayerUUID());
                }
            }
        }

    }

}
