package io.github.pronze.sba.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class GamePlayerData {
    private final String name;
    private int kills;
    private int deaths;
    private int finalKills;
    private int bedDestroys;
    private List<ItemStack> inventory = new ArrayList<>();

    public static GamePlayerData from(Player player) {
        return new GamePlayerData(player.getName());
    }
}
