package pronze.hypixelify.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.lib.lang.LanguageService;

@Service
public class PartyHelpCommand {

    public PartyHelpCommand() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party help")
    private void commandHelp(
            final @NotNull Player sender
    ) {
        LanguageService
                .getInstance()
                .get(MessageKeys.PARTY_MESSAGE_HELP)
                .send(PlayerMapper.wrapPlayer(sender));
    }

}
