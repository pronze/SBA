package pronze.hypixelify.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteDeclineEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.lib.lang.LanguageService;

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
                .as(PlayerWrapper.class);

        if (!player.isInvitedToAParty()) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_INVITED)
                    .send(player);
            return;
        }

        SBAHypixelify
                .getInstance()
                .getPartyManager()
                .getInvitedPartyOf(player)
                .ifPresentOrElse(party -> {
                    final var partyDeclineEvent = new SBAPlayerPartyInviteDeclineEvent(player, party);
                    SBAHypixelify
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(partyDeclineEvent);
                    if (partyDeclineEvent.isCancelled()) {
                        return;
                    }

                    party.removeInvitedPlayer(player);
                    player.setInvitedToAParty(false);

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_DECLINE_OUTGOING)
                            .send(player);

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_DECLINE_INCOMING)
                            .replace("%player%", player.getName())
                            .send(party.getMembers().toArray(new PlayerWrapper[0]));

                    if (party.getMembers().size() == 1) {
                        SBAHypixelify
                                .getInstance()
                                .getPartyManager()
                                .disband(party.getUUID());
                    }
                }, () -> SBAHypixelify
                        .getInstance()
                        .getConfigurator()
                        .getStringList("party.message.error")
                        .forEach(player::sendMessage));
    }
}
