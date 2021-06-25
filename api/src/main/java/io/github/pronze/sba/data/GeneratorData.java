package io.github.pronze.sba.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.game.ItemSpawner;

@Data
@RequiredArgsConstructor
public class GeneratorData {
    private final ItemSpawner itemSpawner;
    private final ItemStack itemStack;
    private int tierLevel;
    private int time;
}
