package io.github.pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.events.SBAPlayerPartyDisbandEvent;
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
public class PartyDisbandCommand {

        static boolean init = false;
        @OnPostEnable
        public void onPostEnabled() {
            if (init)
                return;
            CommandManager.getInstance().getAnnotationParser().parse(this);
            init = true;
        }

        @CommandMethod("party|p disband")
        @CommandPermission("sba.party")
    private void commandDisband(
            final @NotNull Player playerArg
    ) {
        final var player = SBA.getInstance().getPlayerWrapper((playerArg));

        if (!player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_IN_PARTY)
                    .send(player);
            return;
        }

        PartyManager
                .getInstance()
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
                    SBA.getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(disbandEvent);
                    if (disbandEvent.isCancelled()) return;

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_DISBAND)
                            .send(party.getMembers().toArray(SBAPlayerWrapper[]::new));

                    SBA.getInstance()
                            .getPartyManager()
                            .disband(party.getUUID());
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR)
                        .send(player));
    }
}
