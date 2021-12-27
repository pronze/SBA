package io.github.pronze.sba.command;

import cloud.commandframework.CommandManager;
import io.github.pronze.sba.SBWAddonAPI;
import io.github.pronze.sba.visual.npc.SBANPC;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;
import org.screamingsandals.lib.utils.annotations.parameters.ProvidedBy;

import java.util.List;

@RequiredArgsConstructor
@Service
public final class SuggestionsProvider {

    @ProvidedBy(SBACommandManager.class)
    private final CommandManager<CommandSenderWrapper> commandManager;

    @OnEnable
    public void onPostEnable() {
        commandManager.getParserRegistry().registerSuggestionProvider("action-value", (context, s) -> {
            switch (context.<SBANPC.Action>get("action")) {
                case JOIN_GAME:
                    return Main.getGameNames();
                case OPEN_GAMES_INVENTORY:
                    return SBWAddonAPI.getInstance().getGamesInventory().getInventoryNames();
                default:
                    return List.of();
            }
        });

        commandManager.getParserRegistry().registerSuggestionProvider("inventory-names", (context, s) -> SBWAddonAPI.getInstance().getGamesInventory().getInventoryNames());
    }
}
