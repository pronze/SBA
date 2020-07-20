package org.pronze.hypixelify.database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.Party.Party;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.statistics.PlayerStatistic;
import java.util.UUID;

public class PlayerDatabase {

    private UUID player;
    private int kills;
    private int deaths;
    private int wins;
    private int bedDestroyed;
    private int exp;
    private boolean isInParty = false;
    private boolean isInvited = false;
    private Party party;
    private int expiredTime = 60;
    private Party invitedParty;

    public PlayerDatabase(Player player){
        init();
        this.player = player.getUniqueId();
        Hypixelify.getInstance().playerData.put(player.getUniqueId(), this);
    }

    public void setIsInParty(Boolean bool){
        isInParty = bool;
    }
    public void setInvitedParty(Party party){
        invitedParty = party;
    }

    public Party getInvitedParty(){
        return invitedParty;
    }

    public void setPlayer(Player player){
        this.player = player.getUniqueId();
    }

    public UUID getPlayer(){
        return player;
    }

    public int getKills(){
        return kills;
    }

    public int getDeaths(){
        return deaths;
    }

    public int getWins(){
        return wins;
    }

    public int getBedDestroyed(){
        return bedDestroyed;
    }

    public void init(){
        exp = 0;
        kills =0;
        deaths = 0;
        wins = 0;
        bedDestroyed = 0;
        new BukkitRunnable(){
            @Override
            public void run() {
                updateDatabase();
            }
        }.runTaskLater(Hypixelify.getInstance(), 1L);
    }

    public void updateDatabase(){
        PlayerStatistic statistic = Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player));
        kills = statistic.getKills() + statistic.getCurrentKills();
        deaths = statistic.getDeaths() + statistic.getCurrentDeaths();
        wins = statistic.getWins() + statistic.getCurrentWins();
        bedDestroyed = statistic.getDestroyedBeds() + statistic.getCurrentDestroyedBeds();
        if(isInvited){
            expiredTime--;
            if(expiredTime == 0){
                expiredTime = 60;
                isInvited = false;
            }
        }
    }

    public boolean isInParty(){
        return isInParty;
    }

    public boolean isInvited(){
        return isInvited;
    }

    public void setInvited(boolean bool){
        isInvited = bool;
    }

    public Party getParty(){
        if(!isInParty)
            return null;

        return party;
    }

    public void setParty(Party party){
        this.party = party;
    }
}
