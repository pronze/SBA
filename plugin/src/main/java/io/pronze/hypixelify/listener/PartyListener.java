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

public class PartyListener implements Listener {


    @EventHandler
    public void onBWJoin(BedwarsPlayerJoinedEvent e) {
        final Game game = e.getGame();
        final Player player = e.getPlayer();
        final PlayerWrapper data = SBAHypixelify.getWrapperService().getWrapper(player);
        final Party party = SBAHypixelify.getPartyManager().getParty(player);

        if (data == null || party == null) return;
        if (!data.isInParty() || data.getPartyLeader() == null) return;
        if (!data.isPartyLeader()) return;
        if (party.getPlayers() == null) return;

        if (game.getStatus() == GameStatus.WAITING && game.getConnectedPlayers().size() < game.getMaxPlayers()) {
                    for (Player pl : party.getPlayers()) {
                        if (pl == null) return;
                        if (!pl.isOnline()) return;
                        if (BedwarsAPI.getInstance().isPlayerPlayingAnyGame(pl)) {
                            BedwarsAPI.getInstance().getGameOfPlayer(pl).leaveFromGame(pl);
                        }
                        for (String st : SBAHypixelify.getConfigurator().config.getStringList("party.message.leader-join-leave")) {
                            pl.sendMessage(ShopUtil.translateColors(st));
                        }
                        game.joinToGame(pl);
                    }
                }

    }

    @EventHandler
    public void onBwPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final Player player = e.getPlayer();
        final Game game = e.getGame();
        final BedwarsAPI API = BedwarsAPI.getInstance();

        if (player == null) return;
        if (!player.isOnline()) return;
        final PlayerWrapper data = SBAHypixelify.getWrapperService().getWrapper(player);
        final Party party = SBAHypixelify.getPartyManager().getParty(player);
        if (data == null || party == null) return;
        if (!data.isInParty() || !data.isPartyLeader()) return;
        if(party.getPlayers() == null || party.getPlayers().isEmpty()) return;

        for (Player pl : party.getPlayers()) {
            if (!API.isPlayerPlayingAnyGame(pl)) return;

            if (API.getGameOfPlayer(pl).equals(game)) return;

            if (API.getGameOfPlayer(pl).getStatus().equals(GameStatus.RUNNING)) {
                API.getGameOfPlayer(pl).leaveFromGame(pl);
                for (String st : SBAHypixelify.getConfigurator().config.getStringList("party.message.leader-join-leave")) {
                    pl.sendMessage(ShopUtil.translateColors(st));
                }
            }
        }
    }

}
