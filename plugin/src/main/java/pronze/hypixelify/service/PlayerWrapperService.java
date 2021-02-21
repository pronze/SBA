package pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.game.PlayerWrapper;
import pronze.hypixelify.utils.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerWrapperService implements WrapperService<Player> {
    private final Map<UUID, PlayerWrapper> playerData = new ConcurrentHashMap<>();

    public PlayerWrapperService() {
        Bukkit.getOnlinePlayers().forEach(this::register);
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
