package org.pronze.hypixelify.event;

import com.alessiodp.parties.api.interfaces.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.UUID;

public class PartyLeaderGameJoinEvent extends Event implements Cancellable {

    private boolean isCancelled = false;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private Player PartyLeader;
    private Game game;
    private Party party;

    public PartyLeaderGameJoinEvent(UUID uuid, Game game, Party party){
        PartyLeader = Bukkit.getPlayer(uuid);
        this.game = game;
        this.party = party;
    }

    public Party getParty(){
        return party;
    }

    public Game getGame(){
        return game;
    }

    public Player getPartyLeader(){
        return  PartyLeader;
    }
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

}
