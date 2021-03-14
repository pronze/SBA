package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyMutedEvent;
import pronze.hypixelify.api.events.SBAPlayerPartyUnmutedEvent;
import pronze.hypixelify.api.party.PartySetting;
import pronze.hypixelify.game.PlayerWrapperImpl;

import static pronze.hypixelify.lib.lang.I.i18n;

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
                            ctx.getSender().sendMessage(i18n("command_party_settings_get_help", false));
                        }))
                .literal("mute")
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapperImpl.class);

                            if (!player.isInParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.notinparty")
                                        .forEach(player::sendMessage);
                                return;
                            }

                            SBAHypixelify
                                    .getPartyManager()
                                    .getPartyOf(player)
                                    .ifPresentOrElse(party -> {
                                        if (!player.equals(party.getPartyLeader())) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.access-denied")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }

                                        if (party.getSettings().getChat() == PartySetting.Chat.UNMUTE) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.already-muted")
                                                    .stream()
                                                    .map(str -> str.replace("{isMuted}", "unmuted"))
                                                    .forEach(player::sendMessage);
                                        }

                                        final var muteEvent = new SBAPlayerPartyMutedEvent(player, party);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(muteEvent);
                                        if (muteEvent.isCancelled()) return;

                                        party.getSettings().setChat(PartySetting.Chat.MUTED);
                                        party.getMembers().forEach(member -> {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.chat-enable-disabled")
                                                    .stream()
                                                    .map(str -> str.replace("{mode}", "muted"))
                                                    .forEach(str -> member.getInstance().sendMessage(str));
                                        });
                                    }, () -> SBAHypixelify
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));
                        }).execute())
                .literal("unmute")
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapperImpl.class);

                            if (!player.isInParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.notinparty")
                                        .forEach(player::sendMessage);
                                return;
                            }

                            SBAHypixelify
                                    .getPartyManager()
                                    .getPartyOf(player)
                                    .ifPresentOrElse(party -> {
                                        if (!player.equals(party.getPartyLeader())) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.access-denied")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }
                                        if (party.getSettings().getChat() == PartySetting.Chat.MUTED) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.already-muted")
                                                    .stream()
                                                    .map(str -> str.replace("{isMuted}", "unmuted"))
                                                    .forEach(player::sendMessage);
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
                                        party.getMembers().forEach(member -> SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.chat-enable-disabled")
                                                .stream()
                                                .map(str -> str.replace("{mode}", "unmuted"))
                                                .forEach(str -> member.getInstance().sendMessage(str)));

                                    }, () -> SBAHypixelify
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));
                        }).execute()));
    }
}