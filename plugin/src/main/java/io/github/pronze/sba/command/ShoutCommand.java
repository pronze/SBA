package io.github.pronze.sba.command;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.Permission;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.lang.LangKeys;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskBase;
import org.screamingsandals.lib.utils.annotations.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public final class ShoutCommand extends BaseCommand {
    private final SBAConfig config;

    @CommandMethod("shout <args>")
    @CommandDescription("shout command")
    private void commandShout(
            final @NotNull PlayerWrapper playerWrapper,
            final @NotNull @Argument("args") String[] msgContent
    ) {
        if (!config.node("shout", "enabled").getBoolean(true)) {
            //TODO:
            return;
        }

        final var content = List.of(msgContent);

        final var gamePlayer = playerWrapper.as(GamePlayer.class);
        if (!gamePlayer.isInGame()) {
            Message.of(LangKeys.MESSAGE_NOT_IN_GAME).send(gamePlayer);
            return;
        }

        if (gamePlayer.isGameSpectator()) {
            Message.of(LangKeys.COMMAND_DISABLED_FOR_SPECTATORS).send(gamePlayer);
            return;
        }

        if (!gamePlayer.canShout()
                && config.node("shout", "time-out", "enabled").getBoolean(true)) {
            Message.of(LangKeys.MESSAGE_SHOUT_WAIT)
                    .placeholder("seconds", gamePlayer.getShoutCooldown())
                    .send(gamePlayer);
            return;
        }

        final var maybeGameWrapper = gamePlayer.getGame();
        if (maybeGameWrapper.isEmpty()) {
            // probably blacklisted.
            return;
        }

        final var gameWrapper = maybeGameWrapper.get();

        final var bukkitPlayer = gamePlayer.as(Player.class);
        final var game = gameWrapper.getGame();
        final var team = game.getTeamOfPlayer(bukkitPlayer);
        var color = ChatColor.GRAY.toString();
        if (team != null) {
            color = TeamColor.valueOf(team.getColor().name()).chatColor.toString();
        }

        if (content.isEmpty()) {
            Message.of(LangKeys.COMMAND_SHOUT_INVALID_USAGE).send(gamePlayer);
            return;
        }

        final var strBuilder = new StringBuilder();
        content.forEach(word -> strBuilder.append(word).append(" "));
        final var shoutMessage = Message.of(LangKeys.SHOUT_FORMAT)
                .placeholder("color", color)
                .placeholder("player", playerWrapper.getName())
                .placeholder("message", strBuilder.toString())
                .placeholder("team", team == null ? "" : team.getName());

        game.getConnectedPlayers()
                .stream()
                .map(PlayerMapper::wrapPlayer)
                .forEach(shoutMessage::send);

        if (!gamePlayer.hasPermission(Permission.BYPASS_SHOUT.getKey())
                && config.node("shout", "time-out", "enabled").getBoolean(true)) {
            Tasker.build(taskBase -> new ShoutCooldownTask(gamePlayer, taskBase, config.node("shout", "time-out", "time").getInt(60)))
                    .repeat(1L, TaskerTime.SECONDS)
                    .start();
        }
    }

    @RequiredArgsConstructor
    private static final class ShoutCooldownTask implements Runnable {
        private final GamePlayer gamePlayer;
        private final TaskBase taskBase;
        private final int defaultCooldown;

        @Override
        public void run() {
            if (!gamePlayer.isOnline() || gamePlayer.getShoutCooldown() <= 0) {
                gamePlayer.setShoutCooldown(defaultCooldown);
                taskBase.cancel();
                return;
            }
            gamePlayer.setShoutCooldown(gamePlayer.getShoutCooldown() - 1);
        }
    }
}
