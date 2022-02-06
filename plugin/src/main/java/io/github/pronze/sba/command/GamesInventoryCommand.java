package io.github.pronze.sba.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.SBWAddonAPI;
import io.github.pronze.sba.lang.LangKeys;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public final class GamesInventoryCommand extends BaseCommand {

    @CommandMethod("sba gamesinv open <inventory>")
    private void commandOpen(@NotNull PlayerWrapper playerWrapper,
                             @NotNull @Argument(value = "inventory", suggestions = "inventory-names") String inventoryName) {
        if (!SBWAddonAPI.getInstance().getGamesInventory().openForPlayer(playerWrapper, inventoryName)) {
            playerWrapper.sendMessage(
                    Message.of(LangKeys.GAMES_INVENTORY_UNKNOWN_INVENTORY)
                            .defaultPrefix()
                            .placeholder("type", inventoryName)
            );
        }
    }
}
