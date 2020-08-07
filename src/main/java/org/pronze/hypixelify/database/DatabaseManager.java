package org.pronze.hypixelify.database;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class DatabaseManager {
    private HashMap<UUID, PlayerDatabase> playerData;

    public DatabaseManager(){
        playerData = new HashMap<>();
    }

    public void createDatabase(Player player){
        if(playerData.containsKey(player.getUniqueId())) return;
        playerData.put(player.getUniqueId(), new PlayerDatabase(player));
     }

     public void deleteDatabase(Player player){
        playerData.remove(player.getUniqueId());
     }

     public void handleOffline(Player player){
        if(!playerData.containsKey(player.getUniqueId())) return;

        getDatabase(player).handleOffline();
        playerData.remove(player.getUniqueId());
     }


     public void destroy(){
        playerData.clear();
        playerData = null;
     }

    public org.pronze.hypixelify.database.PlayerDatabase getDatabase(Player player){
        return playerData.get(player.getUniqueId());
    }



    public void updateAll(){
        if(playerData == null || playerData.isEmpty()) return;

        for(PlayerDatabase db : playerData.values()){
            if(db == null) continue;
            db.updateDatabase();
        }
    }

}
