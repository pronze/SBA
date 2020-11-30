package io.pronze.hypixelify.game;

import org.bukkit.Location;

public class TeamData {

    private int sharpness;
    private int protection;
    private boolean purchasedPool;
    private boolean purchasedTrap;
    private Location targetBlockLoc;

    public TeamData(int sharpness, int protection,
                    boolean purchasedPool, boolean purchasedTrap,
                    Location targetBlockLoc){
        this.sharpness = sharpness;
        this.protection = protection;
        this.purchasedPool = purchasedPool;
        this.purchasedTrap = purchasedTrap;
        this.targetBlockLoc = targetBlockLoc;
    }

    public int getSharpness() {
        return sharpness;
    }

    public void setSharpness(int sharpness) {
        this.sharpness = sharpness;
    }

    public int getProtection() {
        return protection;
    }

    public void setProtection(int protection) {
        this.protection = protection;
    }

    public boolean isPurchasedPool() {
        return purchasedPool;
    }

    public void setPurchasedPool(boolean purchasedPool) {
        this.purchasedPool = purchasedPool;
    }

    public boolean isPurchasedTrap() {
        return purchasedTrap;
    }

    public void setPurchasedTrap(boolean purchasedTrap) {
        this.purchasedTrap = purchasedTrap;
    }

    public Location getTargetBlockLoc() {
        return targetBlockLoc;
    }

    public void setTargetBlockLoc(Location targetBlockLoc) {
        this.targetBlockLoc = targetBlockLoc;
    }
}
