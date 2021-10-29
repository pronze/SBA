package io.github.pronze.sba.data;

import io.github.pronze.sba.wrapper.RunningTeamWrapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.world.LocationHolder;

/**
 * Represents the data of a team.
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@Data
public class GameTeamData {
    /**
     * Current level of sharpness for the team.
     * Enchant applied is {@link org.bukkit.enchantments.Enchantment#DAMAGE_ALL}
     */
    private int sharpness;

    /**
     * Current level of protection for the team.
     * Enchant applied is {@link org.bukkit.enchantments.Enchantment#PROTECTION_ENVIRONMENTAL}
     */
    private int protection;

    /**
     * Current level of efficiency for the team.
     * Enchant applied is {@link org.bukkit.enchantments.Enchantment#DIG_SPEED}
     */
    private int efficiency;

    /**
     * A boolean representing whether the team has purchased the Heal Pool upgrade from the Upgrades store.
     */
    private boolean purchasedPool;

    /**
     * A boolean representing whether the team has purchased the Blind Trap upgrade from the Upgrades store.
     */
    private boolean purchasedBlindTrap;

    /**
     * A boolean representing whether the team has purchased the Miner Trap upgrade from the Upgrades store.
     */
    private boolean purchasedMinerTrap;

    /**
     * A boolean representing whether the team has purchased the Dragon upgrade from the Upgrades store.
     */
    private boolean purchasedDragonUpgrade;

    /**
     * Location of the target block of the team.
     */
    private final LocationHolder targetBlockLoc;

    /**
     * Constructs a new GameTeamData instance.
     * @param team the team for construction of GameTeamData instance
     * @return a new GameTeamData instance
     */
    public static GameTeamData of(@NotNull RunningTeamWrapper team) {
        return new GameTeamData(team.getTargetBlockLocation());
    }
}
