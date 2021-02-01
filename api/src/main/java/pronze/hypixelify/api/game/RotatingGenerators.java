package pronze.hypixelify.api.game;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.api.game.ItemSpawner;

import java.util.List;

public interface RotatingGenerators {


    ArmorStand getArmorStandEntity();

    void setLocation(Location location);

    RotatingGenerators spawn(List<Player> players);

    ItemStack getItemStack();

    void setItemStack(ItemStack itemStack);

    ItemSpawner getItemSpawner();

    void destroy();


}
