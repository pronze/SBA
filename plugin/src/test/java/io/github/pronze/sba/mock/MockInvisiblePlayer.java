package io.github.pronze.sba.mock;

import io.github.pronze.sba.game.IArena;
import io.github.pronze.sba.game.InvisiblePlayer;
import io.github.pronze.sba.utils.Logger;
import org.bukkit.entity.Player;

public class MockInvisiblePlayer extends InvisiblePlayer {

    public MockInvisiblePlayer(Player player, IArena arena) {
        super(player, arena);
    }

    @Override
    public void vanish() {
        Logger.trace("Hiding player: {} for invisibility", getHiddenPlayer().getName());
        setHidden(true);
    }

    @Override
    public void showPlayer() {
        Logger.trace("UnHiding player: {} for invisibility", getHiddenPlayer().getName());
        setHidden(false);
    }
}
