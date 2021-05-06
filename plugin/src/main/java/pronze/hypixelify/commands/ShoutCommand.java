package pronze.hypixelify.commands;
import org.screamingsandals.bedwars.lib.ext.cloud.arguments.standard.StringArrayArgument;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.player.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.game.TeamColor;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.Permissions;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;
import java.util.List;

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
                            final var wrapper = PlayerMapper
                                    .wrapPlayer(player);
                            final var gameOptional = PlayerManager
                                    .getInstance()
                                    .getGameOfPlayer(wrapper);

                            final var bPlayer = PlayerManager
                                    .getInstance()
                                    .getPlayer(wrapper)
                                    .orElseThrow();

                            if(gameOptional.isEmpty()){
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.MESSAGE_NOT_IN_GAME)
                                        .send(wrapper);
                                return;
                            }

                            if(bPlayer.isSpectator){
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.COMMAND_DISABLED_FOR_SPECTATORS)
                                        .send(wrapper);
                                return;
                            }

                            final var cancelShout = SBAConfig
                                    .getInstance()
                                    .node("shout", "time-out")
                                    .getInt(60) == 0;

                            if(!cancelShout && !player.hasPermission(Permissions.SHOUT_BYPASS.getKey())) {
                                final var sbaPlayerWrapper = wrapper.as(PlayerWrapper.class);
                                if (!sbaPlayerWrapper.canShout()) {
                                    final var shout = String.valueOf(sbaPlayerWrapper.getShoutTimeOut());
                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.MESSAGE_SHOUT_WAIT)
                                            .replace("%seconds%", shout)
                                            .send(sbaPlayerWrapper);
                                    return;
                                }
                            }

                            final var game = gameOptional.get();
                            final var team = game.getTeamOfPlayer(player);
                            String color = ChatColor.GRAY.toString();
                            if(team != null) {
                                color = TeamColor.valueOf(team.getColor().name()).chatColor.toString();
                            }
                            final var strBuilder = new StringBuilder();
                            final var args = (List<String>) c.get("args");
                            if (args.isEmpty()) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.COMMAND_SHOUT_INVALID_USAGE)
                                        .send(wrapper);
                                return;
                            }

                            args.forEach(st -> strBuilder.append(st).append(" "));

                            Component shoutMessage = LanguageService
                                    .getInstance()
                                    .get(MessageKeys.SHOUT_FORMAT)
                                    .replace("%color%", color)
                                    .replace("%player%", player.getName())
                                    .replace("%message%", strBuilder.toString())
                                    .replace("%team%", team == null ? "" : team.getName())
                                    .toComponent();

                            wrapper.as(PlayerWrapper.class).shout(shoutMessage, game);
                        }).execute()));
    }
}
