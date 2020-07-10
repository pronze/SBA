package org.pronze.hypixelify.event;

import com.alessiodp.parties.api.interfaces.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PartyLeaderGameLeaveEvent extends Event implements Cancellable {

    private boolean isCancelled = false;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private Player PartyLeader;
    private Party party;

    public PartyLeaderGameLeaveEvent(UUID uuid, Party party){
        PartyLeader = Bukkit.getPlayer(uuid);
        this.party = party;
    }
    public Party getParty(){
        return party;
    }


    public Player getPartyLeader(){
        return  PartyLeader;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
