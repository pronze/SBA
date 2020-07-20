package org.pronze.hypixelify.Party;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Party {

    private int member_size;
    private List<Player> players;
    private Player leader;
    private boolean anyoneCanInvite;

    public Party(Player leader){
        setLeader(leader);
    }

    public void setLeader(Player player){
        leader = player;
    }

    public Player getLeader(){
        return leader;
    }

    public void addMember(Player player){
        if(!players.contains(player))
            players.add(player);
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
        players.remove(player);
    }

    public boolean canAnyoneInvite(){
        return anyoneCanInvite;
    }
}
