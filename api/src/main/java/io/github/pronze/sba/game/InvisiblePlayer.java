package io.github.pronze.sba.game;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public interface InvisiblePlayer {

    void show();

    void hide();

    void removeHidden(@NotNull GamePlayer gamePlayer);

    @NotNull
    List<GamePlayer> getHiddenFrom();

    boolean isJustHidden();

    void setJustHidden(boolean isJustHidden);

    boolean isHidden();
}
