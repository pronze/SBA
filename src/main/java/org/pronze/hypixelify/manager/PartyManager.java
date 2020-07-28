package org.pronze.hypixelify.manager;

import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.party.Party;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.HashMap;
import java.util.UUID;

public class PartyManager {


    public HashMap<Player, Party> parties = new HashMap<>();


    public void disband(Player leader) {
        Party party = parties.get(leader);

        if (party == null || party.getLeader() == null || !party.getLeader().equals(leader))
            return;

        for (Player pl : party.getAllPlayers()) {
            if (pl != null) {
                if (pl.isOnline()) {
                    for (String str : Hypixelify.getConfigurator().config.getStringList("party.message.disband")) {
                        pl.sendMessage(ShopUtil.translateColors(str));
                    }
                }
                if (Hypixelify.getInstance().playerData.get(pl.getUniqueId()) != null) {
                    Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setIsInParty(false);
                    Hypixelify.getInstance().playerData.get(pl.getUniqueId()).setPartyLeader(null);
                }
            }
        }

        Hypixelify.getInstance().playerData.get(leader.getUniqueId()).setIsInParty(false);

        parties.get(leader).disband();
        parties.remove(leader);
    }

    public boolean isInParty(Player player) {
        if (Hypixelify.getInstance().playerData.get(player.getUniqueId()) != null)
            return Hypixelify.getInstance().playerData.get(player.getUniqueId()).isInParty();

        return false;
    }

    public void addToParty(Player player, Party party) {
        final HashMap<UUID, PlayerDatabase> Database = Hypixelify.getInstance().playerData;

        Player leader = party.getLeader();
        if (leader == null) return;

        if (party.getLeader() == null) return;
        if (parties.get(leader) == null) return;

        parties.get(leader).addMember(player);
        parties.get(leader).removeInvitedMember(player);


        Database.get(player.getUniqueId()).setPartyLeader(leader);
        Database.get(player.getUniqueId()).setInvited(false);
        Database.get(player.getUniqueId()).setIsInParty(true);
        Database.get(player.getUniqueId()).setInvitedParty(null);
        Database.get(player.getUniqueId()).setExpiredTimeTimeout(60);

        for (Player p : parties.get(leader).getAllPlayers()) {
            if (p == null) continue;
            if (!p.isOnline()) continue;
            for (String message : Hypixelify.getConfigurator().config.getStringList("party.message.accepted")) {
                p.sendMessage(ShopUtil.translateColors(message).replace("{player}", player.getDisplayName()));
            }
        }
    }

    public void removeFromParty(Player player, Party party) {
        PlayerDatabase db = Hypixelify.getInstance().playerData.get(player.getUniqueId());

        if (db == null || party == null || party.getLeader() == null)
            return;

        parties.get(db.getPartyLeader()).removeMember(player);

        for (Player pl : parties.get(db.getPartyLeader()).getAllPlayers()) {
            if (pl != null && pl.isOnline()) {
                for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.offline-quit")) {
                    pl.sendMessage(ShopUtil.translateColors(st).replace("{player}", player.getDisplayName()));
                }
            }
        }

        Hypixelify.getInstance().playerData.get(player.getUniqueId()).setIsInParty(false);
        Hypixelify.getInstance().playerData.get(player.getUniqueId()).setPartyLeader(null);
    }


    public void kickFromParty(Player player) {
        if (getParty(player) == null || player == null) return;
        PlayerDatabase db = Hypixelify.getInstance().playerData.get(player.getUniqueId());
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
        Hypixelify.getInstance().playerData.get(player.getUniqueId()).setIsInParty(false);
        Hypixelify.getInstance().playerData.get(player.getUniqueId()).setPartyLeader(null);
    }

    public Party getParty(Player player) {
        if (!isInParty(player)) return null;

        PlayerDatabase database = Hypixelify.getInstance().playerData.get(player.getUniqueId());
        if (database == null) return null;
        if (database.getPartyLeader() != null && isInParty(database.getPartyLeader())) {
            return parties.get(database.getPartyLeader());
        }

        return null;
    }

    public void warpPlayersToLeader(Player leader) {
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
}
