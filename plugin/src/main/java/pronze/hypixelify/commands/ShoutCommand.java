package pronze.hypixelify.commands;
import cloud.commandframework.arguments.standard.StringArrayArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.Main;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pronze.hypixelify.lib.lang.I.i18n;

public class ShoutCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public ShoutCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
    }

    public boolean canByPass(Player player){
        return player.hasPermission("bwaddon.shout") || player.isOp();
    }

    @SuppressWarnings("unchecked")
    public void build() {
        final var builder = this.manager.commandBuilder("shout");
        manager.command(builder
                .senderType(Player.class)
                .argument(StringArrayArgument.ofType(String.class, "args"))
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            final var player = (Player) c.getSender();
                            final var game = BedwarsAPI.getInstance().getGameOfPlayer(player);

                            if(!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)){
                                player.sendMessage(i18n("not_in_game", true));
                                return;
                            }

                            if(Main.getPlayerGameProfile(player).isSpectator){
                                player.sendMessage(i18n("command_spectator_disabled", true));
                                return;
                            }

                            final var playerWrapper = SBAHypixelify.getWrapperService().getWrapper(player);
                            final var cancelShout = SBAHypixelify.getConfigurator()
                                    .config.getInt("shout.time-out", 60) == 0;

                            if(!cancelShout && !canByPass(player)) {
                                if (!playerWrapper.canShout()) {
                                    final var shout = String.valueOf(playerWrapper.getShoutTimeOut());
                                    player.sendMessage(i18n("shout_wait", true)
                                            .replace("{seconds}", shout));
                                    return;
                                }
                            }

                            final var team = game.getTeamOfPlayer(player);
                            String color = ChatColor.GRAY.toString();
                            if(team != null) {
                                color = TeamColor.valueOf(team.getColor().name()).chatColor.toString();
                            }

                            final var strBuilder = new StringBuilder();
                            final var args = (List<String>) c.get("args");
                            if (args.isEmpty())
                                player.sendMessage(i18n("command_shout_invalid_usage", true));

                            args.forEach(st -> strBuilder.append(st).append(" "));

                            String st = i18n("shout-format")
                                    .replace("{color}", color)
                                    .replace("{player}", player.getName())
                                    .replace("{message}", strBuilder.toString());

                            if(team != null){
                                st = st.replace("{team}", team.getName());
                            }

                            for(Player pl : game.getConnectedPlayers()){
                                pl.sendMessage(st);
                            }

                            if(!cancelShout && !canByPass(player))
                                playerWrapper.shout();
                        }).execute()));
    }
}
