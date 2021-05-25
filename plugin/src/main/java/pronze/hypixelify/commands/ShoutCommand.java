package pronze.hypixelify.commands;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.Permissions;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

import javax.annotation.Syntax;
import java.util.List;

@Service
public class ShoutCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("shout <args>")
    @CommandDescription("shout command")
    private void commandShout(
            final @NotNull Player player,
            final @NotNull @Argument("args") String[] argsParam
    ) {
        final var args = List.of(argsParam);

        final var wrapper = PlayerMapper
                .wrapPlayer(player);

        if(!Main.getInstance().isPlayerPlayingAnyGame(player)){
            LanguageService
                    .getInstance()
                    .get(MessageKeys.MESSAGE_NOT_IN_GAME)
                    .send(wrapper);
            return;
        }

        final var bPlayer = Main.getPlayerGameProfile(player);

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

        final var game = Main.getInstance().getGameOfPlayer(player);
        final var team = game.getTeamOfPlayer(player);
        String color = ChatColor.GRAY.toString();
        if(team != null) {
            color = TeamColor.valueOf(team.getColor().name()).chatColor.toString();
        }
        final var strBuilder = new StringBuilder();
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
    }

}
