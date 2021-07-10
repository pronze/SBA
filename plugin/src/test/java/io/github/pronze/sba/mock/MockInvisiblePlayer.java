package io.github.pronze.sba.mock;

import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.IArena;
import io.github.pronze.sba.game.InvisiblePlayer;
import io.github.pronze.sba.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.lib.utils.math.Vector3D;

public class MockInvisiblePlayer extends InvisiblePlayer {

    public MockInvisiblePlayer(Player player, IArena arena) {
        super(player, arena);
    }

    @Override
    public void vanish() {
        Logger.trace("Hiding player: {} for invisibility", getPlayer().getName());
        setHidden(true);
    }

    @Override
    public void showPlayer() {
        Logger.trace("UnHiding player: {} for invisibility", getPlayer().getName());
        setHidden(false);
    }
}
