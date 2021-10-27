package io.github.pronze.sba.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the UpgradeType [Emerald/Diamond]
 */
@RequiredArgsConstructor
@Getter
public enum GeneratorUpgradeType {
    UNKNOWN(null),
    DIAMOND(Material.DIAMOND),
    EMERALD(Material.EMERALD);

    private final Material material;

    public static GeneratorUpgradeType fromString(@NotNull String type) {
        try {
            return GeneratorUpgradeType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            if (type.toLowerCase().contains("diamond")) {
                return GeneratorUpgradeType.DIAMOND;
            }
            if (type.toLowerCase().contains("emerald")) {
                return GeneratorUpgradeType.EMERALD;
            }
            return GeneratorUpgradeType.UNKNOWN;
        }
    }
}
