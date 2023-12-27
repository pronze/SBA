package io.github.pronze.sba.game;

import io.github.pronze.sba.AddonAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the events related to the upgrades of Rotating Generators.
 */
@RequiredArgsConstructor
@Getter
public class GameTierEvent {
    /*
     * DIAMOND_GEN_UPGRADE_TIER_II("Diamond-II"),
     * EMERALD_GEN_UPGRADE_TIER_II("Emerald-II"),
     * DIAMOND_GEN_UPGRADE_TIER_III("Diamond-III"),
     * EMERALD_GEN_UPGRADE_TIER_III("Emerald-III"),
     * DIAMOND_GEN_UPGRADE_TIER_IV("Diamond-IV"),
     * EMERALD_GEN_UPGRADE_TIER_V("Emerald-IV"),
     * GAME_END("GameEnd");
     */

    public static final GameTierEvent GAME_END = new GameTierEvent("GameEnd");
    private static List<GameTierEvent> events = new ArrayList<>();
    private final String key;

    public static void load() {
        var keys = AddonAPI
                .getInstance()
                .getConfigurator().getSubKeys("upgrades.time");

        events.clear();
        for (String key : keys) {
            var gte = new GameTierEvent(key);
            if (gte.getTime() > 0)
            {
                events.add(gte);
            }
        }
        events.sort(Comparator.comparing(e->((GameTierEvent)e).getTime()));
        events.add(GAME_END);
    }

    public static void forceReload() {
        events.clear();
        load();
    }

    public static GameTierEvent first() {
        if (events.size() == 0)
            load();
        return events.get(0);
    }

    public int getTime() {
        return AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("upgrades.time." + key, Integer.MAX_VALUE);
    }

    public static GameTierEvent ofOrdinal(int ordinal) {
        if (ordinal < events.size())
            return events.get(ordinal);
        else
            return GAME_END;
    }

    public GameTierEvent getNextEvent() {
        int ordinal = events.indexOf(this);
        return ofOrdinal(ordinal + 1);
    }
}
