package org.pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.party.PartyManager;
import org.pronze.hypixelify.api.service.WrapperService;
import org.pronze.hypixelify.data.PlayerDatabase;
import org.pronze.hypixelify.utils.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerWrapperService implements WrapperService<Player> {

    private final Map<UUID, PlayerDatabase> playerData = new HashMap<>();
    private final Map<UUID, BukkitTask> deletionTasks = new HashMap<>();

    public PlayerWrapperService() {
        Bukkit.getOnlinePlayers().forEach(this::register);
    }


    public void handleOffline(Player player) {
        if (!playerData.containsKey(player.getUniqueId())) return;
        final PartyManager partyManager = SBAHypixelify.getPartyManager();
        final PlayerDatabase playerDatabase = getDatabase(player);

        if (playerDatabase == null) return;

        deletionTasks.put(player.getUniqueId(), Scheduler.runTaskLater(() -> {
            partyManager.removeFromInvitedParty(player);

            if (playerDatabase.isInParty() && playerDatabase.getPartyLeader() != null) {
                partyManager.databaseDeletionFromParty(player, playerDatabase.getPartyLeader());
            }
            SBAHypixelify.debug("Deleted database of: " + player.getName());
            unregister(player);
            updateAll();
            deletionTasks.remove(player.getUniqueId());
        }, 20L * 60));
    }


    public org.pronze.hypixelify.data.PlayerDatabase getDatabase(Player player) {
        return playerData.get(player.getUniqueId());
    }


    public void updateAll() {
        if (playerData.isEmpty()) return;

        for (PlayerDatabase db : playerData.values()) {
            if (db == null) continue;
            db.updateDatabase();
        }
    }

    @Override
    public void register(Player player) {
        playerData.put(player.getUniqueId(), new PlayerDatabase(player));
        SBAHypixelify.debug("Registered player: " +
                player.getDisplayName());
        cancelTasksIfExists(player);
    }

    public void cancelTasksIfExists(Player player){
        final UUID playerUUID = player.getUniqueId();

        if(deletionTasks.containsKey(playerUUID)) {
            BukkitTask deletionTask = deletionTasks.get(playerUUID);
            try {
                deletionTask.cancel();
            } catch (Throwable t) {
                t.printStackTrace();
                SBAHypixelify.debug("Could not cancel player task!");
            }
        }
    }

    @Override
    public void unregister(Player player) {
        playerData.remove(player.getUniqueId());
        SBAHypixelify.debug("Unregistered player: " +
                player.getDisplayName());
    }
}
