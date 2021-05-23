package pronze.hypixelify.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;

@Service
public class PartyInviteCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party invite [invitee]")
    private void commandInvite(
            final @NotNull Player playerArg,
            final @NotNull @Argument("invitee") Player invitee
    ) {
        final var invitedPlayer = PlayerMapper
                .wrapPlayer(invitee)
                .as(PlayerWrapper.class);

        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(PlayerWrapper.class);

        if (invitedPlayer.equals(player)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_CANNOT_INVITE_YOURSELF)
                    .send(player);
            return;
        }
        if (invitedPlayer.isInvitedToAParty()) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_ALREADY_INVITED)
                    .send(player);
            return;
        }

        if (invitedPlayer.isInParty()) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_CANNOT_INVITE)
                    .send(player);
            return;
        }

        SBAHypixelify
                .getInstance()
                .getPartyManager()
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
                    SBAHypixelify
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
