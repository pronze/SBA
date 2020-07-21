package org.pronze.hypixelify.database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.Party.Party;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;

import java.util.Objects;
import java.util.UUID;

public class PlayerDatabase {

    private UUID player;
    private boolean isInParty = false;
    private boolean isInvited = false;
    private Party party;
    private int expiredTime = 60;
    private Party invitedParty;
    private int timeout = 60;
    private String name;
    private Player pInstance;

    public PlayerDatabase(Player player){
        init();
        this.player = player.getUniqueId();
        name = player.getDisplayName();
        pInstance = player;
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
        return Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getKills() +
                Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getCurrentKills();
    }

    public int getDeaths(){
         return Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getDeaths() +
                Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getCurrentDeaths();
    }

    public int getWins(){
        return Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getWins() +
                Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getCurrentWins();
    }

    public int getBedDestroyed(){
        return Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getDestroyedBeds() +
                Main.getPlayerStatisticsManager().getStatistic(Bukkit.getPlayer(player)).getCurrentDestroyedBeds();
    }

    public void init(){
        new BukkitRunnable(){
            @Override
            public void run() {
                updateDatabase();
            }
        }.runTaskLater(Hypixelify.getInstance(), 1L);
    }

    public void updateDatabase(){
        if(isInParty && getParty().getPlayers().size() < 1 && party.getInvitedMembers().size() < 1
        && getParty().getLeader().getUniqueId().equals(player)){
                for (Player pl : party.getAllPlayers()) {
                    if(pl != null && pl.isOnline()) {
                        for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.disband-inactivity")) {
                            pl.sendMessage(ShopUtil.translateColors(st));
                        }
                    }
                    party.removeMember(pl);
                    if(Hypixelify.getInstance().playerData.get(pl.getUniqueId()) != null) {
                        Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setIsInParty(false);
                        Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setParty(null);
                    }P
                }

                setParty(null);
                setIsInParty(false);
        }

        if(isInvited){
            expiredTime--;
            if(expiredTime == 0){
                expiredTime = 60;
                isInvited = false;
                if(getInvitedParty().getLeader() != null) {
                    UUID uuidLeader = getInvitedParty().getLeader().getUniqueId();
                    Hypixelify.getInstance().playerData.get(uuidLeader).getParty().removeInvitedMember(pInstance);
                }
                setInvitedParty(null);
            }
        } if(Bukkit.getPlayer(player) == null){
            timeout--;
            if(timeout ==0){
                if(isInParty) {
                    Hypixelify.getInstance().playerData.get(player).getParty().removeMember(pInstance);
                    for (Player pl : Hypixelify.getInstance().playerData.get(player).getParty().getAllPlayers()) {
                        if (!player.equals(pl.getUniqueId())) {
                            for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-left")) {
                                pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", name));
                            }
                        }
                    }
                } if (isInvited()){
                    getInvitedParty().removeInvitedMember(pInstance);
                    setInvitedParty(null);
                    setInvited(false);
                }
                Hypixelify.getInstance().playerData.remove(player);
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
