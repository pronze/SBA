package io.github.pronze.sba.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.GameMode;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.service.GamesInventoryService;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.world.LocationMapper;

@Service
public class GamesInvNPCCommand {

    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if (SBA.isBroken())
            return;
        if (init)
            return;
        CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("sba gamesinv spawnnpc <mode>")
    @CommandPermission("sba.spawnnpc")
    private void commandSpawn(final @NotNull Player player,
            final @NotNull @Argument(value = "mode", suggestions = "gameMode") String mode) {
        /*
         * if (GamesInventoryService.getInstance().isNPCAtLocation(LocationMapper.
         * wrapLocation(player.getLocation()))) {
         * LanguageService
         * .getInstance()
         * .get(MessageKeys.NPC_ALREADY_SPAWNED)
         * .send(PlayerMapper.wrapPlayer(player));
         * return;
         * }
         */

        GamesInventoryService.getInstance().addNPC(mode, player.getLocation());
        GamesInventoryService.getInstance().addViewer(player);
        LanguageService
                .getInstance()
                .get(MessageKeys.ADDED_NPC)
                .send(PlayerMapper.wrapPlayer(player));
    }

    @CommandMethod("sba gamesinv removenpc")
    @CommandPermission("sba.removenpc")
    private void commandRemove(final @NotNull Player player) {
        LanguageService
                .getInstance()
                .get(MessageKeys.REMOVABLE_NPC_TOGGLE)
                .send(PlayerMapper.wrapPlayer(player));
        GamesInventoryService.getInstance().addEditable(PlayerMapper.wrapPlayer(player),
                GamesInventoryService.Action.Remove, null);
    }

    @CommandMethod("sba gamesinv editnpc <skin>")
    @CommandPermission("sba.editnpc")
    private void commandEdit(final @NotNull Player player, final @NotNull @Argument("skin") String skin) {
        LanguageService
                .getInstance()
                .get(MessageKeys.SKIN_NPC_TOGGLE)
                .send(PlayerMapper.wrapPlayer(player));
        GamesInventoryService.getInstance().addEditable(PlayerMapper.wrapPlayer(player),
                GamesInventoryService.Action.Skin, skin);
    }
}