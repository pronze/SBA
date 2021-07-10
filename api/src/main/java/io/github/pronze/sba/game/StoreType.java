package io.github.pronze.sba.game;

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
        switch (storeFile) {
            case "shop.yml":
                return NORMAL;
            case "upgradeShop.yml":
                return UPGRADES;
        }
        return NORMAL;
    }
}
