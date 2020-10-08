package org.pronze.hypixelify.commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.database.PlayerDatabase;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Collections;
import java.util.List;

public class ShoutCommand extends AbstractCommand {

    public ShoutCommand() {
        super(null, false, "shout");
    }

    public boolean hasPermission(Player player){
        return player.hasPermission("bwaddon.shout") || player.isOp();
    }


    @Override
    public boolean onPreExecute(CommandSender sender, String[] args) {
        return true;
    }

    @Override
    public void onPostExecute() {

    }

    @Override
    public void execute(String[] args, CommandSender sender) {
        Player player = (Player) sender;

        if(!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)){
            ShopUtil.sendMessage(player, Messages.message_not_in_game);
            return;
        }

        Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);

        if(game.getTeamOfPlayer(player) == null){
            player.sendMessage("§cYou cannot do this command while spectating");
            return;
        }

        final PlayerDatabase playerDatabase = Hypixelify.getDatabaseManager().getDatabase(player);
        boolean cancelShout = Hypixelify.getConfigurator().config.getInt("shout.time-out", 60) == 0;

        if(!cancelShout && !hasPermission(player)) {
            if (!playerDatabase.canShout()) {
                String shout = String.valueOf(playerDatabase.getShoutTimeOut());
                for (String st : Messages.message_shout_wait) {
                    player.sendMessage(ShopUtil.translateColors(st.replace("{seconds}", shout)));
                }
                return;
            }
        }

        RunningTeam team = game.getTeamOfPlayer(player);
        String color = TeamColor.valueOf(team.getColor().name()).chatColor.toString();

        StringBuilder builder = new StringBuilder();

        for(String st : args){
            builder.append(st).append(" ");
        }

        String st = Messages.shoutFormat
                .replace("{color}", color)
                .replace("{team}", team.getName())
                .replace("{player}", player.getName())
                .replace("{message}", builder.toString());

        for(Player pl : game.getConnectedPlayers()){
            pl.sendMessage(st);
        }

        if(!cancelShout && !hasPermission(player))
            playerDatabase.shout();
    }

    @Override
    public void displayHelp(CommandSender sender) {
        sender.sendMessage("§cInvalid usage, /shout {message} is the format!");
    }

    @Override
    public List<String> tabCompletion(String[] args, CommandSender sender) {
        if(args.length == 1){
            return Collections.singletonList("help");
        }
        return null;
    }
}
