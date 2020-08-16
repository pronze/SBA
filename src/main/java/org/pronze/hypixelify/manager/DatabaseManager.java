package org.pronze.hypixelify.manager;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;

import java.util.HashMap;
import java.util.UUID;

public class DatabaseManager {
    private HashMap<UUID, PlayerDatabase> playerData;

    public DatabaseManager() {
        playerData = new HashMap<>();
    }

    public void createDatabase(Player player) {
        if (playerData.containsKey(player.getUniqueId())) return;
        playerData.put(player.getUniqueId(), new PlayerDatabase(player));
    }

    public void deleteDatabase(Player player) {
        playerData.remove(player.getUniqueId());
    }

    public void handleOffline(Player player) {
        if (!playerData.containsKey(player.getUniqueId())) return;

        getDatabase(player).handleOffline().thenAccept(bool ->{
            if(bool){
                Hypixelify.debug("Deleted database of: " + player.getName());
                deleteDatabase(player);
                updateAll();
            }
        });
    }


    public void destroy() {
        playerData.clear();
        playerData = null;
    }

    public org.pronze.hypixelify.database.PlayerDatabase getDatabase(Player player) {
        return playerData.get(player.getUniqueId());
    }


    public void updateAll() {
        if (playerData == null || playerData.isEmpty()) return;

        for (PlayerDatabase db : playerData.values()) {
            if (db == null) continue;
            db.updateDatabase();
        }
    }

}
