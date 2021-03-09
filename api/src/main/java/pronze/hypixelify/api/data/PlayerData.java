package pronze.hypixelify.api.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class PlayerData {
    private final String name;
    private int kills;
    private int deaths;
    private int finalKills;
    private int bedDestroys;
    private List<ItemStack> inventory = new ArrayList<>();
}
