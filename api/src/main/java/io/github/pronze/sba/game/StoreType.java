package io.github.pronze.sba.game;

public enum StoreType {
    UPGRADES,
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
