package io.pronze.hypixelify.listener;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.party.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import io.pronze.hypixelify.api.wrapper.PlayerWrapper;
import io.pronze.hypixelify.utils.ShopUtil;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import java.util.Objects;

public class PartyListener implements Listener {


    @EventHandler
    public void onBWJoin(BedwarsPlayerJoinedEvent e) {
        final var game = e.getGame();
        final var player = e.getPlayer();
        final var data = SBAHypixelify.getWrapperService().getWrapper(player);
        final var party = SBAHypixelify.getPartyManager().getParty(player);

        if (data == null || party == null) return;
        if (!data.isInParty() || data.getPartyLeader() == null) return;
        if (!data.isPartyLeader()) return;
        if (party.getPlayers() == null) return;

        if (game.getStatus() == GameStatus.WAITING
                && game.getConnectedPlayers().size() < game.getMaxPlayers()) {
                    party.getPlayers()
                            .stream()
                            .filter(Objects::nonNull)
                            .filter(Player::isOnline)
                            .forEach(pl-> {
                                if (BedwarsAPI.getInstance().isPlayerPlayingAnyGame(pl)) {
                                    BedwarsAPI.getInstance().getGameOfPlayer(pl).leaveFromGame(pl);
                                }
                                SBAHypixelify.getConfigurator()
                                        .getStringList("party.message.leader-join-leave")
                                        .forEach(string-> pl.sendMessage(ShopUtil.translateColors(string)));
                                game.joinToGame(pl);
                            });
                }

    }

    @EventHandler
    public void onBwPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        final var game = e.getGame();
        final var api = BedwarsAPI.getInstance();

        if (player == null) return;
        if (!player.isOnline()) return;
        final var data = SBAHypixelify.getWrapperService().getWrapper(player);
        final var party = SBAHypixelify.getPartyManager().getParty(player);
        if (data == null || party == null) return;
        if (!data.isInParty() || !data.isPartyLeader()) return;
        if(party.getPlayers() == null || party.getPlayers().isEmpty()) return;

        party.getPlayers()
                .stream()
                .filter(Objects::nonNull)
                .filter(api::isPlayerPlayingAnyGame)
                .forEach(pl-> {
                    final var plGame = api.getGameOfPlayer(pl);

                    if (plGame.equals(game)) return;

                    if (plGame.getStatus().equals(GameStatus.RUNNING)) {
                        plGame.leaveFromGame(pl);
                        SBAHypixelify.getConfigurator()
                                .getStringList("party.message.leader-join-leave")
                                .forEach(st-> pl.sendMessage(ShopUtil.translateColors(st)));
                    }
                });
    }

}
