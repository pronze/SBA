package org.pronze.hypixelify.database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.Party.Party;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import java.util.UUID;

public class PlayerDatabase {

    private UUID player;
    private boolean isInParty = false;
    private boolean isInvited = false;
    private int expiredTime = 60;
    private Party invitedParty;
    private int timeout = 60;
    private String name;
    private Player pInstance;
    private Player partyLeader;


    public String getName(){
        return name;
    }
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

    public void setPartyLeader(Player player){
        partyLeader = player;
    }

    public Player getPartyLeader(){
        return partyLeader;
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
        if(isInvited){
            expiredTime--;
            if(expiredTime == 0){
                expiredTime = 60;
                isInvited = false;
                if(partyLeader != null) {
                    Hypixelify.getInstance().partyManager.parties.get(partyLeader).removeInvitedMember(pInstance);
                }
                setInvitedParty(null);
            }
        }

        if(Bukkit.getPlayer(player) == null){
            timeout--;
            if(timeout ==0){
                if(isInParty && partyLeader != null) {
                    Hypixelify.getInstance().partyManager.parties.get(partyLeader).removeMember(pInstance);
                    if(!partyLeader.getUniqueId().equals(player)) {
                        for (Player pl : Hypixelify.getInstance().partyManager.parties.get(partyLeader).getAllPlayers()) {
                            if (!player.equals(pl.getUniqueId())) {
                                for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-left")) {
                                    pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", name));
                                }
                            }
                        }
                    } else if(Hypixelify.getInstance().partyManager.parties.get(pInstance) != null) {
                        Party party = Hypixelify.getInstance().partyManager.parties.get(pInstance);
                        for(Player pl : party.getAllPlayers()) {
                            if (pl != null) {
                                if(pl.isOnline()) {
                                    for (String str : Hypixelify.getConfigurator().config.getStringList("party.message.disband")) {
                                        pl.sendMessage(ShopUtil.translateColors(str));
                                    }
                                }
                                if(Hypixelify.getInstance().playerData.get(pl.getUniqueId()) != null){
                                    Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setIsInParty(false);
                                    Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setPartyLeader(null);
                                }
                            }
                        }
                        Hypixelify.getInstance().partyManager.parties.get(partyLeader).disband();
                        Hypixelify.getInstance().partyManager.parties.remove(partyLeader);
                    }
                } if (isInvited()){
                    if(getInvitedParty().getLeader() != null) {
                        Hypixelify.getInstance().partyManager.parties.get(partyLeader).removeInvitedMember(pInstance);
                    }
                    setInvitedParty(null);
                    setInvited(false);
                    setPartyLeader(null);
                }
                Hypixelify.getInstance().playerData.remove(player);
                return;
            }
        } else if(timeout < 60){
            timeout = 60;
        }

        if(isInParty && partyLeader!=null && Hypixelify.getInstance().partyManager.parties.get(partyLeader).getAllPlayers().size() <= 1 &&
                Hypixelify.getInstance().partyManager.parties.get(partyLeader).getInvitedMembers().size() < 1 && Hypixelify.getInstance().partyManager.parties.get(partyLeader).getOfflinePlayers() == null){
            if(pInstance != null && pInstance.isOnline()) {
                for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.disband-inactivity")) {
                    pInstance.sendMessage(ShopUtil.translateColors(st));
                }
            }
                setIsInParty(false);
                Hypixelify.getInstance().partyManager.parties.get(partyLeader).disband();
                Hypixelify.getInstance().partyManager.parties.remove(partyLeader);
                setPartyLeader(null);
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

  // public Party getParty(){
  //     if(!isInParty)
  //         return null;

  //     return Hypixelify.getInstance().partyManager.parties.get(partyLeader);
  // }

}
