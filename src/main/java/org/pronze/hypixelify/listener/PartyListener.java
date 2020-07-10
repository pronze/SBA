package org.pronze.hypixelify.listener;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Objects;

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
            if(party == null || !BedwarsAPI.getInstance().isPlayerPlayingAnyGame(Bukkit.getPlayer(player.getPlayerUUID()))) return;

            game.selectPlayerRandomTeam(Bukkit.getPlayer(player.getPlayerUUID()));
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
    public void onPlayerLeave(PlayerQuitEvent e){
        PartiesAPI api = Parties.getApi();
        PartyPlayer player = api.getPartyPlayer(e.getPlayer().getUniqueId());

        if(player == null || player.getPartyName().isEmpty()) return;

        if(checkPartyLeader(player)) {
            for(PartyPlayer p : api.getParty(player.getPartyName()).getOnlineMembers(true)) {
                Objects.requireNonNull(Bukkit.getPlayer(p.getPlayerUUID())).sendMessage("§cParty has been disbanded");
            }
            api.getParty(player.getPartyName()).delete();
        }
        else if(player != null && !player.getPartyName().isEmpty() && api.getParty(player.getPartyName()).getOnlineMembers(true).size() <= 2){
            Objects.requireNonNull(Bukkit.getPlayer(Objects.requireNonNull(api.getParty(player.getPartyName()).getLeader()))).sendMessage("§cParty has been disbanded");
            api.getParty(player.getPartyName()).delete();
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
