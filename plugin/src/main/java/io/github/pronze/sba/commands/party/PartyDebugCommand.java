package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.party.PartyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;

@Service
public class PartyDebugCommand {

    static boolean init = false;
    @OnPostEnable
    public void onPostEnabled() {
        if (init)
            return;
        CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("party|p debug <player>")
    private void commandDebug(
            final @NotNull CommandSender sender,
            final @NotNull @Argument("player") Player playerArg
    ) {
        final var player = SBA.getInstance().getPlayerWrapper((playerArg));

        PartyManager
                .getInstance()
                .getPartyOf(player)
                .ifPresentOrElse(party -> sender.sendMessage(party.toString()), () ->
                        sender.sendMessage("This user is not in a party!"));
    }
}
