package org.pronze.hypixelify.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.party.Party;
import org.pronze.hypixelify.api.party.PartyManager;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.ShopUtil;

import java.util.UUID;

public class PlayerDatabase implements org.pronze.hypixelify.api.database.PlayerDatabase{

    private final String name;
    private final Player pInstance;
    private UUID player;
    private boolean isInParty = false;
    private boolean isInvited = false;
    private boolean clearData = false;
    private boolean partyChat = false;
    private int expiredTime = 60;
    private Party invitedParty;
    private int timeout = 60;
    private Player partyLeader;
    private int shout;
    private boolean shouted = false;

    public PlayerDatabase(Player player) {
        this.player = player.getUniqueId();
        name = player.getDisplayName();
        pInstance = player;
        shout = Hypixelify.getConfigurator().config.getInt("shout.time-out", 60);
        init();
        Hypixelify.getInstance().playerData.put(player.getUniqueId(), this);
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
    public UUID getPlayerUUID() {
        return player;
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
                                    for (Player pl : partyManager.getParty(partyLeader).getAllPlayers()) {
                                        if (!player.equals(pl.getUniqueId())) {
                                            for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-left")) {
                                                pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", name));
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
                                            org.pronze.hypixelify.api.database.PlayerDatabase plDatabase = Hypixelify.getInstance().playerData.get(pl.getUniqueId());
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
                        clearData = true;
                        cancel();
                    }
                }
                //if player comes back online, reset the timeout.
                else if (timeout < 60) {
                    timeout = 60;
                    cancel();
                }
            }
        }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
    }

    @Override
    public void updateDatabase() {

        final PartyManager partyManager = Hypixelify.getPartyManager();
        if (!isInParty || !isPartyLeader()) return;

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
    public boolean toBeRemoved() {
        return clearData;
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
                        this.cancel();
                    } else if (!isInvited) {
                        expiredTime = 60;
                        this.cancel();
                    }
                }
            }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
        }
    }


}
