package io.github.pronze.sba;

import lombok.Getter;

//TODO:

@Getter
public enum Permissions {
    UPGRADE("sba.upgrade"),
    SHOUT_BYPASS("sba.shout.unlimited"),
    GENERATE_GAMES_INV("sba.generate.gamesinv");

    private final String key;

    Permissions(String key) { this.key  = key; }
}
