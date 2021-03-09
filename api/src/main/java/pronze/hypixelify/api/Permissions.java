package pronze.hypixelify.api;

import lombok.Getter;

//TODO:

@Getter
public enum Permissions {
    UPGRADE("hypixelify.upgrade");

    private final String key;

    Permissions(String key) { this.key  = key; }
}
