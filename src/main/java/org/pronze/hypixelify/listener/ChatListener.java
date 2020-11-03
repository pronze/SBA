package org.pronze.hypixelify.listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.database.PlayerDatabase;
import org.pronze.hypixelify.api.party.Party;

public class ChatListener extends AbstractListener {

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        PlayerDatabase db = SBAHypixelify.getDatabaseManager().getDatabase(player);

        if(db == null || !db.isInParty() || !db.getPartyChatEnabled()) return;
        Party party = SBAHypixelify.getPartyManager().getParty(db.getPartyLeader());
        if(party == null) return;
        if(party.getAllPlayers() == null) return;
        party.sendChat(player, e.getMessage());
        e.setCancelled(true);
    }
}
