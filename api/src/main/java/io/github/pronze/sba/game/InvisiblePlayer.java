package io.github.pronze.sba.game;

import org.bukkit.entity.Player;

public interface InvisiblePlayer {
    void vanish();

    void showPlayer();

    Player getHiddenPlayer();

    void setHidden(boolean hidden);

    boolean isJustEquipped();

    void setJustEquipped(boolean justEquipped);
}
