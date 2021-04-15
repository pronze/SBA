package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.lib.lang.LanguageService;

public class PartyHelpCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyHelpCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");
        manager.command(builder.literal("help")
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(ctx -> {
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.PARTY_MESSAGE_HELP)
                                    .send(PlayerMapper.wrapPlayer(ctx.getSender()));
                        }).execute()));
    }
}
