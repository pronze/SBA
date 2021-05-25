package pronze.hypixelify.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;

@Service
public class PartyDebugCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party debug <player>")
    private void commandDebug(
            final @NotNull CommandSender sender,
            final @NotNull @Argument("player") Player playerArg
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(PlayerWrapper.class);

        SBAHypixelify
                .getInstance()
                .getPartyManager()
                .getPartyOf(player)
                .ifPresentOrElse(party -> sender.sendMessage(party.toString()), () ->
                        sender.sendMessage("This user is not in a party!"));
    }
}
