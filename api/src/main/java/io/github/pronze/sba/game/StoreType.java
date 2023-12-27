package io.github.pronze.sba.game;

import org.apache.commons.lang.StringUtils;

/**
 * Represents the different store types available.
 */
public enum StoreType {
    /**
     * Enum for UpgradesStore, for team upgrades
     */
    UPGRADES,
    /**
     * Enum for NormalStore, for individual player items.
     */
    NORMAL;

    public static StoreType of(String storeFile) {
        if(StringUtils.containsIgnoreCase(storeFile,"upgrade"))
            return UPGRADES;
        return NORMAL;
    }
}
