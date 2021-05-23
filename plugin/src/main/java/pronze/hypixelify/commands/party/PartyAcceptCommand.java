package pronze.hypixelify.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteAcceptEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.lib.lang.LanguageService;

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

        final var optionalParty = SBAHypixelify
                .getInstance()
                .getPartyManager()
                .getInvitedPartyOf(player);

        optionalParty.ifPresentOrElse(party -> {
            final var acceptEvent = new SBAPlayerPartyInviteAcceptEvent(player, party);
            SBAHypixelify
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
