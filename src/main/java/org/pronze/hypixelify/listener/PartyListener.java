package org.pronze.hypixelify.listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;
import org.pronze.hypixelify.party.Party;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

public class PartyListener extends AbstractListener{


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBWJoin(BedwarsPlayerJoinedEvent e) {
        Game game = e.getGame();
        Player player = e.getPlayer();
        PlayerDatabase data = Hypixelify.getInstance().playerData.get(player.getUniqueId());
        Party party = Hypixelify.getInstance().partyManager.getParty(player);
        if (data == null || party == null) return;
        if (!data.isInParty() || data.getPartyLeader() == null) return;
        if (!data.isPartyLeader()) return;
        if (party.getPlayers() == null) return;
        if (game.getStatus().equals(GameStatus.WAITING) && game.getConnectedPlayers().size() < game.getMaxPlayers()) {
                    for (Player pl : party.getPlayers()) {
                        if (pl == null) return;
                        if (!pl.isOnline()) return;
                        if (BedwarsAPI.getInstance().isPlayerPlayingAnyGame(pl)) {
                            BedwarsAPI.getInstance().getGameOfPlayer(pl).leaveFromGame(pl);
                        }
                        for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.leader-join-leave")) {
                            pl.sendMessage(ShopUtil.translateColors(st));
                        }
                        game.joinToGame(pl);
                    }
                }

    }

    @EventHandler
    public void onBwPlayerLeave(BedwarsPlayerLeaveEvent e) {
        Player player = e.getPlayer();
        Game game = e.getGame();
        if (player == null) return;
        if (!player.isOnline()) return;
        PlayerDatabase data = Hypixelify.getInstance().playerData.get(player.getUniqueId());
        Party party = Hypixelify.getInstance().partyManager.getParty(player);
        if (data == null || party == null) return;
        if (!data.isInParty() || !data.isPartyLeader()) return;

        for (Player pl : party.getPlayers()) {
            if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(pl)) return;

            if (BedwarsAPI.getInstance().getGameOfPlayer(pl).equals(game)) return;

            if (BedwarsAPI.getInstance().getGameOfPlayer(pl).getStatus().equals(GameStatus.RUNNING)) {
                BedwarsAPI.getInstance().getGameOfPlayer(pl).leaveFromGame(pl);
                for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.leader-join-leave")) {
                    pl.sendMessage(ShopUtil.translateColors(st));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
