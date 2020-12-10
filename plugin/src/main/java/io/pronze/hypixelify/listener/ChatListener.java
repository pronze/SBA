package io.pronze.hypixelify.listener;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.party.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import io.pronze.hypixelify.api.wrapper.PlayerWrapper;

public class ChatListener implements Listener {

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
