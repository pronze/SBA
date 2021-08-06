package io.github.pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.events.SBAPlayerPartyInviteAcceptEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.wrapper.PlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyAcceptCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party accept")
    private void commandAccept(
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

        final var optionalParty = SBA
                .getInstance()
                .getPartyManager()
                .getInvitedPartyOf(player);

        optionalParty.ifPresentOrElse(party -> {
            final var acceptEvent = new SBAPlayerPartyInviteAcceptEvent(player, party);
            SBA
                    .getPluginInstance()
                    .getServer()
                    .getPluginManager()
                    .callEvent(acceptEvent);
            if (acceptEvent.isCancelled()) {
                return;
            }
            player.setInvitedToAParty(false);
            player.setInParty(true);
            party.addPlayer(player);

            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_ACCEPTED)
                    .replace("%player%", player.getName())
                    .send(party.getMembers().toArray(PlayerWrapper[]::new));
        }, () -> LanguageService
                .getInstance()
                .get(MessageKeys.PARTY_MESSAGE_ERROR)
                .send(player));
    }
}
