package io.github.pronze.sba.game;

import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.lang.LangKeys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.utils.AdventureHelper;

import java.util.Arrays;

/**
 * Represents the events related to the upgrades of Rotating Generators.
 */
@RequiredArgsConstructor
public enum GameEvent {
    DIAMOND_GEN_UPGRADE_TIER_II("Diamond-II"),
    EMERALD_GEN_UPGRADE_TIER_II("Emerald-II"),
    DIAMOND_GEN_UPGRADE_TIER_III("Diamond-III"),
    EMERALD_GEN_UPGRADE_TIER_III("Emerald-III"),
    DIAMOND_GEN_UPGRADE_TIER_IV("Diamond-IV"),
    EMERALD_GEN_UPGRADE_TIER_V("Emerald-IV"),
    GAME_END("GameEnd");

    @Getter
    private final String key;

    public String getTranslatedTitle() {
        final var translatedDiamond = AdventureHelper.toLegacy(Message.of(LangKeys.DIAMOND).asComponent());
        final var translatedEmerald = AdventureHelper.toLegacy(Message.of(LangKeys.EMERALD).asComponent());

        return key.replace("Diamond", translatedDiamond)
                  .replace("Emerald", translatedEmerald);
    }

    public int getTime() {
        return AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("upgrades.time." + key, Integer.MAX_VALUE);
    }

    public static GameEvent ofOrdinal(int ordinal) {
        return Arrays.stream(values())
                .filter(val -> val.ordinal() == ordinal)
                .findAny()
                .orElse(GameEvent.GAME_END);
    }

    public GameEvent getNextEvent() {
        return ofOrdinal(ordinal() + 1);
    }
}
