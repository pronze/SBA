package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.party.PartyManagerImpl;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;

@Service
public class PartyWarpCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party warp")
    private void commandWarp(
            final @NotNull Player playerArg
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(SBAPlayerWrapper.class);

        if (!player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            Message.of(LangKeys.PARTY_MESSAGE_NOT_IN_PARTY).send(player);
            return;
        }

        PartyManagerImpl
                .getInstance()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    if (!player.equals(party.getPartyLeader())) {
                       Message.of(LangKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                .send(player);
                        return;
                    }
                    if (party.getMembers().size() == 1) {
                        Message.of(LangKeys.PARTY_MESSAGE_NO_PLAYERS_TO_WARP).send(player);
                        return;
                    }

                    Message.of(LangKeys.PARTY_MESSAGE_WARP).send(player);

                    if (Main.getInstance().isPlayerPlayingAnyGame(playerArg)) {
                        final var game = Main.getInstance().getGameOfPlayer(playerArg);

                        party.getMembers()
                                .stream().filter(member -> !player.equals(member))
                                .forEach(member -> {
                                    final var memberGame = Main.getInstance().getGameOfPlayer(member.getInstance());

                                    Bukkit.getScheduler().runTask(SBA.getPluginInstance(), () -> {
                                        if (game != memberGame) {
                                            if (memberGame != null)
                                                memberGame.leaveFromGame(member.getInstance());
                                            game.joinToGame(member.getInstance());
                                            Message.of(LangKeys.PARTY_MESSAGE_WARP).send(member);
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
                                        Main.getInstance().getGameOfPlayer(member.getInstance()).leaveFromGame(member.getInstance());
                                    }
                                    PlayerUtils.teleportPlayer(member.getInstance(), leaderLocation);
                                    Message.of(LangKeys.PARTY_MESSAGE_LEADER_JOIN_LEAVE)
                                            .send(PlayerMapper.wrapPlayer(member.getInstance()));
                                });
                    }
                }, () -> Message.of(LangKeys.PARTY_MESSAGE_ERROR).send(player));
    }
}
