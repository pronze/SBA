package pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.player.SenderWrapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerWrapperPostUnregisterEvent;
import pronze.hypixelify.api.events.SBAPlayerWrapperPreUnregisterEvent;
import pronze.hypixelify.api.events.SBAPlayerWrapperRegisteredEvent;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.game.PlayerWrapper;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.utils.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AutoInitialize
public class PlayerWrapperService implements WrapperService<Player, PlayerWrapper> {
    private final Map<UUID, PlayerWrapper> playerData = new ConcurrentHashMap<>();

    public static PlayerWrapperService getInstance() {
        return Core.getObjectFromClass(PlayerWrapperService.class);
    }

    public PlayerWrapperService() {
        Bukkit.getOnlinePlayers().forEach(this::register);
        registerMapping();
    }

    private void registerMapping() {
        PlayerMapper.UNSAFE_getPlayerConverter()
                .registerW2P(PlayerWrapper.class, wrapper -> {
                    if (wrapper.getType() == SenderWrapper.Type.PLAYER) {
                        return playerData.get(wrapper.as(org.screamingsandals.bedwars.lib.player.PlayerWrapper.class).getUuid());
                    }
                    return null;
                });
    }

    @Override
    public void register(Player player) {
        final var playerWrapper = new PlayerWrapper(player);
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
    public Optional<PlayerWrapper> get(Player param) {
        return Optional.ofNullable(playerData.get(param.getUniqueId()));
    }
}
