package org.pronze.hypixelify.database;

import org.bukkit.Location;
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
    private final HashMap<String, Integer> sharpness = new HashMap<>();
    private final HashMap<String, Integer> protection = new HashMap<>();
    private final HashMap<RunningTeam, Boolean> purchasedPool = new HashMap<>();
    private final HashMap<RunningTeam, Location> targetBlockLocation = new HashMap<>();

    public GameStorage(Game game) {
        this.game = game;
    }

    public List<ItemStack> getItemsOfPlayer(Player player) {
        return PlayerItems.get(player);
    }

    public void putPlayerItems(Player player, List<ItemStack> stacks) {
        PlayerItems.put(player, stacks);
    }

    public Set<RunningTeam> getTraps() {
        return purchasedTrap.keySet();
    }

    public Set<RunningTeam> getPools(){
        return purchasedPool.keySet();
    }

    public Location getTargetBlockLocation(RunningTeam rt) {
        return targetBlockLocation.get(rt);
    }

    public Integer getSharpness(String team){
        return sharpness.get(team);
    }

    public Integer getProtection(String team){
        return protection.get(team);
    }

    public void setTrap(RunningTeam rt, boolean b) {
        purchasedTrap.put(rt, b);
    }

    public void setPool(RunningTeam rt, boolean b){
        purchasedPool.put(rt, b);
    }

    public void setSharpness(String team, Integer i) {
        sharpness.put(team, i);
    }

    public void setProtection(String team, Integer i) {
        protection.put(team, i);
    }

    public void setTargetBlockLocation(RunningTeam rt){
        targetBlockLocation.put(rt, rt.getTargetBlock());
    }

    public boolean areTrapsEnabled() {
        return purchasedTrap.containsValue(true);
    }

    public boolean arePoolEnabled(){
        return purchasedPool.containsValue(true);
    }

    public void removeTrapTeam(RunningTeam rt) {
        purchasedTrap.remove(rt);
    }

    public void removePoolTeam(RunningTeam rt){
        purchasedPool.remove(rt);
    }

    public Game getGame() {
        return game;
    }

    public boolean isTrapEnabled(RunningTeam team) {
        purchasedTrap.putIfAbsent(team, false);
        return purchasedTrap.get(team);
    }

    public boolean isPoolEnabled(RunningTeam rt){
        purchasedPool.putIfAbsent(rt, false);
        return  purchasedPool.get(rt);
    }


}
