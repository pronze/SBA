package org.pronze.hypixelify.manager;

import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.database.PlayerDatabase;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.party.Party;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PartyManager implements org.pronze.hypixelify.api.party.PartyManager{


    public HashMap<Player, Party> parties = new HashMap<>();

    @Override
    public List<org.pronze.hypixelify.api.party.Party> getParties(){
        return new ArrayList<>(parties.values());
    }

    @Override
    public void disband(Player leader) {
        Party party = parties.get(leader);

        if (party == null || party.getLeader() == null || !party.getLeader().equals(leader))
            return;


        if(party.getPlayers() != null && !party.getPlayers().isEmpty()) {
            for (Player pl : party.getAllPlayers()) {
                if (pl != null) {
                    if (pl.isOnline()) {
                        for (String str : Hypixelify.getConfigurator().config.getStringList("party.message.disband")) {
                            pl.sendMessage(ShopUtil.translateColors(str));
                        }
                    }
                    final PlayerDatabase plDatabase = Hypixelify.getDatabaseManager().getDatabase(pl);
                    if (plDatabase != null) {
                        plDatabase.setIsInParty(false);
                        plDatabase.setPartyLeader(null);
                    }
                }
            }
        }

        Hypixelify.getDatabaseManager().getDatabase(leader).setIsInParty(false);

        parties.get(leader).disband();
        parties.remove(leader);
        Hypixelify.getDatabaseManager().updateAll();
    }

    @Override
    public boolean isInParty(Player player) {
        if (Hypixelify.getDatabaseManager().getDatabase(player) != null)
            return Hypixelify.getDatabaseManager().getDatabase(player).isInParty();

        return false;
    }



    @Override
    public void addToParty(Player player, org.pronze.hypixelify.api.party.Party party) {

        Player leader = party.getLeader();
        if (leader == null) return;

        if (party.getLeader() == null) return;
        if (parties.get(leader) == null) return;

        parties.get(leader).addMember(player);
        parties.get(leader).removeInvitedMember(player);
        final PlayerDatabase playerDatabase = Hypixelify.getDatabaseManager().getDatabase(player);
        playerDatabase.setPartyLeader(leader);
        playerDatabase.setInvited(false);
        playerDatabase.setIsInParty(true);
        playerDatabase.setInvitedParty(null);
        playerDatabase.setExpiredTimeTimeout(60);

        for (Player p : parties.get(leader).getAllPlayers()) {
            if (p == null) continue;
            if (!p.isOnline()) continue;
            for (String message : Hypixelify.getConfigurator().config.getStringList("party.message.accepted")) {
                p.sendMessage(ShopUtil.translateColors(message).replace("{player}", player.getDisplayName()));
            }
        }

    }

    @Override
    public void removeFromParty(Player player, org.pronze.hypixelify.api.party.Party party) {
        final PlayerDatabase db = Hypixelify.getDatabaseManager().getDatabase(player);

        if (db == null || party == null || party.getLeader() == null)
            return;

        parties.get(db.getPartyLeader()).removeMember(player);

        if(parties.get(db.getPartyLeader()).getPlayers() != null && !parties.get(db.getPartyLeader()).getPlayers().isEmpty()) {
            for (Player pl : parties.get(db.getPartyLeader()).getAllPlayers()) {
                if (pl != null && pl.isOnline()) {
                    for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-quit")) {
                        pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", player.getDisplayName()));
                    }
                }
            }
        }
        db.setIsInParty(false);
        db.setPartyLeader(null);
        Hypixelify.getDatabaseManager().updateAll();

    }

    @Override
    public void kickFromParty(Player player) {
        if (getParty(player) == null || player == null) return;
        final PlayerDatabase db = Hypixelify.getDatabaseManager().getDatabase(player);
        if (db == null || db.getPartyLeader() == null) return;
        Player leader = db.getPartyLeader();
        if (leader == null || parties.get(leader) == null) return;
        parties.get(db.getPartyLeader()).removeMember(player);

        if (player.isOnline()) {
            for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.got-kicked")) {
                player.sendMessage(ShopUtil.translateColors(st));
            }
        }
        if (parties.get(leader).getPlayers() != null) {
            for (Player pl : parties.get(leader).getAllPlayers()) {
                if (pl != null && pl.isOnline()) {
                    for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.kicked")) {
                        pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", player.getDisplayName()));
                    }
                }
            }
        }
        db.setIsInParty(false);
        db.setPartyLeader(null);
        Hypixelify.getDatabaseManager().updateAll();

    }

    @Override
    public Party getParty(Player player) {
        if (!isInParty(player)) return null;

        final PlayerDatabase database = Hypixelify.getDatabaseManager().getDatabase(player);
        if (database == null) return null;
        if (database.getPartyLeader() != null && isInParty(database.getPartyLeader())) {
            return parties.get(database.getPartyLeader());
        }

        return null;
    }



    @Override
    public void warpPlayersToLeader(Player leader) {
        if(getParty(leader).getPlayers() == null || getParty(leader).getPlayers().isEmpty()) return;

        if (BedwarsAPI.getInstance().isPlayerPlayingAnyGame(leader)) {
            Game game = BedwarsAPI.getInstance().getGameOfPlayer(leader);
            ShopUtil.sendMessage(leader, Messages.message_warping);
            for (Player pl : getParty(leader).getPlayers()) {
                if (pl != null && pl.isOnline()) {
                    if (game.getConnectedPlayers().size() >= game.getMaxPlayers()) {
                        pl.sendMessage("§cYou could not be warped to game");
                        continue;
                    }

                    ShopUtil.sendMessage(pl, Messages.message_warped);
                    if (BedwarsAPI.getInstance().isPlayerPlayingAnyGame(pl)) {
                        if (BedwarsAPI.getInstance().getGameOfPlayer(pl).equals(game))
                            continue;

                        Game g = BedwarsAPI.getInstance().getGameOfPlayer(pl);
                        g.leaveFromGame(pl);
                    }

                    game.joinToGame(pl);
                }
            }
        } else {
            ShopUtil.sendMessage(leader, Messages.message_warping);
            for (Player pl : getParty(leader).getPlayers()) {
                if (pl != null && pl.isOnline() && leader.isOnline()) {
                    pl.teleport(leader.getLocation());
                    ShopUtil.sendMessage(pl, Messages.message_warped);
                }
            }
        }
    }

    @Override
    public void removeFromInvitedParty(Player player) {
        PlayerDatabase database = Hypixelify.getDatabaseManager().getDatabase(player);
        if(database == null || !database.isInvited()) return;
        org.pronze.hypixelify.api.party.Party invitedParty = database.getInvitedParty();
        if(invitedParty == null || invitedParty.getLeader() == null) return;

        invitedParty.removeInvitedMember(player);
        database.setInvitedParty(null);
        database.setInvited(false);
    }

    @Override
    public Party createParty(Player player){
        if(parties.containsKey(player)) return null;
        Party party = new Party(player);
        parties.put(player, party);
        return party;
    }

    @Override
    public void removeParty(Player leader) {
        parties.remove(leader);
    }

    @Override
    public void databaseDeletionFromParty(Player player, Player partyLeader){
        final org.pronze.hypixelify.api.party.Party party = getParty(partyLeader);
        final PlayerDatabase database = Hypixelify.getDatabaseManager().getDatabase(player);

        if (party != null) {
            if (!partyLeader.getUniqueId().equals(player.getUniqueId())) {
                if (party.getAllPlayers() != null && !party.getAllPlayers().isEmpty()) {
                    for (Player pl : party.getAllPlayers()) {
                        if (pl != null && pl.isOnline() && !player.getUniqueId().equals(pl.getUniqueId())) {
                            for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-left")) {
                                pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", player.getName()));
                            }
                        }
                    }
                }
                party.removeMember(player);
            } else if (partyLeader.getUniqueId().equals(player.getUniqueId())) {
                for (Player pl : party.getAllPlayers()) {
                    if (pl != null && !pl.equals(partyLeader)) {
                        if (pl.isOnline()) {
                            ShopUtil.sendMessage(pl, Messages.message_disband_inactivity);
                        }
                        org.pronze.hypixelify.api.database.PlayerDatabase plDatabase = Hypixelify.getDatabaseManager().getDatabase(pl);
                        if (plDatabase != null) {
                            plDatabase.setIsInParty(false);
                            plDatabase.setPartyLeader(null);
                        }
                    }
                }
                party.disband();
                removeParty(partyLeader);
            }
            database.setIsInParty(false);
            database.setPartyLeader(null);
        }
    }

}
