package pronze.hypixelify.listener;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        final var player = e.getPlayer();
        final var db = SBAHypixelify.getWrapperService().getWrapper(player);

        if(db == null || !db.isInParty() || !db.getPartyChatEnabled()) return;
        final var party = SBAHypixelify.getPartyManager().getParty(db.getPartyLeader());
        if(party == null) return;
        if(party.getAllPlayers() == null) return;
        party.sendChat(player, e.getMessage());
        e.setCancelled(true);
    }
}
