package io.github.pronze.sba.command;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.SBWAddonAPI;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.util.SBAUtil;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public final class ReloadCommand extends BaseCommand {

    @CommandMethod("sba reload")
    @CommandPermission("sba.admin.reload")
    private void commandReload(@NotNull PlayerWrapper playerWrapper) {
        SBAUtil.reloadPlugin(SBWAddonAPI.getInstance().asJavaPlugin());
        Message.of(LangKeys.RELOADED)
                .send(playerWrapper);
    }
}
