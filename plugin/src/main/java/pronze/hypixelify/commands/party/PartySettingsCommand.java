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
import pronze.hypixelify.api.events.SBAPlayerPartyMutedEvent;
import pronze.hypixelify.api.events.SBAPlayerPartyUnmutedEvent;
import pronze.hypixelify.api.party.PartySetting;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.lib.lang.LanguageService;

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
        LanguageService
                .getInstance()
                .get(MessageKeys.COMMAND_PARTY_SETTINGS_GET_HELP)
                .send(PlayerMapper.wrapPlayer(player));
    }

    @CommandMethod("party mute")
    private void commandMute(
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

        SBAHypixelify
                .getInstance()
                .getPartyManager()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    if (!player.equals(party.getPartyLeader())) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                .send(player);
                        return;
                    }

                    if (party.getSettings().getChat() == PartySetting.Chat.UNMUTE) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ALREADY_MUTED)
                                .replace("%isMuted%", "unmuted")
                                .send(player);
                    }

                    final var muteEvent = new SBAPlayerPartyMutedEvent(player, party);
                    SBAHypixelify
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(muteEvent);
                    if (muteEvent.isCancelled()) return;

                    party.getSettings().setChat(PartySetting.Chat.MUTED);
                    party.getMembers().forEach(member -> LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                            .replace("%mode%", "muted")
                            .send(member));
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR)
                        .send(player));
    }

    @CommandMethod("party unmute")
    private void commandUnmute(
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

        SBAHypixelify
                .getInstance()
                .getPartyManager()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    if (!player.equals(party.getPartyLeader())) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                .send(player);
                        return;
                    }

                    if (party.getSettings().getChat() == PartySetting.Chat.MUTED) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ALREADY_MUTED)
                                .replace("%isMuted%", "unmuted")
                                .send(player);
                        return;
                    }

                    final var unmuteEvent = new SBAPlayerPartyUnmutedEvent(player, party);
                    SBAHypixelify
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(unmuteEvent);
                    if (unmuteEvent.isCancelled()) return;

                    party.getSettings().setChat(PartySetting.Chat.MUTED);
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                            .replace("%mode%", "unmuted")
                            .send(party.getMembers().toArray(new PlayerWrapper[0]));
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR)
                        .send(player));
    }
}