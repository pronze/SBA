package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;

@Service
public class PartyChatCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party chat")
    private void commandChat(
            final @NotNull Player playerArg
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(SBAPlayerWrapper.class);

        player.getSettings().toggle(PlayerSetting.PARTY_CHAT_ENABLED);

        Message.of(LangKeys.PARTY_MESSAGE_CHAT_ENABLED_OR_DISABLED)
                .placeholder("mode", player.getSettings().isToggled(PlayerSetting.PARTY_CHAT_ENABLED) ? "enabled" : "disabled")
                .send(player);
    }
}
