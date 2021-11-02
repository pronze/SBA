package io.github.pronze.sba.mock;

import io.github.pronze.sba.game.InvisiblePlayerImpl;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.wrapper.game.GameWrapper;
import org.screamingsandals.lib.player.PlayerWrapper;

public class MockInvisiblePlayer extends InvisiblePlayerImpl {

    public MockInvisiblePlayer(PlayerWrapper player, GameWrapper arena) {
        super(player, arena);
    }

    @Override
    public void vanish() {
        Logger.trace("Hiding player: {} for invisibility", this.getPlayer().getName());
        setHidden(true);
    }

    @Override
    public void showPlayer() {
        Logger.trace("UnHiding player: {} for invisibility", this.getPlayer().getName());
        setHidden(false);
    }
}
