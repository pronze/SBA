package io.github.pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.events.SBAPlayerPartyDisbandEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.wrapper.PlayerWrapper;
import io.github.pronze.sba.manager.CommandManager;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyDisbandCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party disband")
    private void commandDisband(
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

        SBA
                .getInstance()
                .getPartyManager()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    if (!party.getPartyLeader().equals(player)) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                .send(player);
                        return;
                    }

                    final var disbandEvent = new SBAPlayerPartyDisbandEvent(player, party);
                    SBA
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(disbandEvent);
                    if (disbandEvent.isCancelled()) return;

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_DISBAND)
                            .send(party.getMembers().toArray(PlayerWrapper[]::new));

                    SBA
                            .getInstance()
                            .getPartyManager()
                            .disband(party.getUUID());
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR)
                        .send(player));
    }
}
