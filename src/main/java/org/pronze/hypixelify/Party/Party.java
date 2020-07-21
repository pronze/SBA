package org.pronze.hypixelify.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.utils.ShopUtil;

import java.util.ArrayList;
import java.util.List;

public class Party {

    private int member_size = 0;
    private List<Player> players = new ArrayList<>();
    private List<Player> invitedMembers = new ArrayList<>();

    private Player leader;
    private boolean anyoneCanInvite = true;

    public Party(Player leader){
        setLeader(leader);
        addMember(leader);
    }

    public void setLeader(Player player){
        leader = player;
    }

    public Player getLeader(){
        return leader;
    }

    public void addInvitedMember(Player pl){
        if(!players.contains(pl))
            invitedMembers.add(pl);
    }

    public List<Player> getInvitedMembers(){
        return invitedMembers;
    }

    public void removeInvitedMember(Player pl){
        if(!players.contains(pl))
            invitedMembers.remove(pl);
    }

    public void addMember(Player player){
        if(!players.contains(player)) {
            players.add(player);
            member_size++;
        }
    }

    public int getSize(){
        return member_size;
    }

    public List<Player> getAllPlayers(){
        List<Player> newPlayerList = new ArrayList<>();
        for (Player player : players) {
            if(player == null || !player.isOnline())
                continue;

            newPlayerList.add(player);
        }

        return newPlayerList;
    }

    public List<Player> getPlayers(){
        if(leader == null)
            return null;
        List<Player> list = getAllPlayers();
        if(list == null)
            return null;
        list.remove(leader);
        return list;
    }


    public void removeMember(Player player){
        if(player.equals(leader)){
            for(Player pl : players){
                if(Bukkit.getPlayer(pl.getUniqueId()) != null && !pl.equals(leader)){
                    for(String st : Hypixelify.getConfigurator().config.getStringList("party.message.disband-inactivity")){
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

    public boolean canAnyoneInvite(){
        return anyoneCanInvite;
    }
}
