package io.github.pronze.sba.inventory;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;

import java.util.List;

public interface GamesInventory {

    static GamesInventory getInstance() {
        return ServiceManager.get(GamesInventory.class);
    }

    /**
     *
     * @param player
     * @param type
     * @return
     */
    boolean openForPlayer(@NotNull PlayerWrapper player, @NotNull String type);

    @NotNull
    List<String> getInventoryNames();
}
