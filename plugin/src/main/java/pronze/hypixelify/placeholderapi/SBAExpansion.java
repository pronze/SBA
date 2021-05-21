package pronze.hypixelify.placeholderapi;

import pronze.hypixelify.SBAHypixelify;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.service.PlayerWrapperService;

public class SBAExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "sba";
    }

    @Override
    public @NotNull String getAuthor() {
        return "pronze";
    }

    @Override
    public @NotNull String getVersion() {
        return SBAHypixelify.getInstance().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {

        if (player == null) {
            return " ";
        }

        if (identifier.startsWith("player_")) {
            final PlayerWrapper database = PlayerWrapperService.getInstance().get(player).get();
            switch (identifier.substring(10).toLowerCase()){
                case "level":
                    return Integer.toString(database.getLevel());
            }
        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
