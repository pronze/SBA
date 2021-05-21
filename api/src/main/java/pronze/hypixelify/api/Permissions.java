package pronze.hypixelify.api;

import lombok.Getter;

//TODO:

@Getter
public enum Permissions {
    UPGRADE("hypixelify.upgrade"),
    SHOUT_BYPASS("hypixelify.shout.unlimited"),
    GENERATE_GAMES_INV("hypixelify.generate.gamesinv");

    private final String key;

    Permissions(String key) { this.key  = key; }
}
