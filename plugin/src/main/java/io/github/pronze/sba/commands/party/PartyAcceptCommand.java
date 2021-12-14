package io.github.pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.events.SBAPlayerPartyInviteAcceptEvent;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
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
                .as(SBAPlayerWrapper.class);

      
        final var optionalParty = PartyManager
                .getInstance()
                .getInvitedPartyOf(player);

        optionalParty.ifPresentOrElse(party -> {
            final var acceptEvent = new SBAPlayerPartyInviteAcceptEvent(player, party);
            SBA.getPluginInstance()
                    .getServer()
                    .getPluginManager()
                    .callEvent(acceptEvent);

            if (acceptEvent.isCancelled()) {
                return;
            }

            player.getSettings()
                    .disable(PlayerSetting.INVITED_TO_PARTY)
                    .enable(PlayerSetting.IN_PARTY);

            party.addPlayer(player);

            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_ACCEPTED)
                    .replace("%player%", player.getName())
                    .send(party.getMembers().toArray(SBAPlayerWrapper[]::new));
        }, () -> LanguageService
                .getInstance()
                .get(MessageKeys.PARTY_MESSAGE_NOT_INVITED)
                .send(player));
    }
}
