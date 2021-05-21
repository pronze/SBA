package pronze.hypixelify.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.lib.lang.LanguageService;

@Service
public class PartyChatCommand {

    public PartyChatCommand() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party chat")
    private void commandChat(
            final @NotNull Player playerArg
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(PlayerWrapper.class);

        player.setPartyChatEnabled(!player.isPartyChatEnabled());
        LanguageService
                .getInstance()
                .get(MessageKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                .replace("%mode%", player.isPartyChatEnabled() ? "enabled" : "disabled")
                .send(player);
    }
}
