package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.parsers.PlayerArgument;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;

import java.util.stream.Collectors;

public class PartyInviteCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyInviteCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("invite")
                .senderType(Player.class)
                .argument(PlayerArgument.of("party-participant"))
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var invitedPlayer = PlayerMapper
                                    .wrapPlayer((Player) ctx.get("party-participant"))
                                    .as(PlayerWrapper.class);

                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapper.class);

                            if (invitedPlayer.equals(player)) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PARTY_MESSAGE_CANNOT_INVITE_YOURSELF)
                                        .send(player);
                                return;
                            }
                            if (invitedPlayer.isInvitedToAParty()) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PARTY_MESSAGE_ALREADY_INVITED)
                                        .send(player);
                                return;
                            }

                            if (invitedPlayer.isInParty()) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PARTY_MESSAGE_CANNOT_INVITE)
                                        .send(player);
                                return;
                            }

                            SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getOrCreate(player)
                                    .ifPresent(party -> {
                                        if (party.getInvitedPlayers().size() > 5) {
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_MAX_INVITE_SIZE_REACHED)
                                                    .send(player);
                                            return;
                                        }

                                        if ((party.getMembers().size() + party.getInvitedPlayers().size())
                                                > SBAConfig.getInstance().getInt("party.size", 4)) {
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_MAX_INVITE_SIZE_REACHED)
                                                    .send(player);
                                            return;
                                        }

                                        final var inviteEvent = new SBAPlayerPartyInviteEvent(player, invitedPlayer);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(inviteEvent);
                                        if (inviteEvent.isCancelled()) return;

                                        party.invitePlayer(invitedPlayer, player);

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_INVITE_SENT)
                                                .replace("%player%", invitedPlayer.getName())
                                                .send(player);

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_INVITE_RECEIVED)
                                                .replace("%player%", player.getName())
                                                .send(invitedPlayer);
                                    });
                        }).execute()));
    }
}
