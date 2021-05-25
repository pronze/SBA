package pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.SenderWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerWrapperPostUnregisterEvent;
import pronze.hypixelify.api.events.SBAPlayerWrapperPreUnregisterEvent;
import pronze.hypixelify.api.events.SBAPlayerWrapperRegisteredEvent;
import pronze.hypixelify.api.service.WrapperService;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.utils.Logger;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service(dependsOn = {
        Logger.class
})
public class PlayerWrapperService implements WrapperService<Player, PlayerWrapper> {

    public static PlayerWrapperService getInstance() {
        return ServiceManager.get(PlayerWrapperService.class);
    }

    private final Map<UUID, PlayerWrapper> playerData = new ConcurrentHashMap<>();


    public PlayerWrapperService() {
        Bukkit.getOnlinePlayers().forEach(this::register);
    }

    @OnPostEnable
    public void registerMapping() {
        PlayerMapper.UNSAFE_getPlayerConverter()
                .registerW2P(PlayerWrapper.class, wrapper -> {
                    if (wrapper.getType() == SenderWrapper.Type.PLAYER) {
                        return playerData.get(wrapper.getUuid());
                    }
                    return null;
                });
    }

    @Override
    public void register(Player player) {
        if (playerData.containsKey(player.getUniqueId())) {
            return;
        }
        final var playerWrapper = new PlayerWrapper(player);
        playerData.put(player.getUniqueId(), playerWrapper);
        Logger.trace("Registered player: {}", player.getName());
        SBAHypixelify
                .getPluginInstance()
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
                .getPluginInstance()
                .getServer()
                .getPluginManager()
                .callEvent(new SBAPlayerWrapperPreUnregisterEvent(playerWrapper));

        playerData.remove(player.getUniqueId());
        Logger.trace("Unregistered player: {}", player.getName());

        SBAHypixelify
                .getPluginInstance()
                .getServer()
                .getPluginManager()
                .callEvent(new SBAPlayerWrapperPostUnregisterEvent(player));
    }

    @Override
    public Optional<PlayerWrapper> get(Player param) {
        return Optional.ofNullable(playerData.get(param.getUniqueId()));
    }
}
