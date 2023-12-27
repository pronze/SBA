package io.github.pronze.sba.service;

import io.github.pronze.sba.events.SBAPlayerWrapperPostUnregisterEvent;
import io.github.pronze.sba.events.SBAPlayerWrapperPreUnregisterEvent;
import io.github.pronze.sba.events.SBAPlayerWrapperRegisteredEvent;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.player.Sender;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceDependencies(dependsOn = {
        Logger.class
})
public class PlayerWrapperService implements WrapperService<Player, SBAPlayerWrapper> {

    public static PlayerWrapperService getInstance() {
        return ServiceManager.get(PlayerWrapperService.class);
    }

    private final Map<UUID, SBAPlayerWrapper> playerData = new ConcurrentHashMap<>();
    private static boolean init = false;
    @OnPostEnable
    public void registerMapping() {
        if(SBA.isBroken())return;
        if(!init)
            Players.UNSAFE_getPlayerConverter()
                    .registerW2P(SBAPlayerWrapper.class, wrapper -> {
                        if (wrapper.getType() == Sender.Type.PLAYER) {
                            if(!playerData.containsKey(wrapper.getUuid())){
                                var player = wrapper.as(Player.class);// Bukkit.getServer().getPlayer(wrapper.getUuid());
                                register(player);
                            }
                            return playerData.get(wrapper.getUuid());
                        }
                        return null;
                    });
        init = true;
        Bukkit.getOnlinePlayers().forEach(this::register);
    }

    @Override
    public void register(Player player) {
        if (playerData.containsKey(player.getUniqueId())) {
            return;
        }
        final var playerWrapper = new SBAPlayerWrapper(player);
        playerData.put(player.getUniqueId(), playerWrapper);
        SBA.getPluginInstance()
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
        SBA.getPluginInstance()
                .getServer()
                .getPluginManager()
                .callEvent(new SBAPlayerWrapperPreUnregisterEvent(playerWrapper));

        playerData.remove(player.getUniqueId());

        SBA.getPluginInstance()
                .getServer()
                .getPluginManager()
                .callEvent(new SBAPlayerWrapperPostUnregisterEvent(player));
    }

    @Override
    public Optional<SBAPlayerWrapper> get(Player param) {
        return Optional.ofNullable(playerData.get(param.getUniqueId()));
    }
}
