package org.pronze.hypixelify.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;
import org.pronze.hypixelify.party.Party;

public class ChatListener implements Listener {

    public ChatListener(){
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        PlayerDatabase db = Hypixelify.getInstance().playerData.get(player.getUniqueId());
        if(db == null || !db.isInParty() || !db.getPartyChatEnabled()) return;
        Party party = Hypixelify.getInstance().partyManager.parties.get(db.getPartyLeader());
        if(party == null) return;
        if(party.getAllPlayers() == null) return;
        party.sendChat(player, e.getMessage());
        e.setCancelled(true);
    }
}
