package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.party.PartyManagerImpl;
import io.github.pronze.sba.party.PartySetting;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyMutedEvent;
import io.github.pronze.sba.events.SBAPlayerPartyUnmutedEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;

@Service
public class PartySettingsCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party settings")
    private void commandSettings(
            final @NotNull Player player
    ) {
        Message.of(LangKeys.COMMAND_PARTY_SETTINGS_GET_HELP)
                .send(PlayerMapper.wrapPlayer(player));
    }

    @CommandMethod("party mute")
    private void commandMute(
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
                    if (!player.equals(party.getPartyLeader())) {
                        Message.of(LangKeys.PARTY_MESSAGE_ACCESS_DENIED).send(player);
                        return;
                    }

                    if (party.getSettings().getChat() == PartySetting.Chat.UNMUTE) {
                        Message.of(LangKeys.PARTY_MESSAGE_ALREADY_MUTED)
                                .placeholder("isMuted", "unmuted")
                                .send(player);
                    }

                    final var muteEvent = new SBAPlayerPartyMutedEvent(player, party);
                    SBA.getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(muteEvent);
                    if (muteEvent.isCancelled()) return;

                    party.getSettings().setChat(PartySetting.Chat.MUTED);
                    party.getMembers().forEach(member -> Message.of(LangKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                            .placeholder("mode", "muted")
                            .send(member));
                }, () -> Message.of(LangKeys.PARTY_MESSAGE_ERROR).send(player));
    }

    @CommandMethod("party unmute")
    private void commandUnmute(
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
                    if (!player.equals(party.getPartyLeader())) {
                        Message.of(LangKeys.PARTY_MESSAGE_ACCESS_DENIED).send(player);
                        return;
                    }

                    if (party.getSettings().getChat() == PartySetting.Chat.MUTED) {
                        Message.of(LangKeys.PARTY_MESSAGE_ALREADY_MUTED)
                                .placeholder("isMuted", "unmuted")
                                .send(player);
                        return;
                    }

                    final var unmuteEvent = new SBAPlayerPartyUnmutedEvent(player, party);
                    SBA.getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(unmuteEvent);
                    if (unmuteEvent.isCancelled()) return;

                    party.getSettings().setChat(PartySetting.Chat.MUTED);
                    Message.of(LangKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                            .placeholder("mode", "unmuted")
                            .send(party.getMembers());
                }, () -> Message.of(LangKeys.PARTY_MESSAGE_ERROR).send(player));
    }
}