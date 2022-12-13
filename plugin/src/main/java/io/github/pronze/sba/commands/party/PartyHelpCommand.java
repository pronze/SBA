package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyHelpCommand {

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

    @CommandMethod("party|p help")
    @CommandPermission("sba.party")
    private void commandHelp(
            final @NotNull Player sender) {
        LanguageService
                .getInstance()
                .get(MessageKeys.PARTY_MESSAGE_HELP)
                .send(PlayerMapper.wrapPlayer(sender));
    }

}
