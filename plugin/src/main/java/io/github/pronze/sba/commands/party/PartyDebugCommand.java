package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.party.PartyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;

@Service
public class PartyDebugCommand {

    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if(SBA.isBroken())return;
        if (init)
            return;
        if (SBAConfig.getInstance().party().enabled())
            CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("party|p debug <player>")
    @CommandPermission("sba.party")
    private void commandDebug(
            final @NotNull CommandSender sender,
            final @NotNull @Argument("player") Player playerArg) {
        final var player = SBA.getInstance().getPlayerWrapper((playerArg));

        PartyManager
                .getInstance()
                .getPartyOf(player)
                .ifPresentOrElse(party -> sender.sendMessage(party.toString()),
                        () -> sender.sendMessage("This user is not in a party!"));
    }
}
