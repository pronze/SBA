package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.parsers.PlayerArgument;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteEvent;
import pronze.hypixelify.game.PlayerWrapperImpl;

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
                                    .as(PlayerWrapperImpl.class);

                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapperImpl.class);

                            if (invitedPlayer.equals(player)) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.cannot-invite-yourself")
                                        .forEach(player::sendMessage);
                                return;
                            }
                            if (invitedPlayer.isInvitedToAParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.alreadyInvited")
                                        .forEach(player::sendMessage);
                                return;
                            }

                            if (invitedPlayer.isInParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.cannotinvite")
                                        .forEach(player::sendMessage);
                                return;
                            }

                            SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getOrCreate(player)
                                    .ifPresent(party -> {
                                        if (party.getInvitedPlayers().size() > 5) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.max-invite-size")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }

                                        if ((party.getMembers().size() + party.getInvitedPlayers().size())
                                                > SBAHypixelify.getConfigurator().config.getInt("party.size")) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.max-size")
                                                    .forEach(player::sendMessage);
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

                                        SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.invited")
                                                .stream()
                                                .map(str -> str.replace("{player}", invitedPlayer.getName()))
                                                .collect(Collectors.toList())
                                                .forEach(player::sendMessage);

                                        SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.invite")
                                                .stream()
                                                .map(str -> str.replace("{player}", player.getName()))
                                                .collect(Collectors.toList())
                                                .forEach(invitedPlayer::sendMessage);
                                    });
                        }).execute()));
    }
}
