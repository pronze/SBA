package io.github.pronze.sba.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.Team;

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
     * Current level of efficiency for the team.
     * Enchant applied is {@link org.bukkit.enchantments.Enchantment#KNOCKBACK}
     */
    private int knockback;

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
    private final Location targetBlockLoc;

    /**
     * Constructs a new GameTeamData instance.
     * @param team the team for construction of GameTeamData instance
     * @return a new GameTeamData instance
     */
    public static GameTeamData of(@NotNull Team team) {
        return new GameTeamData(team.getTargetBlock());
    }

    private Map<String,Integer> enchantLevel = new HashMap<>();
    public Integer get(String propertyName) {
        return enchantLevel.get(propertyName);
    }

    public void set(String propertyName, @NotNull Integer level) {
        enchantLevel.put(propertyName,level);
    }
}
