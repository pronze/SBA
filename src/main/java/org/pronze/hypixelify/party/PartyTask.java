package org.pronze.hypixelify.party;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;

import java.util.ArrayList;
import java.util.List;

public class PartyTask extends BukkitRunnable {

    private List<PlayerDatabase> toBeRemoved = new ArrayList<>();
    public PartyTask(){
        runTaskTimer(Hypixelify.getInstance(), 20L, 20L);
    }

    @Override
    public void run() {

        if( Hypixelify.getInstance().playerData == null || Hypixelify.getInstance().playerData.isEmpty() ) return;

        for(PlayerDatabase playerDatabase : Hypixelify.getInstance().playerData.values()){
            if(playerDatabase != null){
                playerDatabase.updateDatabase();
                if(playerDatabase.toBeRemoved()){
                    toBeRemoved.add(playerDatabase);
                }
            }
        }

        if(!toBeRemoved.isEmpty()) {
            for (PlayerDatabase db : toBeRemoved) {
                if (db != null) {
                    Hypixelify.getInstance().playerData.remove(db.getPlayerUUID());
                }
            }
            toBeRemoved.clear();
        }

    }

}
