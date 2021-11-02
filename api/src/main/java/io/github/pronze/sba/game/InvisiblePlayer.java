package io.github.pronze.sba.game;

import org.screamingsandals.lib.player.PlayerWrapper;

public interface InvisiblePlayer {
    void vanish();

    void showPlayer();

    PlayerWrapper getPlayer();

    void setHidden(boolean hidden);

    boolean isJustEquipped();

    void setJustEquipped(boolean justEquipped);
}
