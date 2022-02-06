package io.github.pronze.sba.game;

import io.github.pronze.sba.SBWAddonAPI;
import io.github.pronze.sba.lang.LangKeys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.utils.AdventureHelper;

import java.util.Arrays;

/**
 * Represents all the occurring events during a BedWars game match.
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

    @NotNull
    public String getTranslatedTitle() {
        final var translatedDiamond = AdventureHelper.toLegacy(Message.of(LangKeys.DIAMOND).asComponent());
        final var translatedEmerald = AdventureHelper.toLegacy(Message.of(LangKeys.EMERALD).asComponent());

        return key.replace("Diamond", translatedDiamond)
                .replace("Emerald", translatedEmerald);
    }

    public int getTime() {
        return SBWAddonAPI
                .getInstance()
                .getConfigurator()
                .node("upgrades", "time",  key)
                .getInt(Integer.MAX_VALUE);
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
