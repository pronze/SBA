package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyDisbandEvent;
import pronze.hypixelify.game.PlayerWrapperImpl;

public class PartyDisbandCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyDisbandCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("disband")
                .senderType(Player.class)
                .handler(context -> manager.
                        taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player)ctx.getSender())
                                    .as(PlayerWrapperImpl.class);

                            if (!player.isInParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.notinparty")
                                        .forEach(player::sendMessage);
                                return;
                            }

                            SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getPartyOf(player)
                                    .ifPresentOrElse(party -> {
                                        if (!party.getPartyLeader().equals(player)) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.access-denied")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }

                                        final var disbandEvent = new SBAPlayerPartyDisbandEvent(player, party);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(disbandEvent);
                                        if (disbandEvent.isCancelled()) return;

                                        SBAHypixelify
                                                .getInstance()
                                                .getPartyManager()
                                                .disband(party.getUUID());
                                    }, () -> SBAHypixelify
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));
                        }).execute()));
    }
}
