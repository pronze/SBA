package io.github.pronze.sba.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.game.ItemSpawner;

/**
 * Represents the data related to Floating Generators.
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@Data
public class FloatingGeneratorData {
    /**
     * ItemSpawner instance to replicate Floating Generators.
     */
    private final ItemSpawner itemSpawner;

    /**
     * The ItemStack to be applied at the head of the rotating entity
     * By default it obtains the stack from the following materials: {@link org.bukkit.Material#EMERALD_BLOCK}, {@link org.bukkit.Material#DIAMOND_BLOCK}.
     */
    private final ItemStack itemStack;

    /**
     * The current Tier level of the floating generator. Defaults to 1.
     */
    private int tierLevel;

    /**
     * The current time elapsed since its cycle.
     * Refer {@link ItemSpawner#getItemSpawnerType()#getTime()}
     */
    private int time;
}
