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
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinEvent;
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
    public void PartyLeaderJoin(PartyLeaderGameJoinEvent e){
        Player partyleader = e.getPartyLeader();
        Game game = e.getGame();
        Party party = e.getParty();
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
