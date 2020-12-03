package io.pronze.hypixelify.commands;

import io.pronze.hypixelify.api.party.Party;
import io.pronze.hypixelify.api.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.wrapper.PlayerWrapper;
import io.pronze.hypixelify.message.Messages;
import io.pronze.hypixelify.service.PlayerWrapperService;
import io.pronze.hypixelify.utils.MessageUtils;
import io.pronze.hypixelify.utils.ShopUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyCommand extends AbstractCommand {

    private final boolean partyEnabled;


    public PartyCommand() {
        super(null, false, "party");
        partyEnabled = SBAHypixelify.getConfigurator().config.getBoolean("party.enabled", true);

    }

    @Override
    public boolean onPreExecute(CommandSender sender, String[] args) {
        if (!partyEnabled) {
            sender.sendMessage("§cCannot access command, party system is disabled.");
            return false;
        }
        //can cast safely due to code in abstract class
        final Player player = (Player) sender;

        final PlayerWrapper playerWrapper = SBAHypixelify.getWrapperService().getWrapper(player);
        final Player partyLeader = playerWrapper.getPartyLeader();

        if (playerWrapper.isInParty() &&
                partyLeader != null && !partyLeader.isOnline()) {
            player.sendMessage("§cPlease wait until the leader comes back online..., or party disbands");
            return false;
        }
        return true;
    }

    @Override
    public void onPostExecute() {

    }

    @Override
    public void execute(String[] args, CommandSender sender) {
        Player player = (Player) sender;

        if (args == null || args.length == 0 || args.length > 2) {
            ShopUtil.sendMessage(player, Messages.message_invalid_command);
            return;
        }

        final PlayerWrapperService playerWrapperService = SBAHypixelify.getWrapperService();
        final PlayerWrapper playerWrapper = playerWrapperService.getWrapper(player);
        final PartyManager partyManager = SBAHypixelify.getPartyManager();
        final Party initParty = playerWrapper.getParty();
        final Player initPartyLeader = playerWrapper.getPartyLeader();


        if (args[0].equalsIgnoreCase("invite")) {
            if (args.length != 2) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return;
            }

            Party party = partyManager.getParty(playerWrapper.getPartyLeader());

            //Party size limit
            int max_sz = SBAHypixelify.getConfigurator().config.getInt("party.size", 4);
            if (playerWrapper.isInParty() && party != null && party.getCompleteSize() >= max_sz) {
                player.sendMessage("§cParty has reached maximum Size.");
                return;
            }


            //check if player argument is online
            final Player invited = Bukkit.getPlayerExact(args[1].toLowerCase());
            if (invited == null) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return;
            }


            if (invited.getUniqueId().equals(player.getUniqueId())) {
                ShopUtil.sendMessage(player, Messages.message_cannot_invite_yourself);
                return;
            }

            final PlayerWrapper invitedData = playerWrapperService.getWrapper(invited);

            if (playerWrapper.isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_decline_inc);
                return;
            }


            party = partyManager.getParty(player);
            if (party == null) {
                party = partyManager.createParty(player);
                playerWrapper.setIsInParty(true);
                playerWrapper.setParty(player);
            }

            if (invitedData.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_cannot_invite);
                return;
            }

            if (invitedData.isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_already_invited);
                return;
            }

            if ( playerWrapper.isPartyLeader() || party.canAnyoneInvite()) {
                party.addInvitedMember(invited);
                final Map<String, String> replacementPair = new HashMap<>();
                replacementPair.put("{player}", player.getDisplayName());

                MessageUtils.sendMessage("party.message.invite", invited, replacementPair);
                replacementPair.put("{player}", invited.getDisplayName());
                MessageUtils.sendMessage("party.message.invited", player, replacementPair);
                return;
            }

        }

        //check if player does not do other commands on his newly created party.
        else if (playerWrapper.isInParty() && initParty != null
                && initParty.getPlayers() == null) {
            ShopUtil.sendMessage(player, Messages.message_no_other_commands);
            return;
        } else if (args[0].equalsIgnoreCase("accept")) {

            if (!playerWrapper.isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_not_invited);
                return;
            }

            if (playerWrapper.isInParty() || playerWrapper.getInvitedParty() == null) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return;
            }

            final Party pParty = playerWrapper.getInvitedParty();
            partyManager.addToParty(player, pParty);
            return;

        } else if (args[0].equalsIgnoreCase("leave")) {

            if (args.length != 1) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return;
            }

            if (!playerWrapper.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return;
            }
            if (playerWrapper.isPartyLeader()) {
                player.sendMessage("§cYou have to disband the party first!");
                return;
            }

            if (initPartyLeader == null || initParty == null)
                return;

            if (initParty == null) return;
            partyManager.removeFromParty(player, initParty);
            ShopUtil.sendMessage(player, Messages.message_party_left);
        } else if (args[0].equalsIgnoreCase("decline")) {

            if (!playerWrapper.isInvited()) {
                ShopUtil.sendMessage(player, Messages.message_not_invited);
                return;
            }

            final Party invitedParty = playerWrapper.getInvitedParty();

            if (invitedParty == null || invitedParty.getLeader() == null) return;

            partyManager.getParty(invitedParty.getLeader()).removeInvitedMember(player);

            ShopUtil.sendMessage(player, Messages.message_decline_user);


            final List<Player> partyMembers = invitedParty.getAllPlayers();

            if (partyMembers != null) {
                final Map<String, String> replacementMap = new HashMap<>();
                replacementMap.put("{player}", player.getDisplayName());

                partyMembers.forEach(member -> {
                    MessageUtils.sendMessage("party.message.declined", member, replacementMap);
                });
            }

            playerWrapper.setInvited(false);
            playerWrapper.setInvitedParty(null);
            return;

        } else if (args[0].equalsIgnoreCase("list")) {

            if (!playerWrapper.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return;
            }

            if (initPartyLeader == null) return;
            player.sendMessage("Players: ");
            final List<Player> members = initParty.getAllPlayers();
            if (members != null) {
                members.forEach(member -> {
                    player.sendMessage(member.getDisplayName());
                });
            }

            return;
        } else if (args[0].equalsIgnoreCase("disband")) {

            if (initParty == null) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return;
            }

            if (!initPartyLeader.equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return;
            }
            partyManager.disband(player);
            return;
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (!playerWrapper.isInParty() || initPartyLeader == null) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return;
            }

            if (!initPartyLeader.equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return;
            }
            if (args.length != 2) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return;
            }

            final Player invited = Bukkit.getPlayerExact(args[1].toLowerCase());
            if (invited == null) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return;
            }

            if (invited.equals(player)) {
                for (String str : SBAHypixelify.getConfigurator().config.getStringList("party.message.cannot-blank-yourself")) {
                    player.sendMessage(ShopUtil.translateColors(str).replace("{blank}", "kick"));
                }
                return;
            }

            if (partyManager.getParty(player).getPlayers() == null ||
                    partyManager.getParty(player).getPlayers().isEmpty()) return;

            if (!partyManager.getParty(player).getAllPlayers().contains(invited)) {
                ShopUtil.sendMessage(player, Messages.message_player_not_found);
                return;
            }

            partyManager.kickFromParty(invited);
            return;

        } else if (args[0].equalsIgnoreCase("warp")) {

            if (args.length != 1) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return;
            }

            if (!player.isOnline()) {
                return;
            }

            if (!partyManager.isInParty(player)) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return;
            }

            if (partyManager.getParty(player) == null) {
                player.sendMessage("§cAn error has occured");
                return;
            }

            if (playerWrapper.getPartyLeader() == null) {
                player.sendMessage("§cSomething went wrong, reload Addon or BedWars to fix this issue");
                return;
            }
            if (!playerWrapper.getPartyLeader().equals(player)) {
                ShopUtil.sendMessage(player, Messages.message_access_denied);
                return;
            }
            partyManager.warpPlayersToLeader(player);

        } else if (args[0].equalsIgnoreCase("chat")) {

            if (!playerWrapper.isInParty()) {
                ShopUtil.sendMessage(player, Messages.message_not_in_party);
                return;
            }

            if (args.length != 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                ShopUtil.sendMessage(player, Messages.message_invalid_command);
                return;
            }

            playerWrapper.setPartyChatEnabled(args[1].equalsIgnoreCase("on"));

            String mode = args[1].equals("on") ? "enabled" : "disabled";

            for (String st : SBAHypixelify.getConfigurator().config.getStringList("party.message.chat-enable-disabled")) {
                player.sendMessage(ShopUtil.translateColors(st).replace("{mode}", mode));
            }
            return;

        } else {
            ShopUtil.sendMessage(player, Messages.message_invalid_command);
            return;
        }
    }

    @Override
    public void displayHelp(CommandSender sender) {
        Player player = (Player) sender;
        ShopUtil.sendMessage(player, Messages.message_party_help);
    }

    @Override
    public List<String> tabCompletion(String[] strings, CommandSender commandSender) {
        if (!(commandSender instanceof Player))
            return null;
        final Player player = (Player) commandSender;

        final PlayerWrapper playerWrapper = SBAHypixelify.getWrapperService().getWrapper(player);

        if (playerWrapper.isInvited()) {
            return Arrays.asList("accept", "decline");
        }
        if (strings.length == 1) {
            final Player partyLeader = playerWrapper.getPartyLeader();
            if (playerWrapper.isInParty() && partyLeader != null &&
                    playerWrapper.getPartyLeader().equals(player))
                return Arrays.asList("invite", "list", "disband", "kick", "warp");


            return Arrays.asList("invite", "list", "help", "chat");
        }

        if (strings.length == 2 && strings[0].equalsIgnoreCase("chat")) {
            return Arrays.asList("on", "off");
        }

        return null;
    }
}
