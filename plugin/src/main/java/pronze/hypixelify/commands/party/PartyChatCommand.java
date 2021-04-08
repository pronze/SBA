package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.lang.Message;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

public class PartyChatCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyChatCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("chat")
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapper.class);

                            player.setPartyChatEnabled(!player.isPartyChatEnabled());
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                                    .replace("%mode%", player.isPartyChatEnabled() ? "enabled" : "disabled")
                                    .send(player);
                        }).execute()));
    }
}
