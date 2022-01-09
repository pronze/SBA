package io.github.pronze.sba.listener;

import io.github.pronze.sba.events.SBAPlayerPartyChatEvent;
import io.github.pronze.sba.wrapper.PlayerSetting;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;

@Service
public class PartyListener implements Listener {

    @OnPostEnable
    public void registerListener() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final var player = SBA.getInstance().getPlayerWrapper((event.getPlayer()));

        if (player.getSettings().isToggled(PlayerSetting.PARTY_CHAT_ENABLED) && player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            event.setCancelled(true);
            SBA.getInstance()
                    .getPartyManager()
                    .getPartyOf(player)
                    .ifPresent(party -> {
                        final var chatEvent = new SBAPlayerPartyChatEvent(player, party);
                        chatEvent.setMessage(Component.text(event.getMessage()));

                        new BukkitRunnable() {
                            public void run() {
                                SBA.getPluginInstance().getServer()
                                        .getPluginManager()
                                        .callEvent(chatEvent);
                            }
                        }.runTask(SBA.getPluginInstance());
                      
                        if (chatEvent.isCancelled()) {
                            return;
                        }

                        party.sendMessage(AdventureHelper.toComponent(event.getMessage()), player);
                    });
        }
    }

}
