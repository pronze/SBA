package io.github.pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.events.SBAPlayerPartyDisbandEvent;
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
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;

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
                .as(SBAPlayerWrapper.class);

        if (!player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            Message.of(LangKeys.PARTY_MESSAGE_NOT_IN_PARTY).send(player);
            return;
        }

        PartyManagerImpl
                .getInstance()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    if (!party.getPartyLeader().equals(player)) {
                        Message.of(LangKeys.PARTY_MESSAGE_ACCESS_DENIED).send(player);
                        return;
                    }

                    final var disbandEvent = new SBAPlayerPartyDisbandEvent(player, party);
                    SBA.getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(disbandEvent);
                    if (disbandEvent.isCancelled()) {
                        return;
                    }

                    Message.of(LangKeys.PARTY_MESSAGE_DISBAND).send(party.getMembers());

                    SBA.getInstance()
                            .getPartyManager()
                            .disband(party.getUUID());
                }, () -> Message.of(LangKeys.PARTY_MESSAGE_ERROR).send(player));
    }
}
