package org.pronze.hypixelify.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.party.PartyManager;
import org.pronze.hypixelify.api.database.PlayerDatabase;
import org.pronze.hypixelify.manager.DatabaseManager;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.api.party.Party;
import org.pronze.hypixelify.utils.ShopUtil;

import java.util.Arrays;
import java.util.List;

public class PartyCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can access this command");
            return true;
        }

        if (!Hypixelify.getConfigurator().config.getBoolean("party.enabled", true)) {
            sender.sendMessage("Cannot access command, party system is disabled.");
        }

        Player player = (Player) sender;

        if (args == null || args.length == 0 || args.length > 2) {
            ShopUtil.sendMessage(player, Messages.message_invalid_command);
            return true;
        }

        final DatabaseManager databaseManager = Hypixelify.getDatabaseManager();
        final PlayerDatabase playerDatabase = databaseManager.getDatabase(player);
        final PartyManager partyManager = Hypixelify.getPartyManager();

        if (args[0].equalsIgnoreCase("help")) {
            if (args.length != 1) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }
            ShopUtil.sendMessage(player, Messages.message_party_help);
            return true;
        }

        else if(playerDatabase.isInParty() && playerDatabase.getPartyLeader() != null
                && !playerDatabase.getPartyLeader().isOnline()){
                player.sendMessage(ChatColor.RED + "Please wait until the leader comes back online..., or party disbands");
                return true;
        }

        else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length != 2) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            //Party size limit
            if (playerDatabase != null) {
                int max_sz = Hypixelify.getConfigurator().config.getInt("party.size", 4);
                if (playerDatabase.isInParty() && partyManager.getParty(playerDatabase.getPartyLeader()) != null && partyManager.getParty(playerDatabase.getPartyLeader()).getCompleteSize() >= max_sz) {
                    player.sendMessage("§cParty has reached maximum Size.");
                    return true;
                }
            }

            //check if player argument is online
            Player invited = Bukkit.getPlayerExact(args[1].toLowerCase());
            if (invited == null) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return true;
            }


            if (invited.getUniqueId().equals(player.getUniqueId())) {
                ShopUtil.sendMessage(player, Messages.message_cannot_invite_yourself);
                return true;
            }

            if (playerDatabase == null)
                Hypixelify.getDatabaseManager().createDatabase(player);

            PlayerDatabase invitedData = databaseManager.getDatabase(invited);
            if (invitedData == null)
                databaseManager.createDatabase(invited);

            if (databaseManager.getDatabase(player).isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_decline_inc);
                return true;
            }

            Party party;
            if (!playerDatabase.isInParty()) {
                party = partyManager.getParty(player);
                if (party == null) {
                    party = partyManager.createParty(player);
                    playerDatabase.setIsInParty(true);
                    playerDatabase.setPartyLeader(player);
                }
            } else {
                party = partyManager.getParty(player);
            }

            if (invitedData.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_cannot_invite);
                return true;
            }

            if (invitedData.isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_already_invited);
                return true;
            }

            if (party.canAnyoneInvite() || player.equals(party.getLeader())) {
                partyManager.getParty(party.getLeader()).addInvitedMember(invited);
                for (String message : Hypixelify.getConfigurator().config.getStringList("party.message.invite")) {
                    invited.sendMessage(ShopUtil.translateColors(message).replace("{player}", player.getDisplayName()));
                }
                for (String message : Hypixelify.getConfigurator().config.getStringList("party.message.invited")) {
                    player.sendMessage(ShopUtil.translateColors(message).replace("{player}", invited.getDisplayName()));
                }
                return true;
            }

        }

        //check if player does not do other commands on his newly created party.
        else if (playerDatabase != null && playerDatabase.isInParty() &&
                partyManager.getParty(playerDatabase.getPartyLeader()) != null &&
                partyManager.getParty(player).getPlayers() == null) {
            ShopUtil.sendMessage(player, Messages.message_no_other_commands);
            return true;
        }

        else if (args[0].equalsIgnoreCase("accept")) {

            if (playerDatabase == null) {
                player.sendMessage("§cAn error has occurred..");
                return true;
            }

            if (!playerDatabase.isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_not_invited);
                return true;
            }

            if (playerDatabase.isInParty() || playerDatabase.getInvitedParty() == null) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            Party pParty = playerDatabase.getInvitedParty();
            partyManager.addToParty(player, pParty);
            return true;

        }

        else if (args[0].equalsIgnoreCase("leave")) {

            if (args.length != 1) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            if(playerDatabase == null) return true;

            if (!playerDatabase.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }
            if (playerDatabase.isPartyLeader()) {
                player.sendMessage("§cYou have to disband the party first!");
                return true;
            }

            if (playerDatabase.getPartyLeader() == null || partyManager.getParty(playerDatabase.getPartyLeader()) == null)
                return true;

            Party party = partyManager.getParty(playerDatabase.getPartyLeader());
            if (party == null) return true;
            partyManager.removeFromParty(player, party);
            ShopUtil.sendMessage(player, Messages.message_party_left);
        }

        else if (args[0].equalsIgnoreCase("decline")) {

            if (playerDatabase == null) return true;

            if (!playerDatabase.isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_not_invited);
                return true;
            }

            Party invitedParty = playerDatabase.getInvitedParty();

            if (invitedParty == null || invitedParty.getLeader() == null) return true;

            partyManager.getParty(invitedParty.getLeader()).removeInvitedMember(player);

            ShopUtil.sendMessage(player, Messages.message_decline_user);

            for (Player pl : invitedParty.getAllPlayers()) {
                if (pl != null && pl.isOnline()) {
                    for(String st : Messages.message_declined){
                        pl.sendMessage(ShopUtil.translateColors(st.replace("{player}", player.getDisplayName())));
                    }
                }
            }
            playerDatabase.setInvited(false);
            playerDatabase.setExpiredTimeTimeout(60);
            playerDatabase.setInvitedParty(null);
            return true;

        }

        else if (args[0].equalsIgnoreCase("list")) {

            if (playerDatabase == null) return true;
            if (!playerDatabase.isInParty()){
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            Player leader = playerDatabase.getPartyLeader();
            if(leader == null) return true;
            player.sendMessage("Players: ");
            for (Player pl : partyManager.getParty(leader).getAllPlayers()) {
                player.sendMessage(pl.getDisplayName());
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("disband")) {

            if (playerDatabase == null || partyManager.getParty(playerDatabase.getPartyLeader()) == null) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (!playerDatabase.getPartyLeader().equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return true;
            }
            partyManager.disband(player);
            return true;
        }

        else if (args[0].equalsIgnoreCase("kick")) {
            if (!playerDatabase.isInParty() || playerDatabase.getPartyLeader() == null) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (!playerDatabase.getPartyLeader().equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return true;
            }
            if (args.length != 2) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            Player invited = Bukkit.getPlayerExact(args[1].toLowerCase());
            if (invited == null) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return true;
            }

            if (invited.equals(player)) {
                for (String str : Hypixelify.getConfigurator().config.getStringList("party.message.cannot-blank-yourself")) {
                    player.sendMessage(ShopUtil.translateColors(str).replace("{blank}", "kick"));
                }
                return true;
            }

            if (!partyManager.getParty(player).getAllPlayers().contains(invited)) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return true;
            }

            partyManager.kickFromParty(invited);
            return true;

        }

        else if (args[0].equalsIgnoreCase("warp")) {

            if (args.length != 1) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            if (!player.isOnline()) {
                return true;
            }

            if (!partyManager.isInParty(player)) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (partyManager.getParty(player) == null) {
                player.sendMessage(ChatColor.RED + "An error has occured");
                return true;
            }

            if(playerDatabase == null || playerDatabase.getPartyLeader() == null){
                player.sendMessage("§cSomething went wrong, reload Addon or BedWars to fix this issue");
                return true;
            }
            if (!playerDatabase.getPartyLeader().equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return true;
            }
            partyManager.warpPlayersToLeader(player);

        }

        else if (args[0].equalsIgnoreCase("chat")) {

            if (playerDatabase == null) return true;

            if (!playerDatabase.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (args.length != 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            playerDatabase.setPartyChatEnabled(args[1].equalsIgnoreCase("on"));

            String mode = args[1].equals("on") ? "enabled" : "disabled";

            for (String st : Hypixelify.getConfigurator().config.getStringList("party.message.chat-enable-disabled")) {
                player.sendMessage(ShopUtil.translateColors(st).replace("{mode}", mode));
            }
            return true;

        } else {
            ShopUtil.sendMessage(player, Messages.message_invalid_command);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player))
            return null;
        Player player = (Player) commandSender;

        final PlayerDatabase playerDatabase = Hypixelify.getDatabaseManager().getDatabase(player);

        if (playerDatabase!= null && playerDatabase.isInvited()) {
            return Arrays.asList("accept", "decline");
        }
        if (strings.length == 1) {
            if (playerDatabase != null && playerDatabase.isInParty()
                    && playerDatabase.getPartyLeader().equals(player))
                return Arrays.asList("invite", "list", "disband", "kick", "warp");


            return Arrays.asList("invite", "list", "help", "chat");
        }

        if (strings.length == 2 && strings[0].equalsIgnoreCase("chat")) {
            return Arrays.asList("on", "off");
        }

        return null;
    }
}
