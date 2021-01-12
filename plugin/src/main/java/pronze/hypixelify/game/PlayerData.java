package pronze.hypixelify.game;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlayerData {
    private int kills;
    private int deaths;
    private int finalKills;
    private int bedDestroys;
    private List<ItemStack> inventory = new ArrayList<>();
}
