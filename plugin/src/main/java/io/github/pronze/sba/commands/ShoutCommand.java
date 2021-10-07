package io.github.pronze.sba.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.Permissions;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

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
        final var wrapper = PlayerMapper.wrapPlayer(player);

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            Message.of(LangKeys.MESSAGE_NOT_IN_GAME).send(wrapper);
            return;
        }

        final var bPlayer = Main.getPlayerGameProfile(player);

        if (bPlayer.isSpectator) {
            Message.of(LangKeys.COMMAND_DISABLED_FOR_SPECTATORS).send(wrapper);
            return;
        }

        final var cancelShout = SBAConfig
                .getInstance()
                .node("shout", "time-out")
                .getInt(60) == 0;

        if (!cancelShout && !player.hasPermission(Permissions.SHOUT_BYPASS.getKey())) {
            final var sbaPlayerWrapper = wrapper.as(SBAPlayerWrapper.class);
            if (!sbaPlayerWrapper.canShout()) {
                final var shout = String.valueOf(sbaPlayerWrapper.getShoutCooldown());
                Message.of(LangKeys.MESSAGE_SHOUT_WAIT)
                        .placeholder("seconds", shout)
                        .send(sbaPlayerWrapper);
                return;
            }
        }

        final var game = Main.getInstance().getGameOfPlayer(player);
        final var team = game.getTeamOfPlayer(player);
        String color = ChatColor.GRAY.toString();
        if (team != null) {
            color = TeamColor.valueOf(team.getColor().name()).chatColor.toString();
        }
        final var strBuilder = new StringBuilder();
        if (args.isEmpty()) {
            Message.of(LangKeys.COMMAND_SHOUT_INVALID_USAGE).send(wrapper);
            return;
        }

        args.forEach(st -> strBuilder.append(st).append(" "));

        final var shoutMessage = Message.of(LangKeys.SHOUT_FORMAT)
                .placeholder("color", color)
                .placeholder("player", player.getName())
                .placeholder("message", strBuilder.toString())
                .placeholder("team", team == null ? "" : team.getName())
                .asComponent();

        wrapper.as(SBAPlayerWrapper.class).shout(shoutMessage);
    }

}
