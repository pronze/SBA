package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyWarpCommand {

    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if (init)
            return;
        if (SBAConfig.getInstance().party().enabled())
            CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("party|p warp")
    @CommandPermission("sba.party")
    private void commandWarp(
            final @NotNull Player playerArg) {
        final var player = SBA.getInstance().getPlayerWrapper((playerArg));

        if (!player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_IN_PARTY)
                    .send(player);
            return;
        }

        PartyManager
                .getInstance()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    if (!player.equals(party.getPartyLeader())) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                .send(player);
                        return;
                    }
                    if (party.getMembers().size() == 1) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_NO_PLAYERS_TO_WARP)
                                .send(player);
                        return;
                    }

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_WARP)
                            .send(player);

                    if (Main.getInstance().isPlayerPlayingAnyGame(playerArg)) {
                        final var game = Main.getInstance().getGameOfPlayer(playerArg);

                        party.getMembers()
                                .stream().filter(member -> !player.equals(member))
                                .forEach(member -> {
                                    final var memberGame = Main.getInstance().getGameOfPlayer(member.getInstance());

                                    Bukkit.getScheduler().runTask(SBA.getPluginInstance(), () -> {
                                        if (player.getInstance() != member.getInstance())
                                            if (game != memberGame) {
                                                if (memberGame != null)
                                                    memberGame.leaveFromGame(member.getInstance());
                                                game.joinToGame(member.getInstance());
                                                LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_WARP)
                                                        .send(member);
                                            }
                                    });
                                });
                    } else {
                        final var leaderLocation = player.getInstance().getLocation();
                        party.getMembers()
                                .stream()
                                .filter(member -> !member.equals(player))
                                .forEach(member -> {
                                    if (Main.getInstance().isPlayerPlayingAnyGame(member.getInstance())) {
                                        Main.getInstance().getGameOfPlayer(member.getInstance())
                                                .leaveFromGame(member.getInstance());
                                    }
                                    PlayerUtils.teleportPlayer(member.getInstance(), leaderLocation);
                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_LEADER_JOIN_LEAVE)
                                            .send(PlayerMapper.wrapPlayer(member.getInstance()));
                                });
                    }
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR)
                        .send(player));
    }
}
