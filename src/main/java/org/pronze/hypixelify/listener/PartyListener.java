package org.pronze.hypixelify.listener;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.event.PartyLeaderGameJoinEvent;
import org.pronze.hypixelify.event.PartyLeaderGameLeaveEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import static org.pronze.hypixelify.utils.PartyUtil.checkPartyLeader;

public class PartyListener implements Listener {

    public PartyListener(){
        Bukkit.getPluginManager().registerEvents(this, Hypixelify.getInstance());
    }

    @EventHandler
    public void onBwPlayerJoin(BedwarsPlayerJoinEvent e){
        PartiesAPI api = Parties.getApi();
        PartyPlayer player = api.getPartyPlayer(e.getPlayer().getUniqueId());
        if(checkPartyLeader(player)) {
            PartyLeaderGameJoinEvent event = new PartyLeaderGameJoinEvent(player.getPlayerUUID(), e.getGame(), api.getParty(player.getPartyName()));
            Bukkit.getPluginManager().callEvent(event);
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
            PartyLeaderGameLeaveEvent event = new PartyLeaderGameLeaveEvent(player.getPlayerUUID(), api.getParty(player.getPartyName()));
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @EventHandler
    public void PartyLeaderLeave(PartyLeaderGameLeaveEvent e){
        Player partyleader = e.getPartyLeader();
        Party party = e.getParty();

        if(party == null) return;
        if(e.isCancelled()) return;

        if(party.getMembers().size() <= 1) return;

        for (PartyPlayer p : party.getOnlineMembers(true)){
            if(!p.getPlayerUUID().equals(partyleader.getUniqueId()) && Bukkit.getPlayer(p.getPlayerUUID()).isOnline() && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(Bukkit.getPlayer(p.getPlayerUUID()))){
                Main.getGame(BedwarsAPI.getInstance().getGameOfPlayer(Bukkit.getPlayer(p.getPlayerUUID())).getName()).leaveFromGame(Bukkit.getPlayer(p.getPlayerUUID()));
            }
        }

    }

    @EventHandler
    public void PartyLeaderJoin(PartyLeaderGameJoinEvent e){
        Player partyleader = e.getPartyLeader();
        Game game = e.getGame();
        Party party = e.getParty();

        if(e.isCancelled())
            return;

        if(game.getStatus() != GameStatus.RUNNING || !partyleader.isOnline())
            return;

        for(PartyPlayer p : party.getOnlineMembers(true)){
            if(!p.getPlayerUUID().equals(partyleader.getUniqueId())){
                Main.getGame(game.getName()).joinToGame(Bukkit.getPlayer(p.getPlayerUUID()));
                if(BedwarsAPI.getInstance().isPlayerPlayingAnyGame(partyleader) && game.isPlayerInAnyTeam(partyleader) && e.getGame().getTeamOfPlayer(partyleader).getMaxPlayers() > e.getGame().getTeamOfPlayer(partyleader).countConnectedPlayers())
                    e.getGame().selectPlayerTeam(Bukkit.getPlayer(p.getPlayerUUID()), game.getTeamOfPlayer(partyleader));
            }
        }
    }
}
