package org.pronze.hypixelify.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;

import java.util.Arrays;
import java.util.List;

public class GameSelectorCommands implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("You have to be in game to execute this command");
            return true;
        }

        Player player = (Player) sender;
        if(args.length >= 1){
            if(args[0].equalsIgnoreCase("Solo") || args[0].equalsIgnoreCase("Double")
            || args[0].equalsIgnoreCase("Triple") || args[0].equalsIgnoreCase("Squad")){
                Hypixelify.getGamesInventory().showGUI(player, args[0]);
            }

            else if(args[0].equalsIgnoreCase("help")){
                sender.sendMessage(ChatColor.RED + "SBAHypixelify");
                sender.sendMessage("Available commands:");
                sender.sendMessage("/bwinv Solo - Shows all Solo matches");
                sender.sendMessage("/bwinv Double - Shows all Doubles matches");
                sender.sendMessage("/bwinv Triple - Shows all Triples matches");
                sender.sendMessage("/bwinv Squad - Shows all Squad matches");
            }
            else{
                sender.sendMessage("[SBAHypixelify]" + ChatColor.RED + "Unknown command, do /bwinv help for more.");
            }
        }
        else{
                sender.sendMessage("[SBAHypixelify]" + ChatColor.RED + "Unknown command, do /bwinv help for more.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 1){
            return Arrays.asList("Solo", "Double", "Triple", "Squad", "help");
        }
        return null;
    }
}
