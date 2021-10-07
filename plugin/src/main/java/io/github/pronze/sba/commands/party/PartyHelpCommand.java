package io.github.pronze.sba.commands.party;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.commands.CommandManager;

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
        Message.of(LangKeys.PARTY_MESSAGE_HELP).send(PlayerMapper.wrapPlayer(sender));
    }
}
