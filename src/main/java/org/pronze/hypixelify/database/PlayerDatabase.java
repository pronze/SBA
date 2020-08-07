package org.pronze.hypixelify.database;

import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.party.Party;
import org.pronze.hypixelify.api.party.PartyManager;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;

import java.util.UUID;

public class PlayerDatabase implements org.pronze.hypixelify.api.database.PlayerDatabase{

    private final String name;
    private final Player pInstance;
    private UUID player;
    private boolean isInParty = false;
    private boolean isInvited = false;
    private boolean partyChat = false;
    private int expiredTime = 60;
    private int timeout = 60;
    private Party invitedParty;
    private Player partyLeader;
    private int shout;
    private boolean shouted = false;
    private boolean clear = false;

    public PlayerDatabase(Player player) {
        this.player = player.getUniqueId();
        name = player.getDisplayName();
        pInstance = player;
        shout = Hypixelify.getConfigurator().config.getInt("shout.time-out", 60);
        init();
    }

    @Override
    public boolean shouldClear(){
        return clear;
    }

    @Override
    public int getShoutTimeOut(){
        return shout;
    }

    @Override
    public void shout(){
        if(!shouted){
            shouted = true;
            new BukkitRunnable(){
                @Override
                public void run() {
                    shout--;
                    if(shout == 0){
                        shouted = false;
                        shout = Hypixelify.getConfigurator().config.getInt("shout.time-out", 60);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
        }
    }

    @Override
    public boolean canShout(){
        return !shouted;
    }

    @Override
    public boolean getPartyChatEnabled() {
        return partyChat;
    }

    @Override
    public void setPartyChatEnabled(boolean b) {
        partyChat = b;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setIsInParty(boolean bool) {
        isInParty = bool;
    }

    @Override
    public boolean isPartyLeader() {
        if (partyLeader == null)
            return false;
        return player.equals(partyLeader.getUniqueId());
    }

    @Override
    public Party getInvitedParty() {
        return invitedParty;
    }

    @Override
    public void setInvitedParty(Party party) {
        invitedParty = party;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player.getUniqueId();
    }

    @Override
    public Player getPartyLeader() {
        return partyLeader;
    }

    @Override
    public void setPartyLeader(Player player) {
        partyLeader = player;
    }


    @Override
    public void setExpiredTimeTimeout(int timeout) {
        expiredTime = timeout;
    }

    public void init() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateDatabase();
            }
        }.runTaskLater(Hypixelify.getInstance(), 1L);
    }

    @Override
    public int getKills(){
        return Main.getPlayerStatisticsManager().getStatistic(pInstance).getCurrentKills() +
                Main.getPlayerStatisticsManager().getStatistic(pInstance).getKills();
    }

    @Override
    public int getWins(){
        return Main.getPlayerStatisticsManager().getStatistic(pInstance).getWins() +
                Main.getPlayerStatisticsManager().getStatistic(pInstance).getCurrentWins();
    }

    @Override
    public int getBedDestroys(){
        return Main.getPlayerStatisticsManager().getStatistic(pInstance).getCurrentDestroyedBeds() +
                Main.getPlayerStatisticsManager().getStatistic(pInstance).getDestroyedBeds();
    }

    @Override
    public int getDeaths(){
        return Main.getPlayerStatisticsManager().getStatistic(pInstance).getDeaths() +
                Main.getPlayerStatisticsManager().getStatistic(pInstance).getCurrentKills();
    }

    @Override
    public int getXP(){
        int total;
        try {
            total = (getBedDestroys() * 5) + (getKills() * 2);
        }  catch(Exception e){
            return 1;
        }
        return total;
    }

    @Override
    public int getLevel(){
        try {
            int xp = getXP();

            if (xp < 50)
                return 1;
        } catch(Exception e){
            return 1;
        }

        return getXP() / 50;
    }

    @Override
    public String getProgress(){
        String p = "§b{p}§7/§a50";
        int progress;
        try {
            progress = getXP() - (getLevel() * 50);
        } catch(Exception e){
            progress = 1;
        }
        return p
                .replace("{p}", String.valueOf(progress));
    }

    @Override
    public String getCompletedBoxes(){
        int progress;
        try {
            progress = (getXP() - (getLevel() * 50)) * 2;
        } catch(Exception e){
            progress = 1;
        }
        char i;
        i = String.valueOf(Math.abs((long)progress)).charAt(0);
        if(progress < 10) {
            i = '1';
        }
        return "§b" + Strings.repeat("■", Integer.parseInt(String.valueOf(i)))
                + "§7" +  Strings.repeat("■", 10 - Integer.parseInt(String.valueOf(i)));
    }


    @Override
    public double getKD(){
        return  Main.getPlayerStatisticsManager().getStatistic(pInstance).getKD();
    }

    @Override
    public void handleOffline() {
        final PartyManager partyManager = Hypixelify.getPartyManager();

        new BukkitRunnable() {
            @Override
            public void run() {
                //Handle when player goes offline, decrement timeout after every 20 ticks delay
                if (Bukkit.getPlayer(player) == null) {
                    timeout--;
                    if (timeout == 0) {
                        //Handle pending invites
                        if (isInvited()) {
                            if (getInvitedParty().getLeader() != null && partyManager.getParty(partyLeader) != null) {
                                partyManager.getParty(partyLeader).removeInvitedMember(pInstance);
                            }
                            setInvitedParty(null);
                            setInvited(false);
                        }

                        //check if player is in party and remove him, if he's the leader, disband the party.
                        if (isInParty && partyLeader != null) {
                            final Party party = partyManager.getParty(partyLeader);
                            if (party != null) {
                                if (!partyLeader.getUniqueId().equals(player)) {
                                    if(partyManager.getParty(partyLeader).getAllPlayers() != null && !partyManager.getParty(partyLeader).getAllPlayers().isEmpty()) {
                                        for (Player pl : partyManager.getParty(partyLeader).getAllPlayers()) {
                                            if (pl != null && pl.isOnline() && !player.equals(pl.getUniqueId())) {
                                                for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-left")) {
                                                    pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", name));
                                                }
                                            }
                                        }
                                    }
                                    partyManager.getParty(partyLeader).removeMember(pInstance);
                                } else if (partyLeader.getUniqueId().equals(player)) {
                                    for (Player pl : party.getAllPlayers()) {
                                        if (pl != null && !pl.equals(partyLeader)) {
                                            if (pl.isOnline()) {
                                                ShopUtil.sendMessage(pl, Messages.message_disband_inactivity);
                                            }
                                            org.pronze.hypixelify.api.database.PlayerDatabase plDatabase = Hypixelify.getDatabaseManager().getDatabase(pl);
                                            if (plDatabase != null) {
                                                plDatabase.setIsInParty(false);
                                                plDatabase.setPartyLeader(null);
                                            }
                                        }
                                    }
                                    partyManager.getParty(partyLeader).disband();
                                    partyManager.removeParty(partyLeader);
                                }
                                setIsInParty(false);
                                setPartyLeader(null);
                            }
                        }
                        clear = true;
                        cancel();
                    }
                }
                //if player comes back online, reset the timeout.
                else {
                    timeout = 60;
                    cancel();
                }
            }
        }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
    }

    @Override
    public void updateDatabase() {
        if (!isInParty || !isPartyLeader()) return;
        final PartyManager partyManager = Hypixelify.getPartyManager();
        final Party party = partyManager.getParty(partyLeader);
        if (party == null) return;
        if (party.shouldDisband()) {
            if (pInstance != null && pInstance.isOnline()) {
                for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.disband-inactivity")) {
                    pInstance.sendMessage(ShopUtil.translateColors(st));
                }
            }
            setIsInParty(false);
            party.disband();
            partyManager.removeParty(partyLeader);
            setPartyLeader(null);
        }
    }

    @Override
    public boolean isInParty() {
        return isInParty;
    }

    @Override
    public boolean isInvited() {
        return isInvited;
    }

    @Override
    public void setInvited(boolean bool) {
        isInvited = bool;
        final PartyManager partyManager = Hypixelify.getPartyManager();

        if (bool) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    expiredTime--;
                    if (expiredTime == 0 && isInvited) {
                        expiredTime = 60;
                        isInvited = false;
                        final Party party = partyManager.getParty(invitedParty.getLeader());

                        if (invitedParty != null && party != null) {
                            party.removeInvitedMember(pInstance);
                            if (invitedParty.getLeader() != null && invitedParty.getLeader().isOnline())
                                ShopUtil.sendMessage(invitedParty.getLeader(), Messages.message_invite_expired);
                            if (pInstance != null && pInstance.isOnline())
                                ShopUtil.sendMessage(pInstance, Messages.message_invite_expired);
                        }
                        setInvitedParty(null);
                        Hypixelify.getDatabaseManager().updateAll();
                        this.cancel();
                    } else if (!isInvited) {
                        Hypixelify.getDatabaseManager().updateAll();
                        expiredTime = 60;
                        this.cancel();
                    }
                }
            }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
        }
    }


}
