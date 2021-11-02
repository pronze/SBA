package io.github.pronze.sba.wrapper;

import io.github.pronze.sba.wrapper.event.BedWarsEventWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.entity.EntityBasic;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.annotations.Service;

@Service(initAnother = {
        BedWarsEventWrapper.class
})
public final class BedWarsAPIWrapper {

    public static void unregisterGameEntity(@NotNull EntityBasic entity) {
        Main.unregisterGameEntity(entity.as(Entity.class));
    }

    public static boolean isBedWarsSpectator(@NotNull PlayerWrapper playerWrapper) {
        return Main.getPlayerGameProfile(playerWrapper.as(Player.class)).isSpectator;
    }

    public static boolean isPlayerInGame(@NotNull PlayerWrapper playerWrapper) {
        return Main.getPlayerGameProfile(playerWrapper.as(Player.class)).isInGame();
    }
}
