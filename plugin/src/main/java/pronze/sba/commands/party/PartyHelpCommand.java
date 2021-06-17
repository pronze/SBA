package pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.sba.MessageKeys;
import pronze.sba.commands.CommandManager;
import pronze.sba.lib.lang.LanguageService;

@Service
public class PartyHelpCommand {

    @OnPostEnable
    public void onPostEnable() {
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
