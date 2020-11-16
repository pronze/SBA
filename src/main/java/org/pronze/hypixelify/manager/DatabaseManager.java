package org.pronze.hypixelify.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.party.PartyManager;
import org.pronze.hypixelify.database.PlayerDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final Map<UUID, PlayerDatabase> playerData = new HashMap<>();

    public DatabaseManager() {
        Bukkit.getOnlinePlayers().forEach(this::createDatabase);
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
        final PartyManager partyManager = SBAHypixelify.getPartyManager();
        final PlayerDatabase playerDatabase = getDatabase(player);

        if(playerDatabase == null ) return;

        new BukkitRunnable() {
            int timeout = 60;

            @Override
            public void run() {
                if (Bukkit.getPlayer(player.getUniqueId()) == null) {
                    timeout--;
                    if (timeout == 0) {
                        partyManager.removeFromInvitedParty(player);

                        if (playerDatabase.isInParty() && playerDatabase.getPartyLeader() != null) {
                            partyManager.databaseDeletionFromParty(player, playerDatabase.getPartyLeader());
                        }
                        SBAHypixelify.debug("Deleted database of: " + player.getName());
                        deleteDatabase(player);
                        updateAll();
                        cancel();
                    }
                }
                else {
                    cancel();
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
    }




    public org.pronze.hypixelify.database.PlayerDatabase getDatabase(Player player) {
        return playerData.get(player.getUniqueId());
    }


    public void updateAll() {
        if (playerData.isEmpty()) return;

        for (PlayerDatabase db : playerData.values()) {
            if (db == null) continue;
            db.updateDatabase();
        }
    }

}
