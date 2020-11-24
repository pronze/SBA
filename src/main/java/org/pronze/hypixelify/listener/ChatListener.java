package org.pronze.hypixelify.listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.wrapper.PlayerWrapper;
import org.pronze.hypixelify.api.party.Party;

public class ChatListener extends AbstractListener {

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        final Player player = e.getPlayer();
        final PlayerWrapper db = SBAHypixelify.getWrapperService().getWrapper(player);

        if(db == null || !db.isInParty() || !db.getPartyChatEnabled()) return;
        final Party party = SBAHypixelify.getPartyManager().getParty(db.getPartyLeader());
        if(party == null) return;
        if(party.getAllPlayers() == null) return;
        party.sendChat(player, e.getMessage());
        e.setCancelled(true);
    }
}
