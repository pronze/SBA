package io.pronze.hypixelify.game;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {

    private int kills;
    private int deaths;
    private int finalKills;
    private int bedDestroys;
    private List<ItemStack> inventory = new ArrayList<>();

    public PlayerData(){ }

    public List<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(List<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getFinalKills() {
        return finalKills;
    }

    public void setFinalKills(int finalKills) {
        this.finalKills = finalKills;
    }

    public int getBedDestroys() {
        return bedDestroys;
    }

    public void setBedDestroys(int bedDestroys) {
        this.bedDestroys = bedDestroys;
    }
}
