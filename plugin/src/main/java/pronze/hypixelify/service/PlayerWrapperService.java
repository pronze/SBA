package pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.game.PlayerWrapper;
import pronze.hypixelify.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerWrapperService implements WrapperService<Player> {
    private final Map<UUID, PlayerWrapper> playerData = new HashMap<>();

    public PlayerWrapperService() {
        Bukkit.getOnlinePlayers().stream().filter(Objects::nonNull).forEach(this::register);
    }

    public PlayerWrapper getWrapper(Player player) {
        return playerData.get(player.getUniqueId());
    }

    @Override
    public void register(Player player) {
        playerData.put(player.getUniqueId(), new PlayerWrapper(player));
        Logger.trace("Registered player: {}", player.getName());
    }

    @Override
    public void unregister(Player player) {
        playerData.remove(player.getUniqueId());
        Logger.trace("Unregistered player: {}", player.getName());
    }
}
