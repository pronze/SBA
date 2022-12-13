package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyInviteDeclineEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyDeclineCommand {

    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if(SBA.isBroken())return;
        if (init)
            return;
        if (SBAConfig.getInstance().party().enabled())
            CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("party|p decline")
    @CommandPermission("sba.party")
    private void commandDecline(
            final @NotNull Player playerArg) {
        final var player = SBA.getInstance().getPlayerWrapper((playerArg));

        if (!player.getSettings().isToggled(PlayerSetting.INVITED_TO_PARTY)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_INVITED)
                    .send(player);
            return;
        }

        PartyManager
                .getInstance()
                .getInvitedPartyOf(player)
                .ifPresentOrElse(party -> {
                    final var partyDeclineEvent = new SBAPlayerPartyInviteDeclineEvent(player, party);
                    SBA
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(partyDeclineEvent);
                    if (partyDeclineEvent.isCancelled()) {
                        return;
                    }

                    party.removeInvitedPlayer(player);
                    player.getSettings().disable(PlayerSetting.INVITED_TO_PARTY);

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_DECLINE_OUTGOING)
                            .send(player);

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_DECLINE_INCOMING)
                            .replace("%player%", player.as(Player.class).getDisplayName() + ChatColor.RESET)
                            .send(party.getMembers().toArray(new SBAPlayerWrapper[0]));

                    if (party.getMembers().size() == 1) {
                        PartyManager
                                .getInstance()
                                .disband(party.getUUID());
                    }
                }, () -> SBA
                        .getInstance()
                        .getConfigurator()
                        .getStringList("party.message.error")
                        .forEach(player::sendMessage));
    }
}
