package org.pronze.hypixelify.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.party.Party;
import org.pronze.hypixelify.utils.ShopUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

        final HashMap<UUID, PlayerDatabase> Database = Hypixelify.getInstance().playerData;

        if (args[0].equalsIgnoreCase("help")) {
            if (args.length != 1) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }
            ShopUtil.sendMessage(player, Messages.message_party_help);
            return true;
        }

        else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length != 2) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }
            if (Database.get(player.getUniqueId()) != null) {
                PlayerDatabase data = Database.get(player.getUniqueId());
                int max_sz = Hypixelify.getConfigurator().config.getInt("party.size", 4);
                if (data.isInParty() && Hypixelify.getInstance().partyManager.parties.get(data.getPartyLeader()) != null && Hypixelify.getInstance().partyManager.parties.get(data.getPartyLeader()).getCompleteSize() >= max_sz) {
                    player.sendMessage("§cParty has reached maximum Size.");
                    return true;
                }
            }

            Player invited = Bukkit.getPlayerExact(args[1].toLowerCase());
            if (invited == null) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return true;
            }

            if (invited.getUniqueId().equals(player.getUniqueId())) {
                ShopUtil.sendMessage(player, Messages.message_cannot_invite_yourself);
                return true;
            }

            PlayerDatabase data = Database.get(player.getUniqueId());
            if (data == null)
                Hypixelify.getInstance().playerData.put(player.getUniqueId(), new PlayerDatabase(player));

            PlayerDatabase invitedData = Database.get(invited.getUniqueId());
            if (invitedData == null)
                Hypixelify.getInstance().playerData.put(invited.getUniqueId(), new PlayerDatabase(invited));

            if (Database.get(player.getUniqueId()).isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_decline_inc);
                return true;
            }

            Party party;
            if (!Database.get(player.getUniqueId()).isInParty()) {
                party = Hypixelify.getInstance().partyManager.parties.get(player);
                if (party == null) {
                    party = new Party(player);
                    Hypixelify.getInstance().partyManager.parties.put(player, party);
                    Hypixelify.getInstance().playerData.get(player.getUniqueId()).setIsInParty(true);
                    Hypixelify.getInstance().playerData.get(player.getUniqueId()).setPartyLeader(player);
                }
            } else {
                party = Hypixelify.getInstance().partyManager.parties.get(Database.get(player.getUniqueId()).getPartyLeader());
            }

            if (Database.get(invited.getUniqueId()).isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_cannot_invite);
                return true;
            }

            if (Database.get(invited.getUniqueId()).isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_already_invited);
                return true;
            }

            if (party.canAnyoneInvite() || player.equals(party.getLeader())) {
                Hypixelify.getInstance().partyManager.parties.get(party.getLeader()).addInvitedMember(invited);
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
        else if (Database.get(player.getUniqueId()) != null && Database.get(player.getUniqueId()).isInParty() &&
                Hypixelify.getInstance().partyManager.parties.get(Database.get(player.getUniqueId()).getPartyLeader()) != null &&
                Hypixelify.getInstance().partyManager.getParty(player).getPlayers() == null) {
            ShopUtil.sendMessage(player, Messages.message_no_other_commands);
            return true;
        }

        else if (args[0].equalsIgnoreCase("accept")) {

            if (Database.get(player.getUniqueId()) == null) {
                player.sendMessage("§cAn error has occurred..");
                return true;
            }

            if (!Database.get(player.getUniqueId()).isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_not_invited);
                return true;
            }

            if (Database.get(player.getUniqueId()).isInParty() || Database.get(player.getUniqueId()).getInvitedParty() == null) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            Party pParty = Database.get(player.getUniqueId()).getInvitedParty();
            Hypixelify.getInstance().partyManager.addToParty(player, pParty);
            return true;

        }

        else if (args[0].equalsIgnoreCase("leave")) {

            PlayerDatabase db = Hypixelify.getInstance().playerData.get(player.getUniqueId());
            if (args.length != 1) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            if (!db.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }
            if (db.isPartyLeader()) {
                player.sendMessage("§cYou have to disband the party first!");
                return true;
            }

            if (db.getPartyLeader() == null || Hypixelify.getInstance().partyManager.parties.get(db.getPartyLeader()) == null)
                return true;

            Party party = Hypixelify.getInstance().partyManager.parties.get(db.getPartyLeader());
            if (party == null) return true;
            Hypixelify.getInstance().partyManager.removeFromParty(player, party);

        }

        else if (args[0].equalsIgnoreCase("decline")) {

            if (Database.get(player.getUniqueId()) == null) return true;

            if (!Database.get(player.getUniqueId()).isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_not_invited);
                return true;
            }

            Party invitedParty = Database.get(player.getUniqueId()).getInvitedParty();

            if (invitedParty == null || invitedParty.getLeader() == null) return true;

            Hypixelify.getInstance().partyManager.parties.get(invitedParty.getLeader()).removeInvitedMember(player);

            ShopUtil.sendMessage(player, Messages.message_decline_user);

            for (Player pl : invitedParty.getAllPlayers()) {
                if (pl != null && pl.isOnline()) {
                    ShopUtil.sendMessage(pl, Messages.message_declined);
                }
            }
            Database.get(player.getUniqueId()).setInvited(false);
            Database.get(player.getUniqueId()).setExpiredTimeTimeout(60);
            Database.get(player.getUniqueId()).setInvitedParty(null);
            return true;

        }

        else if (args[0].equalsIgnoreCase("list")) {

            if (Database.get(player.getUniqueId()) == null) return true;
            if (!Database.get(player.getUniqueId()).isInParty()){
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            Player leader = Database.get(player.getUniqueId()).getPartyLeader();
            if(leader == null) return true;
            player.sendMessage("Players: ");
            for (Player pl : Hypixelify.getInstance().partyManager.parties.get(leader).getAllPlayers()) {
                player.sendMessage(pl.getDisplayName());
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("disband")) {

            PlayerDatabase data = Database.get(player.getUniqueId());
            if (data == null || Hypixelify.getInstance().partyManager.parties.get(data.getPartyLeader()) == null) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (!data.getPartyLeader().equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return true;
            }
            Hypixelify.getInstance().partyManager.disband(player);
            return true;
        }

        else if (args[0].equalsIgnoreCase("kick")) {
            PlayerDatabase data = Database.get(player.getUniqueId());
            if (!data.isInParty() || data.getPartyLeader() == null) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (!data.getPartyLeader().equals(player)) {
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

            if (!Hypixelify.getInstance().partyManager.parties.get(player).getAllPlayers().contains(invited)) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return true;
            }

            Hypixelify.getInstance().partyManager.kickFromParty(invited);
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

            if (!Hypixelify.getInstance().partyManager.isInParty(player)) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (Hypixelify.getInstance().partyManager.getParty(player) == null) {
                player.sendMessage(ChatColor.RED + "An error has occured");
                return true;
            }

            PlayerDatabase database = Hypixelify.getInstance().playerData.get(player.getUniqueId());
            if(database == null || database.getPartyLeader() == null){
                player.sendMessage("§cSomething went wrong, reload Addon or BedWars to fix this issue");
                return true;
            }
            if (!database.getPartyLeader().equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return true;
            }
            Hypixelify.getInstance().partyManager.warpPlayersToLeader(player);

        }

        else if (args[0].equalsIgnoreCase("chat")) {

            PlayerDatabase db = Database.get(player.getUniqueId());
            if (db == null) return true;

            if (!db.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return true;
            }

            if (args.length != 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return true;
            }

            Database.get(player.getUniqueId()).setPartyChatEnabled(args[1].equalsIgnoreCase("on"));

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

        if (Hypixelify.getInstance().playerData.get(player.getUniqueId()) != null && Hypixelify.getInstance().playerData.get(player.getUniqueId()).isInvited()) {
            return Arrays.asList("accept", "decline");
        }
        if (strings.length == 1) {
            if (Hypixelify.getInstance().playerData.get(player.getUniqueId()) != null && Hypixelify.getInstance().playerData.get(player.getUniqueId()).isInParty()
                    && Hypixelify.getInstance().playerData.get(player.getUniqueId()).getPartyLeader().equals(player))
                return Arrays.asList("invite", "list", "disband", "kick", "warp");


            return Arrays.asList("invite", "list", "help", "chat");
        }

        if (strings.length == 2 && strings[0].equalsIgnoreCase("chat")) {
            return Arrays.asList("on", "off");
        }

        return null;
    }
}
