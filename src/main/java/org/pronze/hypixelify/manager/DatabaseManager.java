package org.pronze.hypixelify.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.party.PartyManager;
import org.pronze.hypixelify.database.PlayerDatabase;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

        handleOfflineAsync(player).thenAccept(bool ->{
            if(bool){
                Hypixelify.debug("Deleted database of: " + player.getName());
                deleteDatabase(player);
                updateAll();
            }
        });
    }

    public CompletableFuture<Boolean> handleOfflineAsync(Player player){
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        final PartyManager partyManager = Hypixelify.getPartyManager();
        final PlayerDatabase playerDatabase = Hypixelify.getDatabaseManager().getDatabase(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                //Handle when player goes offline, decrement timeout after every 20 ticks delay
                if (Bukkit.getPlayer(player.getUniqueId()) == null) {
                    playerDatabase.decrementTimeout();
                    if (playerDatabase.timeoutComplete()) {
                        //Handle pending invites
                        partyManager.removeFromInvitedParty(player);

                        //check if player is in party and remove him, if he's the leader, disband the party.
                        if (playerDatabase.isInParty() && playerDatabase.getPartyLeader() != null) {
                            partyManager.databaseDeletionFromParty(player, playerDatabase.getPartyLeader());
                        }

                        completableFuture.complete(true);
                        cancel();
                    }
                }
                //if player comes back online, reset the timeout.
                else {
                    playerDatabase.resetTimeout();
                    completableFuture.complete(false);
                    cancel();
                }
            }
        }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);

        return completableFuture;
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
