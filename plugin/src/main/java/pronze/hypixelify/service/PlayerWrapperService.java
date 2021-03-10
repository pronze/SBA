package pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.game.PlayerWrapperImpl;
import pronze.hypixelify.utils.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerWrapperService implements WrapperService<Player, PlayerWrapperImpl> {
    private final Map<UUID, PlayerWrapperImpl> playerData = new ConcurrentHashMap<>();

    public PlayerWrapperService() {
        Bukkit.getOnlinePlayers().forEach(this::register);
    }

    @Override
    public void register(Player player) {
        playerData.put(player.getUniqueId(), new PlayerWrapperImpl(player));
        Logger.trace("Registered player: {}", player.getName());
    }

    @Override
    public void unregister(Player player) {
        playerData.remove(player.getUniqueId());
        Logger.trace("Unregistered player: {}", player.getName());
    }

    @Override
    public Optional<PlayerWrapperImpl> get(Player param) {
        if (!playerData.containsKey(param.getUniqueId())) {
            return Optional.empty();
        }
        return Optional.of(playerData.get(param.getUniqueId()));
    }
}
