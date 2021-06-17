package pronze.sba.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.sba.SBA;
import pronze.sba.events.SBAPlayerPartyChatEvent;
import pronze.sba.wrapper.PlayerWrapper;

@Service
public class PartyListener implements Listener {

    @OnPostEnable
    public void registerListener() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final var player = PlayerMapper
                .wrapPlayer(event.getPlayer())
                .as(PlayerWrapper.class);

        if (player.isPartyChatEnabled() && player.isInParty()) {
            SBA
                    .getInstance()
                    .getPartyManager()
                    .getPartyOf(player)
                    .ifPresent(party -> {
                        final var chatEvent = new SBAPlayerPartyChatEvent(player, party);
                        chatEvent.setMessage(Component.text(event.getMessage()));

                        Runnable runnable = () -> {
                                SBA
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
                            Bukkit.getScheduler().runTaskAsynchronously(SBA.getPluginInstance(), runnable);
                        } else {
                            runnable.run();
                        }
                    });
        }
    }

}
