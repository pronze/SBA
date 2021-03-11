package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
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
                            final var sender = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapperImpl.class);

                            if (!sender.isInvitedToAParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.not-invited")
                                        .forEach(sender::sendMessage);
                                return;
                            }

                            final var optionalParty = SBAHypixelify
                                    .getPartyManager()
                                    .getInvitedPartyOf(sender);

                            optionalParty.ifPresentOrElse(party -> {
                                party.addPlayer(sender);
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.accepted")
                                        .stream()
                                        .map(str -> str.replace("{player}", sender.getName()))
                                        .forEach(str -> party
                                                .getMembers()
                                                .forEach(member -> member.getInstance().sendMessage(str)));
                            }, () -> SBAHypixelify
                                    .getConfigurator()
                                    .getStringList("party.message.error")
                                    .forEach(sender::sendMessage));
                        }).execute()));
    }
}
