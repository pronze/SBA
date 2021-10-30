package io.github.pronze.sba.listener;

import io.github.pronze.sba.events.SBAPlayerPartyChatEvent;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.party.PartyManagerImpl;
import io.github.pronze.sba.wrapper.PlayerSetting;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
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
        final var player = PlayerMapper
                .wrapPlayer(event.getPlayer())
                .as(SBAPlayerWrapper.class);

        if (player.getSettings().isToggled(PlayerSetting.PARTY_CHAT_ENABLED) && player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            event.setCancelled(true);
            SBA.getInstance()
                    .getPartyManager()
                    .getPartyOf(player)
                    .ifPresent(party -> {
                        final var chatEvent = new SBAPlayerPartyChatEvent(player, party);
                        chatEvent.setMessage(Component.text(event.getMessage()));
                        SBA.getPluginInstance()
                                .getServer()
                                .getPluginManager()
                                .callEvent(chatEvent);
                        if (chatEvent.isCancelled()) {
                            return;
                        }

                        party.sendMessage(AdventureHelper.toComponent(event.getMessage()), player);
                    });
        }
    }

    @EventHandler
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent event) {
        final var player = event.getPlayer();
        final var game = event.getGame();
        final var wrappedPlayer = PlayerMapper
                .wrapPlayer(player)
                .as(SBAPlayerWrapper.class);

        final var maybeParty = PartyManagerImpl
                .getInstance()
                .getPartyOf(wrappedPlayer);

        if (maybeParty.isEmpty()) {
            return;
        }

        final var party = maybeParty.get();

        if (!wrappedPlayer.equals(party.getPartyLeader())) {
            Message.of(LangKeys.PARTY_MESSAGE_ACCESS_DENIED)
                    .send(wrappedPlayer);
            wrappedPlayer.leaveFromGame();
            return;
        }

        Message.of(LangKeys.PARTY_MESSAGE_WARP)
                .send(wrappedPlayer);

        if (Main.getInstance().isPlayerPlayingAnyGame(player)) {
            party.getMembers()
                    .stream()
                    .filter(member -> !wrappedPlayer.equals(member))
                    .forEach(member -> {
                        final var memberGame = Main.getInstance().getGameOfPlayer(member.getInstance());

                        // already in game no need of warping
                        if (game == memberGame) {
                            return;
                        }

                        if (memberGame != null) {
                            memberGame.leaveFromGame(member.getInstance());
                        }

                        game.joinToGame(member.getInstance());
                        Message.of(LangKeys.PARTY_MESSAGE_WARP)
                                .send(member);
                    });
        } else {
            final var leaderLocation = wrappedPlayer.getInstance().getLocation();
            party.getMembers()
                    .stream()
                    .filter(member -> !wrappedPlayer.equals(member))
                    .map(SBAPlayerWrapper::asBukkitPlayer)
                    .forEach(member -> {
                        if (Main.getInstance().isPlayerPlayingAnyGame(member)) {
                            Main.getInstance().getGameOfPlayer(member).leaveFromGame(member);
                        }
                        PlayerUtils.teleportPlayer(member, leaderLocation);
                        Message.of(LangKeys.PARTY_MESSAGE_LEADER_JOIN_LEAVE)
                                .send(PlayerMapper.wrapPlayer(member));
                    });
        }

    }

}
