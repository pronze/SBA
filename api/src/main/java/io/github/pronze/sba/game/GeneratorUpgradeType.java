package io.github.pronze.sba.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.naming.NameNotFoundException;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;

/**
 * Represents the UpgradeType [Emerald/Diamond]
 */
@Getter
public class GeneratorUpgradeType {
   
    private Material material;
    public static ItemSpawnerType fromString(@NotNull String type) throws NameNotFoundException {
        var spawnerType = Main.getSpawnerType(type.toLowerCase());
        if (spawnerType == null)
        {
            throw new NameNotFoundException("Cannot find a spawner with type ["+type+"], verify config");
        }
        return spawnerType;
    }

    public GeneratorUpgradeType(Material material2) {
        material = material2;
    }
}
