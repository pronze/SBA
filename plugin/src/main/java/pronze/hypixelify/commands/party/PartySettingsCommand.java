package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyMutedEvent;
import pronze.hypixelify.api.events.SBAPlayerPartyUnmutedEvent;
import pronze.hypixelify.api.party.PartySetting;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

public class PartySettingsCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartySettingsCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");
        manager.command(builder.literal("settings")
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.COMMAND_PARTY_SETTINGS_GET_HELP)
                                    .send(PlayerMapper.wrapPlayer(ctx.getSender()));
                        }))
                .literal("mute")
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
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
                                                .getInstance()
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
                        }).execute())
                .literal("unmute")
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
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
                                                .getInstance()
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
                        }).execute()));
    }
}