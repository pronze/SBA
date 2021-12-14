package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyLeaveEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyLeaveCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party leave")
    private void commandLeave(
            final @NotNull Player playerArg
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(SBAPlayerWrapper.class);

        if (!player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_IN_PARTY)
                    .send(player);
            return;
        }

        SBA
                .getInstance()
                .getPartyManager()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    final var event = new SBAPlayerPartyLeaveEvent(player, party);
                    SBA
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(event);
                    if (event.isCancelled()) return;

                    player.getSettings().disable(PlayerSetting.IN_PARTY);
                    party.removePlayer(player);
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_OFFLINE_QUIT)
                            .replace("%player%", player.getName())
                            .send(party.getMembers().toArray(new SBAPlayerWrapper[0]));

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_LEFT)
                            .send(player);

                    if (party.getMembers().size() == 1) {
                        SBA
                                .getInstance()
                                .getPartyManager()
                                .disband(party.getUUID());
                        return;
                    }
                    if (party.getPartyLeader().equals(player)) {
                        party
                                .getMembers()
                                .stream()
                                .findAny()
                                .ifPresentOrElse(member -> {
                                    party.setPartyLeader(member);
                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_PROMOTED_LEADER)
                                            .replace("%player%", member.getName())
                                            .send(player);
                                }, () -> SBA
                                        .getInstance()
                                        .getPartyManager()
                                        .disband(party.getUUID()));
                    }
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_OFFLINE_LEFT)
                            .replace("%player%", player.getName())
                            .send(party.getMembers().stream().filter(member -> !player.equals(member)).toArray(SBAPlayerWrapper[]::new));
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR));
    }

}
