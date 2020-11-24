package org.pronze.hypixelify.commands;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.wrapper.PlayerWrapper;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Arrays;
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
        final Player player = (Player) sender;

        if(!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)){
            ShopUtil.sendMessage(player, Messages.message_not_in_game);
            return;
        }

        final Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);

        if(game.getTeamOfPlayer(player) == null){
            player.sendMessage("§cYou cannot do this command while spectating");
            return;
        }

        final PlayerWrapper playerWrapper = SBAHypixelify.getWrapperService().getWrapper(player);
        final boolean cancelShout = SBAHypixelify.getConfigurator()
                .config.getInt("shout.time-out", 60) == 0;

        if(!cancelShout && !hasPermission(player)) {
            if (!playerWrapper.canShout()) {
                String shout = String.valueOf(playerWrapper.getShoutTimeOut());
                for (String st : Messages.message_shout_wait) {
                    player.sendMessage(ShopUtil.translateColors(st.replace("{seconds}", shout)));
                }
                return;
            }
        }

        final RunningTeam team = game.getTeamOfPlayer(player);
        String color = ChatColor.GRAY.toString();
        if(team != null) {
            color = TeamColor.valueOf(team.getColor().name()).chatColor.toString();
        }

        final StringBuilder builder = new StringBuilder();

        Arrays.stream(args).forEach(st -> builder.append(st).append(" "));

        String st = Messages.shoutFormat
                .replace("{color}", color)
                .replace("{player}", player.getName())
                .replace("{message}", builder.toString());

        if(team != null){
            st = st.replace("{team}", team.getName());
        }

        for(Player pl : game.getConnectedPlayers()){
            pl.sendMessage(st);
        }

        if(!cancelShout && !hasPermission(player))
            playerWrapper.shout();
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
