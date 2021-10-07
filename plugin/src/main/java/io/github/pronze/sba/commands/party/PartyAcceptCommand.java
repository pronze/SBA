package io.github.pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.events.SBAPlayerPartyInviteAcceptEvent;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;

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

        if (!player.getSettings().isToggled(PlayerSetting.INVITED_TO_PARTY)) {
            Message.of(LangKeys.PARTY_MESSAGE_NOT_INVITED).send(player);
            return;
        }

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
            Message.of(LangKeys.PARTY_MESSAGE_ACCEPTED)
                    .placeholder("player", player.getName())
                    .send(party.getMembers());
        }, () -> Message.of(LangKeys.PARTY_MESSAGE_ERROR).send(player));
    }
}
