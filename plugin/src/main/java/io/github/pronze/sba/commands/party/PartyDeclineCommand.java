package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.party.PartyManagerImpl;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyInviteDeclineEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;

@Service
public class PartyDeclineCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party decline")
    private void commandDecline(
            final @NotNull Player playerArg
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(SBAPlayerWrapper.class);

        if (!player.getSettings().isToggled(PlayerSetting.INVITED_TO_PARTY)) {
            Message.of(LangKeys.PARTY_MESSAGE_NOT_INVITED).send(player);
            return;
        }

        PartyManagerImpl
                .getInstance()
                .getInvitedPartyOf(player)
                .ifPresentOrElse(party -> {
                    final var partyDeclineEvent = new SBAPlayerPartyInviteDeclineEvent(player, party);
                    SBA.getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(partyDeclineEvent);
                    if (partyDeclineEvent.isCancelled()) {
                        return;
                    }

                    party.removeInvitedPlayer(player);
                    player.getSettings().disable(PlayerSetting.INVITED_TO_PARTY);

                    Message.of(LangKeys.PARTY_MESSAGE_DECLINE_OUTGOING).send(player);

                    Message.of(LangKeys.PARTY_MESSAGE_DECLINE_INCOMING)
                            .placeholder("player", player.getName())
                            .send(party.getMembers());

                    if (party.getMembers().size() == 1) {
                        PartyManagerImpl
                                .getInstance()
                                .disband(party.getUUID());
                    }
                }, () -> SBA
                        .getInstance()
                        .getConfigurator()
                        .getStringList("party.message.error")
                        .forEach(player::sendMessage));
    }
}
