package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.party.PartySetting;
import io.github.pronze.sba.party.PartySetting.GameMode;
import io.github.pronze.sba.party.PartySetting.Invite;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyMutedEvent;
import io.github.pronze.sba.events.SBAPlayerPartyUnmutedEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartySettingsCommand {

        static boolean init = false;

        @OnPostEnable
        public void onPostEnabled() {
                if (init)
                        return;
                CommandManager.getInstance().getAnnotationParser().parse(this);
                init = true;
        }

        // @CommandMethod("party|p settings")
        // private void commandSettings(
        // final @NotNull Player player
        // ) {
        // LanguageService
        // .getInstance()
        // .get(MessageKeys.COMMAND_PARTY_SETTINGS_GET_HELP)
        // .send(PlayerMapper.wrapPlayer(player));
        // }
        @CommandMethod("party|p stream")
        @CommandPermission("sba.party.public")
        private void commandStream(
                        final @NotNull Player playerArg) {
                commandOpen(playerArg);
        }

        @CommandMethod("stream")
        @CommandPermission("sba.party.public")
        private void commandStream2(
                        final @NotNull Player playerArg) {
                commandOpen(playerArg);
        }

        @CommandMethod("party|p open")
        @CommandPermission("sba.party.public")
        private void commandOpen(
                        final @NotNull Player playerArg) {
                final var player = SBA.getInstance().getPlayerWrapper((playerArg));

                var playerParty = PartyManager
                                .getInstance()
                                .getPartyOf(player);
                if (playerParty.isEmpty())
                        playerParty = PartyManager.getInstance().createParty(player);

                playerParty.ifPresentOrElse(party -> {
                        if (!player.equals(party.getPartyLeader())) {
                                LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                                .send(player);
                                return;
                        }

                        if (party.getSettings().getInvite() == Invite.NONE) {
                                LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_WENT_CLOSED)
                                                .replace("%host%",player.getName())
                                                .send(player);
                                party.getSettings().setInvite(Invite.ALL);
                        } else {
                                LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_WENT_OPEN)
                                                .replace("%host%",player.getName())
                                                .send(player);
                                party.getSettings().setInvite(Invite.NONE);
                        }
                }, () -> LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                .send(player));
        }

        @CommandMethod("party|p private")
        @CommandPermission("sba.party.private")
        private void commandPrivate(
                        final @NotNull Player playerArg) {
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
                                        if (!player.equals(party.getPartyLeader())) {
                                                LanguageService
                                                                .getInstance()
                                                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                                                .send(player);
                                                return;
                                        }

                                        if (party.getSettings().getGamemode() == GameMode.PUBLIC) {
                                                LanguageService
                                                                .getInstance()
                                                                .get(MessageKeys.PARTY_WENT_PRIVATE)
                                                                .replace("%host%", player.getName())
                                                                .send(player);
                                                party.getSettings().setGamemode(GameMode.PRIVATE);
                                        } else {
                                                LanguageService
                                                                .getInstance()
                                                                .get(MessageKeys.PARTY_WENT_PUBLIC)
                                                                .replace("%host%", player.getName())
                                                                .send(player);
                                                party.getSettings().setGamemode(GameMode.PUBLIC);
                                        }
                                }, () -> LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                                .send(player));
        }

        @CommandMethod("party|p mute")
        @CommandPermission("sba.party")
        private void commandMute(
                        final @NotNull Player playerArg) {
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
                                        SBA
                                                        .getPluginInstance()
                                                        .getServer()
                                                        .getPluginManager()
                                                        .callEvent(muteEvent);
                                        if (muteEvent.isCancelled())
                                                return;

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

        @CommandMethod("party|p unmute")
        @CommandPermission("sba.party")
        private void commandUnmute(
                        final @NotNull Player playerArg) {
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
                                        SBA.getPluginInstance()
                                                        .getServer()
                                                        .getPluginManager()
                                                        .callEvent(unmuteEvent);
                                        if (unmuteEvent.isCancelled())
                                                return;

                                        party.getSettings().setChat(PartySetting.Chat.MUTED);
                                        LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                                                        .replace("%mode%", "unmuted")
                                                        .send(party.getMembers().toArray(new SBAPlayerWrapper[0]));
                                }, () -> LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                                .send(player));
        }
}