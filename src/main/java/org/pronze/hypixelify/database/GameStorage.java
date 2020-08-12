package org.pronze.hypixelify.database;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GameStorage {
    private final Game game;
    private final HashMap<Player, List<ItemStack>> PlayerItems = new HashMap<>();
    private final HashMap<RunningTeam, Boolean> purchasedTrap = new HashMap<>();


    public List<ItemStack> getItemsOfPlayer(Player player){
        return PlayerItems.get(player);
    }

    public void putPlayerItems(Player player, List<ItemStack> stacks){
        PlayerItems.put(player, stacks);
    }

    public GameStorage(Game game){
        this.game = game;
    }

    public Set<RunningTeam> getTraps(){
        return purchasedTrap.keySet();
    }

    public void setTrap(RunningTeam rt, Boolean b){
        purchasedTrap.put(rt, b);
    }

    public boolean areTrapsEnabled(){
        return purchasedTrap.containsValue(true);
    }

    public void removeTeam(RunningTeam rt){
        purchasedTrap.remove(rt);
    }
    public Game getGame(){
        return game;
    }

    public boolean isTrapEnabled(RunningTeam team){
        purchasedTrap.putIfAbsent(team, false);
        return purchasedTrap.get(team);
    }



}
