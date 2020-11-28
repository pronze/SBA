package io.pronze.hypixelify.commands;

import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.inventories.GamesInventory;
import io.pronze.hypixelify.utils.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.game.GameCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BWACommand extends AbstractCommand {

    private final boolean gamesInvEnabled;

    public BWACommand() {
        super(null, true, "bwaddon");
        gamesInvEnabled = SBAHypixelify.getConfigurator().config.getBoolean("games-inventory.enabled", true);
    }


    @Override
    public boolean onPreExecute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("misat11.bw.admin") &&
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

        final String base = args[0];
        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        final FileConfiguration config = SBAHypixelify.getConfigurator().config;

        switch (base.toLowerCase()) {


            case "reload":
                pluginManager.disablePlugin(Main.getInstance());
                pluginManager.enablePlugin(Main.getInstance());
                sender.sendMessage("Plugin reloaded!");
                break;


            case "setlobby":
                if (!(sender instanceof Player)) return;

                Player player = (Player) sender;
                Location location = player.getLocation();

                config.set("main-lobby.enabled", true);
                config.set("main-lobby.world", location.getWorld().getName());
                config.set("main-lobby.x", location.getX());
                config.set("main-lobby.y", location.getY());
                config.set("main-lobby.z", location.getZ());
                config.set("main-lobby.yaw", location.getYaw());
                config.set("main-lobby.pitch", location.getPitch());
                SBAHypixelify.getConfigurator().saveConfig();
                player.sendMessage("Sucessfully set Lobby location!");
                break;


            case "reset":
                sender.sendMessage("Resetting...");
                try {
                    SBAHypixelify.getConfigurator().upgradeCustomFiles();
                    sender.sendMessage("Sucessfully resetted");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            case "gamesinv":
                if (args.length != 2) {
                    sender.sendMessage("[SBAHypixelify]" + "§cUnknown command, do /bwaddon help for more.");
                    return;
                }

                if (!config.getBoolean("games-inventory.enabled", true)) {
                    sender.sendMessage("§cGames inventory has been disabled, Contact the server owner to enable it.");
                    return;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("[SBAHypixelify]" + " §cYou cannot do this command in the console");
                    return;
                }

                final Player pl = (Player) sender;
                final GamesInventory gamesInventory = SBAHypixelify.getGamesInventory();

                final int mode = ShopUtil.getIntFromMode(args[1]);
                if (mode == 0) {
                    pl.sendMessage("[SBAHypixelify]" + "§cUnknown command, do /bwaddon help for more.");
                    return;
                }

                gamesInventory.openForPlayer(pl, mode);
                break;


            case "upgrade":
                if (!SBAHypixelify.isUpgraded()) {
                    sender.sendMessage("Cannot do this right now!");
                    break;
                }
                try {
                    SBAHypixelify.getConfigurator().upgradeCustomFiles();
                    sender.sendMessage("[SBAHypixelify]: " + "§6Sucessfully upgraded files!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            case "cancel":
                if (!SBAHypixelify.isUpgraded()) {
                    sender.sendMessage("Cannot do this right now!");
                    break;
                }

                config.set("version", SBAHypixelify.getVersion());
                SBAHypixelify.getConfigurator().saveConfig();
                sender.sendMessage("[SBAHypixelify]: Cancelled shop and upgradeShop changes");
                break;

            case "cleargens":
                BedwarsAPI.getInstance().getGames().forEach(game -> {
                    if (game == null) {
                        return;
                    }

                    final World world = game.getGameWorld();

                    if (world == null) {
                        return;
                    }

                    for (Entity entity : world.getEntities()) {
                        if (entity == null) {
                            return;
                        }
                        if (entity.getType() == EntityType.ARMOR_STAND) {
                            if (GameCreator.isInArea(entity.getLocation(), game.getPos1(), game.getPos2())) {
                                entity.remove();
                            }
                        }
                    }

                });
                break;
            default:
                sender.sendMessage("[SBAHypixelify]" + "§cUnknown command, do /bwaddon help for more.");
                break;
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
            if (SBAHypixelify.isUpgraded()) {
                return Arrays.asList("cancel", "upgrade");
            }
            final List<String> Commands = new ArrayList<>(Arrays
                    .asList("reload", "help", "reset", "gamesinv", "setlobby", "cleargens"));

            if (!gamesInvEnabled)
                Commands.remove("gamesinv");

            return Commands;
        }
        if (strings.length == 2 && strings[0].equalsIgnoreCase("gamesinv")) {
            return Arrays.asList("solo", "double", "triple", "squad");
        }

        return null;
    }
}

