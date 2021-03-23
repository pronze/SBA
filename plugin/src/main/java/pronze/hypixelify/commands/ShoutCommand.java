package pronze.hypixelify.commands;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.ext.cloud.arguments.standard.StringArrayArgument;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.game.TeamColor;
import pronze.hypixelify.api.Permissions;

import java.util.List;

import static pronze.hypixelify.lib.lang.I.i18n;

public class ShoutCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public ShoutCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    public void build() {
        final var builder = this.manager.commandBuilder("shout");
        manager.command(builder
                .senderType(Player.class)
                .argument(StringArrayArgument.of( "args", (ctx, s) -> List.of()))
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

                            final var playerWrapper = SBAHypixelify.getInstance().getPlayerWrapperService().get(player).get();
                            final var cancelShout = SBAHypixelify.getConfigurator()
                                    .config.getInt("shout.time-out", 60) == 0;

                            if(!cancelShout && !player.hasPermission(Permissions.SHOUT_BYPASS.getKey())) {
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

                            playerWrapper.shout(st, game);
                        }).execute()));
    }
}
