package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.parsers.PlayerArgument;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.game.PlayerWrapperImpl;

public class PartyDebugCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyDebugCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("debug")
                .permission("party.debug")
                .argument(PlayerArgument.of("party-participant"))
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer(ctx.get("party-participant"))
                                    .as(PlayerWrapperImpl.class);

                            final var sender = (Player)ctx.getSender();

                            SBAHypixelify
                                    .getPartyManager()
                                    .getPartyOf(player)
                                    .ifPresentOrElse(party -> sender.sendMessage(party.toString()), () ->
                                            sender.sendMessage("This user is not in a party!"));
                        }).execute()));
    }
}
