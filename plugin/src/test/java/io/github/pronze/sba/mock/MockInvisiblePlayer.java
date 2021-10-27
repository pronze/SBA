package io.github.pronze.sba.mock;

import io.github.pronze.sba.game.GameWrapperImpl;
import io.github.pronze.sba.game.InvisiblePlayerImpl;
import io.github.pronze.sba.utils.Logger;
import org.bukkit.entity.Player;

public class MockInvisiblePlayer extends InvisiblePlayerImpl {

    public MockInvisiblePlayer(Player player, GameWrapperImpl arena) {
        super(player, (GameWrapperImpl) arena);
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
