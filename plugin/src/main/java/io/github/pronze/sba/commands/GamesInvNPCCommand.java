package io.github.pronze.sba.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.game.GameMode;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.service.GamesInventoryService;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

@Service
public class GamesInvNPCCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("sba gamesinv spawnnpc <mode>")
    @CommandPermission("sba.spawnnpc")
    private void commandSpawn(final @NotNull Player player,
                              final @NotNull @Argument("mode") GameMode mode) {
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player);
        if (GamesInventoryService.getInstance().isNPCAtLocation(wrappedPlayer.getLocation())) {
            Message.of(LangKeys.NPC_ALREADY_SPAWNED).send(wrappedPlayer);
            return;
        }

        GamesInventoryService.getInstance().addNPC(mode, player.getLocation());
        GamesInventoryService.getInstance().addViewer(wrappedPlayer);

        Message.of(LangKeys.ADDED_NPC).send(wrappedPlayer);
    }

    @CommandMethod("sba gamesinv removenpc")
    @CommandPermission("sba.removenpc")
    private void commandRemove(final @NotNull Player player) {
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player);

        Message.of(LangKeys.REMOVABLE_NPC_TOGGLE).send(wrappedPlayer);
        GamesInventoryService.getInstance().addEditable(PlayerMapper.wrapPlayer(player));
    }
}
