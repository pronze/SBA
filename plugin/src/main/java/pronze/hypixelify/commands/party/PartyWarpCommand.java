package pronze.hypixelify.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.lib.lang.LanguageService;

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
                .as(PlayerWrapper.class);

        if (!player.isInParty()) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_IN_PARTY)
                    .send(player);
            return;
        }

        SBAHypixelify
                .getInstance()
                .getPartyManager()
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

                                    Bukkit.getScheduler().runTask(SBAHypixelify.getPluginInstance(), () -> {
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
                                        Main.getInstance().getGameOfPlayer(member.getInstance()).leaveFromGame(member.getInstance());
                                    }
                                    PlayerUtils.teleportPlayer(member.getInstance(), leaderLocation);
                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_LEADER_JOIN_LEAVE)
                                            .send(PlayerMapper.wrapPlayer(member));
                                });
                    }
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR)
                        .send(player));
    }
}
