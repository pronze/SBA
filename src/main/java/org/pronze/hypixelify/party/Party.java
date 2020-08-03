package org.pronze.hypixelify.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.utils.ShopUtil;

import java.util.ArrayList;
import java.util.List;

public class Party implements org.pronze.hypixelify.api.party.Party {

    private int member_size = 0;
    private List<Player> players = new ArrayList<>();
    private List<Player> invitedMembers = new ArrayList<>();

    private Player leader;
    private final boolean anyoneCanInvite = true;

    public Party(Player leader) {
        setLeader(leader);
        if(players.contains(leader)) return;
        players.add(leader);
    }

    public void sendMessage(String msg) {
        for(Player p : getAllPlayers())   {
            p.sendMessage(msg);
        }
    }

    @Override
    public void sendChat(Player sender, String msg) {
        msg = ChatColor.GREEN +"Party>> " + sender.getName() + ChatColor.WHITE + " : " + ChatColor.ITALIC + msg;
        sendMessage(msg);
    }

    public List<Player> getOfflinePlayers() {
        List<Player> offlinePlayers = new ArrayList<>();

        for (Player player : players) {
            if (player != null) {
                if (Bukkit.getPlayer(player.getUniqueId()) == null) {
                    offlinePlayers.add(player);
                }
            }
        }

        if (offlinePlayers.isEmpty()) return null;

        return offlinePlayers;
    }

    @Override
    public boolean shouldDisband(){
        return getPlayers() == null && getInvitedMembers() == null && getOfflinePlayers() == null;
    }

    @Override
    public void disband() {
        players.clear();
        invitedMembers.clear();
        invitedMembers = null;
        players = null;
        setLeader(null);
    }

    public void setLeader(Player player) {
        leader = player;
    }

   public void addInvitedMember(Player pl) {
       if (!invitedMembers.contains(pl)) {
           invitedMembers.add(pl);
           Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setInvited(true);
           Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setInvitedParty(this);
       }
   }

    @Override
    public List<Player> getInvitedMembers() {
        if(invitedMembers == null || invitedMembers.isEmpty()) return null;

        return invitedMembers;
    }

    @Override
    public void removeInvitedMember(Player pl) {
        invitedMembers.remove(pl);
    }

    @Override
   public void addMember(Player player) {
       if (!players.contains(player)) {
           players.add(player);
           member_size++;
       }
   }

    @Override
    public int getSize() {
        return member_size;
    }

    @Override
    public List<Player> getPlayers() {
        if (leader == null)
            return null;
        List<Player> list = getAllPlayers();
        if (list == null)
            return null;
        list.remove(leader);
        if(list.isEmpty())
            return null;
        return list;
    }

    @Override
    public void removeMember(Player player) {
        if (player.equals(leader)) {
            for (Player pl : players) {
                if (Bukkit.getPlayer(pl.getUniqueId()) != null && !pl.equals(leader)) {
                    for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.disband-inactivity")) {
                        pl.sendMessage(ShopUtil.translateColors(st));
                    }
                }
            }
            players.clear();
            leader = null;
            return;
        }
        players.remove(player);
    }

    @Override
    public int getCompleteSize(){
        if( players == null || invitedMembers == null) return 0;


        return players.size() + invitedMembers.size();
    }

    @Override
    public boolean canAnyoneInvite() {
        return anyoneCanInvite;
    }

    @Override
    public Player getLeader(){
        return leader;
    }

    @Override
    public List<Player> getAllPlayers(){
        List<Player> newPlayerList = new ArrayList<>();
        for (Player player : players) {
            if (player == null || !player.isOnline())
                continue;

            newPlayerList.add(player);
        }
        if(newPlayerList.isEmpty())
            return null;
        return newPlayerList;
    }
}
