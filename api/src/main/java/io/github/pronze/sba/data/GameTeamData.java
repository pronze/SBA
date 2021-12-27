package io.github.pronze.sba.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.lib.item.meta.EnchantmentHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data of a team.
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@Getter
public class GameTeamData {

    /**
     * Constructs a new GameTeamData instance.
     * @param team the team for construction of GameTeamData instance
     * @return a new GameTeamData instance
     */
    public static GameTeamData of(@NotNull Team team) {
        return new GameTeamData(team.getTargetBlock());
    }

    private final ToggleableSetting<TeamSetting> settings = new ToggleableSetting<>();
    private final Location targetBlockLocation;
    @Getter(AccessLevel.NONE)
    private final Map<EnchantmentHolder, Integer> upgradesMap = new HashMap<>();

    public int getUpgradeLevel(@NotNull EnchantmentHolder holder) {
        return upgradesMap.getOrDefault(holder,0);
    }

    public void setUpgrade(@NotNull EnchantmentHolder holder, int upgradeLevel) {
        upgradesMap.put(holder, upgradeLevel);
    }

    public enum TeamSetting {
        DRAGON_UPGRADE,
        HEAL_POOL,
        BLINDNESS_TRAP,
        MINER_TRAP
    }
}
