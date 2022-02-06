package io.github.pronze.sba.visual.generator;


import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.player.PlayerWrapper;
import java.util.List;

/**
 * Represents an implementation for the RotatingGenerator.
 */
public interface RotatingGenerator {

    /**
     * Updates the hologram of the rotating generator with the specified lines.
     *
     * @param newLines lines to be updated over the current lines
     */
    void updateLines(@NotNull List<Component> newLines);

    /**
     * Destroys the hologram and rotating entity completely from viewers.
     */
    void destroy();

    /**
     * Spawns the RotatingGenerator for the specified viewers with packets.
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

    Hologram getHologram();

    boolean isFull();
}