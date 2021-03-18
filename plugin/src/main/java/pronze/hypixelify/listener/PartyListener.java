package pronze.hypixelify.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyChatEvent;
import pronze.hypixelify.game.PlayerWrapperImpl;

public class PartyListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final var player = PlayerMapper
                .wrapPlayer(event.getPlayer())
                .as(PlayerWrapperImpl.class);

        if (player.isPartyChatEnabled() && player.isInParty()) {
            SBAHypixelify
                    .getInstance()
                    .getPartyManager()
                    .getPartyOf(player)
                    .ifPresent(party -> {
                        final var chatEvent = new SBAPlayerPartyChatEvent(player, party);
                        chatEvent.setMessage(Component.text(event.getMessage()));

                        if (Bukkit.isPrimaryThread()) {
                            Bukkit.getScheduler()
                                    .runTaskAsynchronously(SBAHypixelify.getInstance(),
                                            () -> {
                                                SBAHypixelify
                                                        .getInstance()
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

                                            });
                            return;
                        }
                        SBAHypixelify
                                .getInstance()
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
                    });
        }
    }

}
