package pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.player.PlayerWrapper;
import org.screamingsandals.bedwars.lib.player.SenderWrapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerWrapperPostUnregisterEvent;
import pronze.hypixelify.api.events.SBAPlayerWrapperPreUnregisterEvent;
import pronze.hypixelify.api.events.SBAPlayerWrapperRegisteredEvent;
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
        registerMapping();
    }

    private void registerMapping() {
        PlayerMapper.UNSAFE_getPlayerConverter()
                .registerW2P(PlayerWrapperImpl.class, wrapper -> {
                    if (wrapper.getType() == SenderWrapper.Type.PLAYER) {
                        return playerData.get(wrapper.as(PlayerWrapper.class).getUuid());
                    }
                    return null;
                });
    }


    @Override
    public void register(Player player) {
        final var playerWrapper = new PlayerWrapperImpl(player);
        playerData.put(player.getUniqueId(), playerWrapper);
        Logger.trace("Registered player: {}", player.getName());
        SBAHypixelify
                .getInstance()
                .getServer()
                .getPluginManager()
                .callEvent(new SBAPlayerWrapperRegisteredEvent(playerWrapper));
    }

    @Override
    public void unregister(Player player) {
        if (!playerData.containsKey(player.getUniqueId())) {
            return;
        }
        final var playerWrapper = playerData.get(player.getUniqueId());
        SBAHypixelify
                .getInstance()
                .getServer()
                .getPluginManager()
                .callEvent(new SBAPlayerWrapperPreUnregisterEvent(playerWrapper));

        playerData.remove(player.getUniqueId());
        Logger.trace("Unregistered player: {}", player.getName());

        SBAHypixelify
                .getInstance()
                .getServer()
                .getPluginManager()
                .callEvent(new SBAPlayerWrapperPostUnregisterEvent(player));
    }

    @Override
    public Optional<PlayerWrapperImpl> get(Player param) {
        if (!playerData.containsKey(param.getUniqueId())) {
            return Optional.empty();
        }
        return Optional.of(playerData.get(param.getUniqueId()));
    }
}
