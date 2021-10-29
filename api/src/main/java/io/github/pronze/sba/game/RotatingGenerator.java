package io.github.pronze.sba.game;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.world.LocationHolder;

import java.util.List;

/**
 * Represents an implementation for the RotatingGenerator.
 */
public interface RotatingGenerator {

    /**
     * Updates the hologram of the rotating generator with the specified lines.
     * @param newLines lines to be updated over the current lines
     */
    void update(@NotNull List<Component> newLines);

    /**
     * Destroys the hologram and rotating entity completely from viewers.
     */
    void destroy();

    /**
     * Sets the location of the hologram and rotating entity.
     * @param location the location to teleport the entity to
     */
    void setLocation(@NotNull LocationHolder location);

    /**
     * Spawns the RotatingGenerator for the specified viewers with packets.
     * @param viewers the viewers to be displayed this generator
     */
    void spawn();

    /**
     *
     * @param player adds the player as a viewer to this RotatingGenerator
     */
    void addViewer(@NotNull PlayerWrapper player);

    /**
     *
     * @param player removes the player as a viewer to this RotatingGenerator
     */
    void removeViewer(@NotNull PlayerWrapper player);

    boolean isType(Material material);

    void incrementTier();
}
