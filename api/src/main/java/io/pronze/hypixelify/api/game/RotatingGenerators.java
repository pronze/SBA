package io.pronze.hypixelify.api.game;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.ItemSpawner;

import java.util.List;

public interface RotatingGenerators {


    public ArmorStand getArmorStandEntity();

    public void setLocation(Location location);

    public RotatingGenerators spawn(List<Player> players);

    public void update(List<String> lines);

    public ItemStack getItemStack();

    public void setItemStack(ItemStack itemStack);

    public ItemSpawner getItemSpawner();

    public void setLine(int index, String line);

    public void destroy();


}
