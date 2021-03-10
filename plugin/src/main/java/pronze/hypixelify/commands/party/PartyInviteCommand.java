package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.parsers.PlayerArgument;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteEvent;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.game.PlayerWrapperImpl;
import pronze.hypixelify.party.PartyImpl;

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
                .argument(PlayerArgument.ofType(Player.class, "args"))
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .synchronous(ctx -> {
                            final var invitedPlayer = PlayerMapper
                                    .wrapPlayer((Player) ctx.get("args"))
                                    .as(PlayerWrapperImpl.class);

                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapperImpl.class);

                            if (invitedPlayer.isInvitedToAParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.cannotinvite")
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
                                    .getPartyManager()
                                    .getOrCreate(player)
                                    .ifPresent(party -> {
                                        final var inviteEvent = new SBAPlayerPartyInviteEvent(player, invitedPlayer);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(inviteEvent);
                                        if (inviteEvent.isCancelled()) return;

                                        party.invitePlayer(invitedPlayer);
                                    });
                        }).execute()));
    }
}
