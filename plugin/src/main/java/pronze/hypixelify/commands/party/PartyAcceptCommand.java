package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteAcceptEvent;
import pronze.hypixelify.game.PlayerWrapperImpl;

public class PartyAcceptCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyAcceptCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("accept")
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapperImpl.class);

                            if (!player.isInvitedToAParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.not-invited")
                                        .forEach(player::sendMessage);
                                return;
                            }

                            final var optionalParty = SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getInvitedPartyOf(player);

                            optionalParty.ifPresentOrElse(party -> {
                                final var acceptEvent = new SBAPlayerPartyInviteAcceptEvent(player, party);
                                SBAHypixelify.getInstance()
                                        .getServer()
                                        .getPluginManager()
                                        .callEvent(acceptEvent);
                                if (acceptEvent.isCancelled()) {
                                    return;
                                }
                                player.setInvitedToAParty(false);
                                player.setInParty(true);
                                party.addPlayer(player);
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.accepted")
                                        .stream()
                                        .map(str -> str.replace("{player}", player.getName()))
                                        .forEach(str -> party
                                                .getMembers()
                                                .forEach(member -> member.getInstance().sendMessage(str)));
                            }, () -> SBAHypixelify
                                    .getConfigurator()
                                    .getStringList("party.message.error")
                                    .forEach(player::sendMessage));
                        }).execute()));
    }
}
