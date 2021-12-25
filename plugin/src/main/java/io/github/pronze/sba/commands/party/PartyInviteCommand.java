package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyInviteEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyInviteCommand {

        static boolean init = false;
        @OnPostEnable
        public void onPostEnabled() {
            if (init)
                return;
            CommandManager.getInstance().getAnnotationParser().parse(this);
            init = true;
        }

    @CommandMethod("party|p invite <invitee>")
    private void commandInvite(
            final @NotNull Player playerArg,
            final @NotNull @Argument("invitee") Player invitee
    ) {
        final var invitedPlayer = SBA.getInstance().getPlayerWrapper((invitee));

        final var player = SBA.getInstance().getPlayerWrapper((playerArg));

        if (invitedPlayer.equals(player)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_CANNOT_INVITE_YOURSELF)
                    .send(player);
            return;
        }
        if (invitedPlayer.getSettings().isToggled(PlayerSetting.INVITED_TO_PARTY)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_ALREADY_INVITED)
                    .send(player);
            return;
        }

        if (invitedPlayer.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_CANNOT_INVITE)
                    .send(player);
            return;
        }

        PartyManager
                .getInstance()
                .getOrCreate(player)
                .ifPresent(party -> {
                    if (party.getInvitedPlayers().size() > 5) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_MAX_INVITE_SIZE_REACHED)
                                .send(player);
                        return;
                    }

                    if ((party.getMembers().size() + party.getInvitedPlayers().size())
                            > SBAConfig.getInstance().getInt("party.size", 4)) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_MAX_INVITE_SIZE_REACHED)
                                .send(player);
                        return;
                    }

                    final var inviteEvent = new SBAPlayerPartyInviteEvent(player, invitedPlayer);
                    SBA
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(inviteEvent);
                    if (inviteEvent.isCancelled()) return;

                    party.invitePlayer(invitedPlayer, player);

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_INVITE_SENT)
                            .replace("%player%", invitedPlayer.getName())
                            .send(player);

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_INVITE_RECEIVED)
                            .replace("%player%", player.getName())
                            .send(invitedPlayer);
                });
    }

}
