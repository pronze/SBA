package pronze.hypixelify.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyChatEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

@Service
public class PartyListener implements Listener {

    public PartyListener() {
        SBAHypixelify.getInstance().registerListener(this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final var player = PlayerMapper
                .wrapPlayer(event.getPlayer())
                .as(PlayerWrapper.class);

        if (player.isPartyChatEnabled() && player.isInParty()) {
            SBAHypixelify
                    .getInstance()
                    .getPartyManager()
                    .getPartyOf(player)
                    .ifPresent(party -> {
                        final var chatEvent = new SBAPlayerPartyChatEvent(player, party);
                        chatEvent.setMessage(Component.text(event.getMessage()));

                        Runnable runnable = () -> {
                                SBAHypixelify
                                        .getPluginInstance()
                                        .getServer()
                                        .getPluginManager()
                                        .callEvent(chatEvent);
                                if (chatEvent.isCancelled()) {
                                    return;
                                }

                                party.sendMessage(
                                        AdventureHelper.toComponent(event.getMessage()),
                                        player
                                );
                        };

                        if (Bukkit.isPrimaryThread()) {
                            Bukkit.getScheduler().runTaskAsynchronously(SBAHypixelify.getPluginInstance(), runnable);
                        } else {
                            runnable.run();
                        }
                    });
        }
    }

}
