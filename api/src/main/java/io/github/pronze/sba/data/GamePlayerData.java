package io.github.pronze.sba.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data of game player.
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@Data
public class GamePlayerData {
    /**
     * Name of the game player.
     */
    private final String name;

    /**
     * Current number of kills of game player.
     */
    private int kills;

    /**
     * Current number of deaths of game player.
     */
    private int deaths;

    /**
     * Current number of final kills of game player.
     */
    private int finalKills;

    /**
     * Current number of bed destroys of game player.
     */
    private int bedDestroys;

    /**
     * Stored inventory of the game player, contains armour, tools, persistent items etc.
     */
    @NotNull
    private List<ItemStack> inventory = new ArrayList<>();

    /**
     * Constructs a new GamePlayerData instance.
     * @param player the player to create for
     * @return GamePlayerData object linked to the player.
     */
    public static GamePlayerData of(@NotNull PlayerWrapper player) {
        return new GamePlayerData(player.getName());
    }
}
