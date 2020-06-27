package org.pronze.bwaddon.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.pronze.bwaddon.BwAddon;

import java.util.Arrays;
import java.util.List;


public class BWACommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

                if (args.length >= 1) {
                    if(sender instanceof  Player) {
                        if (!sender.isOp()) {
                            sender.sendMessage(ChatColor.RED + "You Don't have permissions to do this command");
                            return true;
                        }
                    }
                   if (args[0].equalsIgnoreCase("reload")) {
                        Bukkit.getServer().getPluginManager().disablePlugin(BwAddon.getInstance());
                        Bukkit.getServer().getPluginManager().enablePlugin(BwAddon.getInstance());
                        sender.sendMessage("Plugin reloaded!");
                    }
                    else if (args[0].equalsIgnoreCase("help"))
                    {
                                  sender.sendMessage(ChatColor.RED + "ScreamingBedwarsAddon");
                                  sender.sendMessage("Available commands:");
                                  sender.sendMessage("/bwaddon reload - Reload the addon");
                                  sender.sendMessage("/bwaddon help - Show available list of commands");
                    }

                   else {
                       sender.sendMessage("[BedwarsAddon]" + ChatColor.RED + "Unknown command, do /bwaddon help for more.");
                   }
                }
                else {
                    sender.sendMessage("[BedwarsAddon]" + ChatColor.RED + "Unknown command, do /bwaddon help for more.");
                }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            return Arrays.asList("reload", "help");
        }

        return null;
    }
}

