package io.github.pronze.sba.game;
import io.github.pronze.sba.AddonAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Represents the events related to the upgrades of Rotating Generators.
 */
@RequiredArgsConstructor
@Getter
public enum GameTierEvent {
    DIAMOND_GEN_UPGRADE_TIER_II("Diamond-II"),
    EMERALD_GEN_UPGRADE_TIER_II("Emerald-II"),
    DIAMOND_GEN_UPGRADE_TIER_III("Diamond-III"),
    EMERALD_GEN_UPGRADE_TIER_III("Emerald-III"),
    DIAMOND_GEN_UPGRADE_TIER_IV("Diamond-IV"),
    EMERALD_GEN_UPGRADE_TIER_V("Emerald-IV"),
    GAME_END("GameEnd");

    private final String key;

    public int getTime() {
        return AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("upgrades.time." + key, Integer.MAX_VALUE);
    }

    public static GameTierEvent ofOrdinal(int ordinal) {
        return Arrays.stream(values())
                .filter(val -> val.ordinal() == ordinal)
                .findAny()
                .orElse(GameTierEvent.GAME_END);
    }

    public GameTierEvent getNextEvent() {
        return ofOrdinal(ordinal() + 1);
    }
}
