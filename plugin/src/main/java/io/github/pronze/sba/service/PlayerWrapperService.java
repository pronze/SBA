package io.github.pronze.sba.service;

import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.entity.Player;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.SenderWrapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

@Service(dependsOn = {
        PlayerMapper.class,
        Logger.class
})
public class PlayerWrapperService {

    public static SBAPlayerWrapper wrapPlayer(Player player) {
        return new SBAPlayerWrapper(player.getName(), player.getUniqueId());
    }

    @OnPostEnable
    public void registerMapping() {
        PlayerMapper.UNSAFE_getPlayerConverter()
                .registerW2P(SBAPlayerWrapper.class, wrapper -> {
                    if (wrapper.getType() == SenderWrapper.Type.PLAYER) {
                        return new SBAPlayerWrapper(wrapper.getName(), wrapper.getUuid());
                    }
                    return null;
                });
    }
}
