package org.pronze.hypixelify.commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.commands.BaseCommand;
import java.util.Arrays;
import java.util.List;

public class GamesCommand extends BaseCommand {

    public GamesCommand() {
        super("gamesinv", null, false, true);
    }

    @Override
    public boolean execute(CommandSender commandSender, List<String> args) {

        if(!(commandSender instanceof Player)) return true;

        if(args.size() == 1){
            Player player = (Player) commandSender;

            if(BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player))
            {
                player.sendMessage("§cYou cannot access the GUI in game.");
                return true;
            }
            if(args.get(0).equalsIgnoreCase("solo")) {
                Hypixelify.getInstance().getSoloGameInventory().openForPlayer(player);
            } else if(args.get(0).equalsIgnoreCase("double")){
                Hypixelify.getInstance().getDoubleGameInventory().openForPlayer(player);
            } else if(args.get(0).equalsIgnoreCase("triple")){
                Hypixelify.getInstance().getTripleGameInventory().openForPlayer(player);
            } else if(args.get(0).equalsIgnoreCase("squad")){
                Hypixelify.getInstance().getSquadGameInventory().openForPlayer(player);
            }
            else
                commandSender.sendMessage("§c[BW] Invalid command");
        } else
            commandSender.sendMessage("§c[BW] Invalid command");
        return true;
    }

    @Override
    public void completeTab(List<String> completion, CommandSender sender, List<String> args) {
            if(args.size() == 1){
                completion.addAll(Arrays.asList("solo", "double", "triple", "squad"));
            }
    }
}
