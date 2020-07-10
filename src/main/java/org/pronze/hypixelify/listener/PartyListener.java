package org.pronze.hypixelify.listener;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import static org.pronze.hypixelify.utils.PartyUtil.checkPartyLeader;

public class PartyListener implements Listener {

    public PartyListener(){
        Bukkit.getPluginManager().registerEvents(this, Hypixelify.getInstance());
    }

    @EventHandler
    public void onBwPlayerJoin(BedwarsPlayerJoinEvent e){
        PartiesAPI api = Parties.getApi();
        PartyPlayer player = api.getPartyPlayer(e.getPlayer().getUniqueId());
        Game game = e.getGame();
        if(checkPartyLeader(player)) {
            Party party = api.getParty(player.getPartyName());
            if(party == null) return;

            for(PartyPlayer p : party.getOnlineMembers(true)){
                if(!p.getPlayerUUID().equals(player.getPlayerUUID())){
                    Main.getGame(game.getName()).joinToGame(Bukkit.getPlayer(p.getPlayerUUID()));
                    if(BedwarsAPI.getInstance().isPlayerPlayingAnyGame(e.getPlayer()) && game.isPlayerInAnyTeam(e.getPlayer()) && e.getGame().getTeamOfPlayer(e.getPlayer()).getMaxPlayers() > e.getGame().getTeamOfPlayer(e.getPlayer()).countConnectedPlayers())
                        e.getGame().selectPlayerTeam(Bukkit.getPlayer(p.getPlayerUUID()), game.getTeamOfPlayer(e.getPlayer()));
                }
            }
        }
    }

    @EventHandler
    public void onBwPlayerLeave(BedwarsPlayerLeaveEvent e){
        PartiesAPI api = Parties.getApi();
        PartyPlayer player = api.getPartyPlayer(e.getPlayer().getUniqueId());

        if(player == null || player.getPartyName().isEmpty()) return;
        if(!e.getPlayer().isOnline())
            return;
        if(checkPartyLeader(player)){
            Party party = api.getParty(player.getPartyName());
            if(party != null  && party.getMembers().size() <= 1) return;

            assert party != null;
            for (PartyPlayer p : party.getOnlineMembers(true)){
                if(!p.getPlayerUUID().equals(player.getPlayerUUID()) && Bukkit.getPlayer(p.getPlayerUUID()).isOnline() && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(Bukkit.getPlayer(p.getPlayerUUID()))){
                    Main.getGame(BedwarsAPI.getInstance().getGameOfPlayer(Bukkit.getPlayer(p.getPlayerUUID())).getName()).leaveFromGame(Bukkit.getPlayer(p.getPlayerUUID()));
                }
            }
        }
    }
}
