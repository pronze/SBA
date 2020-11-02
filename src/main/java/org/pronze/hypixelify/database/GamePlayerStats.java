package org.pronze.hypixelify.database;

public class GamePlayerStats {

    private int finalKills;
    private int bedDestroys;
    private int kills;
    private int deaths;

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
}
