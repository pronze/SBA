package org.pronze.hypixelify.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class BWACommand extends AbstractCommand {

    //Use default bedwars admin permissions
    public BWACommand() {
        super("misat11.bw.admin", true, "bwaddon");
    }


    @Override
    public boolean onPreExecute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission(getPerm()) &&
                    !args[0].equalsIgnoreCase("gamesinv")) {
                sender.sendMessage("§cYou Don't have permissions to do this command");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onPostExecute() {

    }

    @Override
    public void execute(String[] args, CommandSender sender) {

        if (args[0].equalsIgnoreCase("reload")) {
            Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
            Bukkit.getServer().getPluginManager().enablePlugin(Main.getInstance());
            sender.sendMessage("Plugin reloaded!");
        } else if (args[0].equalsIgnoreCase("setlobby")) {
            if (!(sender instanceof Player)) return;

            Player player = (Player) sender;
            Location location = player.getLocation();

            Hypixelify.getConfigurator().config.set("main-lobby.enabled", true);
            Hypixelify.getConfigurator().config.set("main-lobby.world", location.getWorld().getName());
            Hypixelify.getConfigurator().config.set("main-lobby.x", location.getX());
            Hypixelify.getConfigurator().config.set("main-lobby.y", location.getY());
            Hypixelify.getConfigurator().config.set("main-lobby.z", location.getZ());
            Hypixelify.getConfigurator().config.set("main-lobby.yaw", location.getYaw());
            Hypixelify.getConfigurator().config.set("main-lobby.pitch", location.getPitch());
            Hypixelify.getConfigurator().saveConfig();
            player.sendMessage("Sucessfully set Lobby location!");
        } else if (args[0].equalsIgnoreCase("reset")) {
            sender.sendMessage("Resetting...");
            try {
                Hypixelify.getConfigurator().upgradeCustomFiles();
                sender.sendMessage("Sucessfully resetted");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("gamesinv")) {
            if (args.length != 2) {
                sender.sendMessage("[SBAHypixelify]" + "§cUnknown command, do /bwaddon help for more.");
                return;
            }

            if (!Hypixelify.getConfigurator().config.getBoolean("games-inventory.enabled")) {
                sender.sendMessage("§cGames inventory has been disabled, Contact the server owner to enable it.");
                return;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("[SBAHypixelify]" + " §cYou cannot do this command in the console");
                return;
            }

            Player player = (Player) sender;
            if (args[1].equalsIgnoreCase("solo")) {
                Hypixelify.getGamesInventory().openForPlayer(player, 1);
            } else if (args[1].equalsIgnoreCase("double")) {
                Hypixelify.getGamesInventory().openForPlayer(player, 2);
            } else if (args[1].equalsIgnoreCase("triple")) {
                Hypixelify.getGamesInventory().openForPlayer(player, 3);
            } else if (args[1].equalsIgnoreCase("squad")) {
                Hypixelify.getGamesInventory().openForPlayer(player, 4);
            } else {
                sender.sendMessage("[SBAHypixelify]" + "§cUnknown command, do /bwaddon help for more.");
            }

        }


        //Show the upgrade files command on update
        else if (Hypixelify.isUpgraded()) {

            if (args[0].equalsIgnoreCase("upgrade")) {
                try {
                    Hypixelify.getConfigurator().upgradeCustomFiles();
                    sender.sendMessage("[SBAHypixelify]: " + "§6Sucessfully upgraded files!");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (args[0].equalsIgnoreCase("cancel")) {
                Hypixelify.getConfigurator().config.set("version", Hypixelify.getVersion());
                Hypixelify.getConfigurator().saveConfig();
                sender.sendMessage("[SBAHypixelify]: Cancelled shop and upgradeShop changes");
            }
        } else {
            sender.sendMessage("[SBAHypixelify]" + "§cUnknown command, do /bwaddon help for more.");
        }


    }

    @Override
    public void displayHelp(CommandSender sender) {
        sender.sendMessage("§cSBAHypixelify");
        sender.sendMessage("Available commands:");
        sender.sendMessage("/bwaddon reload - Reload the addon");
        sender.sendMessage("/bwaddon help - Show available list of commands");
        sender.sendMessage("/bwaddon reset - resets all configs related to addon");
        sender.sendMessage("/bwaddon setlobby - sets lobby for scoreboard and chat message");
    }

    @Override
    public List<String> tabCompletion(String[] strings, CommandSender commandSender) {
        if (!commandSender.hasPermission("misat11.bw.admin")
            || !commandSender.hasPermission("bw.admin"))
            return null;
        if (strings.length == 1) {
            if (!Objects.requireNonNull(Hypixelify.getConfigurator().config.getString("version")).contains(Hypixelify.getVersion())) {
                return Arrays.asList("cancel", "upgrade");
            }
            List<String> Commands = new ArrayList<>();
            Commands.add("reload");
            Commands.add("help");
            Commands.add("reset");
            Commands.add("clearnpc");
            Commands.add("gamesinv");
            Commands.add("setlobby");
            if (!Hypixelify.getConfigurator().config.getBoolean("games-inventory.enabled", true))
                Commands.remove("gamesinv");
            if (!Hypixelify.getConfigurator().config.getBoolean("citizens-shop", true))
                Commands.remove("clearnpc");

            return Commands;
        }
        if (strings.length == 2 && strings[0].equalsIgnoreCase("gamesinv")) {
            if (!Hypixelify.getConfigurator().config.getBoolean("citizens-shop", true))
                return null;
            return Arrays.asList("solo", "double", "triple", "squad");
        }

        return null;
    }
}

