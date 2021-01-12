package pronze.hypixelify.service;

import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.game.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pronze.hypixelify.utils.Logger;

import java.util.*;

public class PlayerWrapperService implements WrapperService<Player> {

    private final Map<UUID, PlayerWrapper> playerData = new HashMap<>();
    private final Map<UUID, BukkitTask> deletionTasks = new HashMap<>();

    public PlayerWrapperService() {
        Bukkit.getOnlinePlayers().stream().filter(Objects::nonNull).forEach(this::register);
    }

    public void handleOffline(Player player) {
        if (!playerData.containsKey(player.getUniqueId())) return;
        final var partyManager = SBAHypixelify.getPartyManager();
        final var playerDatabase = getWrapper(player);

        if(partyManager == null){
            unregister(player);
            return;
        }

        deletionTasks.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> {
            partyManager.removeFromInvitedParty(player);
            if (playerDatabase.isInParty() && playerDatabase.getPartyLeader() != null) {
                partyManager.databaseDeletionFromParty(player, playerDatabase.getPartyLeader());
            }
            unregister(player);
            updateAll();
            deletionTasks.remove(player.getUniqueId());
        }, 20L * 60));
    }

    public PlayerWrapper getWrapper(Player player) {
        return playerData.get(player.getUniqueId());
    }

    public void updateAll() {
        if (playerData.isEmpty()) return;

        Bukkit.getScheduler().runTask(SBAHypixelify.getInstance(), ()->{
            playerData.values()
                    .forEach(PlayerWrapper::updateDatabase);
        });

    }

    @Override
    public void register(Player player) {
        playerData.put(player.getUniqueId(), new PlayerWrapper(player));
        Logger.trace("Registered player: {}", player.getName());
        cancelTasksIfExists(player);
    }

    public void register(UUID uuid) {
        register(Objects.requireNonNull(Bukkit.getPlayer(uuid)));
    }

    public void cancelTasksIfExists(Player player){
        final UUID playerUUID = player.getUniqueId();

        if(deletionTasks.containsKey(playerUUID)) {
            final var deletionTask = deletionTasks.get(playerUUID);
            if(deletionTask != null && !deletionTask.isCancelled()) {
                try {
                    deletionTask.cancel();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    @Override
    public void unregister(Player player) {
        playerData.remove(player.getUniqueId());
        Logger.trace("Unregistered player: ", player.getName());
    }
}
