package org.pronze.hypixelify.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.party.Party;
import org.pronze.hypixelify.utils.ShopUtil;
import java.util.UUID;

public class PlayerDatabase {

    private final String name;
    private final Player pInstance;
    private UUID player;
    private boolean isInParty = false;
    private boolean isInvited = false;
    private int expiredTime = 60;
    private Party invitedParty;
    private int timeout = 60;
    private Player partyLeader;
    private boolean clearData = false;
    private boolean partyChat = false;

    public PlayerDatabase(Player player) {
        this.player = player.getUniqueId();
        name = player.getDisplayName();
        pInstance = player;
        init();
        Hypixelify.getInstance().playerData.put(player.getUniqueId(), this);
    }

    public boolean getPartyChatEnabled() {
        return partyChat;
    }

    public void setPartyChatEnabled(Boolean b) {
        partyChat = b;
    }

    public String getName() {
        return name;
    }

    public void setIsInParty(Boolean bool) {
        isInParty = bool;
    }

    public boolean isPartyLeader() {
        if (partyLeader == null)
            return false;
        return player.equals(partyLeader.getUniqueId());
    }

    public Party getInvitedParty() {
        return invitedParty;
    }

    public void setInvitedParty(Party party) {
        invitedParty = party;
    }

    public void setPlayer(Player player) {
        this.player = player.getUniqueId();
    }

    public Player getPartyLeader() {
        return partyLeader;
    }

    public void setPartyLeader(Player player) {
        partyLeader = player;
    }

    public UUID getPlayerUUID() {
        return player;
    }

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

    public void handleOffline(){
        new BukkitRunnable(){
            @Override
            public void run() {
                //Handle when player goes offline, decrement timeout after every 20 ticks delay
                if (Bukkit.getPlayer(player) == null) {
                    timeout--;
                    if (timeout == 0) {
                        //Handle pending invites
                        if (isInvited()) {
                            if (getInvitedParty().getLeader() != null && Hypixelify.getInstance().partyManager.parties.get(partyLeader) != null) {
                                Hypixelify.getInstance().partyManager.parties.get(partyLeader).removeInvitedMember(pInstance);
                            }
                            setInvitedParty(null);
                            setInvited(false);
                        }

                        //check if player is in party and remove him, if he's the leader, disband the party.
                        if (isInParty && partyLeader != null && Hypixelify.getInstance().partyManager.parties.get(partyLeader) != null) {

                            if (!partyLeader.getUniqueId().equals(player)) {
                                for (Player pl : Hypixelify.getInstance().partyManager.parties.get(partyLeader).getAllPlayers()) {
                                    if (!player.equals(pl.getUniqueId())) {
                                        for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-left")) {
                                            pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", name));
                                        }
                                    }
                                }
                                Hypixelify.getInstance().partyManager.parties.get(partyLeader).removeMember(pInstance);
                            } else if (Hypixelify.getInstance().partyManager.parties.get(partyLeader) != null && partyLeader.getUniqueId().equals(player)) {
                                Party party = Hypixelify.getInstance().partyManager.parties.get(partyLeader);
                                for (Player pl : party.getAllPlayers()) {
                                    if (pl != null && !pl.equals(partyLeader)) {
                                        if (pl.isOnline()) {
                                            ShopUtil.sendMessage(pl, Messages.message_disband_inactivity);
                                        }
                                        if (Hypixelify.getInstance().playerData.get(pl.getUniqueId()) != null) {
                                            Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setIsInParty(false);
                                            Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setPartyLeader(null);
                                        }
                                    }
                                }
                                Hypixelify.getInstance().partyManager.parties.get(partyLeader).disband();
                                Hypixelify.getInstance().partyManager.parties.remove(partyLeader);
                            }
                            setIsInParty(false);
                            setPartyLeader(null);
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

    public void updateDatabase() {
        //check if player is in party, and make sure that party size and offline players are 0. If so disband the party.
        if (isInParty && isPartyLeader() && Hypixelify.getInstance().partyManager.parties.get(partyLeader) != null &&
                Hypixelify.getInstance().partyManager.parties.get(partyLeader).shouldDisband()) {
            if (pInstance != null && pInstance.isOnline()) {
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

    public boolean toBeRemoved() {
        return clearData;
    }

    public boolean isInParty() {
        return isInParty;
    }

    public boolean isInvited() {
        return isInvited;
    }

    public void setInvited(boolean bool) {
        isInvited = bool;
        if (bool) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    expiredTime--;
                    if (expiredTime == 0 && isInvited) {
                        expiredTime = 60;
                        isInvited = false;
                        if (invitedParty != null && invitedParty.getLeader() != null && Hypixelify.getInstance().partyManager.parties.get(invitedParty.getLeader()) != null) {
                            Hypixelify.getInstance().partyManager.parties.get(invitedParty.getLeader()).removeInvitedMember(pInstance);
                            if (invitedParty.getLeader() != null && invitedParty.getLeader().isOnline())
                                ShopUtil.sendMessage(invitedParty.getLeader(), Messages.message_invite_expired);
                            if (pInstance != null && pInstance.isOnline())
                                ShopUtil.sendMessage(pInstance, Messages.message_invite_expired);
                        }
                        setInvitedParty(null);
                        this.cancel();
                    } else if(!isInvited){
                        expiredTime = 60;
                        this.cancel();
                    }
                }
            }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
        }
    }


}
