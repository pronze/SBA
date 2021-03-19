package pronze.hypixelify.api;

import lombok.Getter;

//TODO:

@Getter
public enum Permissions {
    UPGRADE("hypixelify.upgrade"),
    SHOUT_BYPASS("hypixelify.shout.unlimited");

    private final String key;

    Permissions(String key) { this.key  = key; }
}
