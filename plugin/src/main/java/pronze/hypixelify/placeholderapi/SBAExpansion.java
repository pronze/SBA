package pronze.hypixelify.placeholderapi;

import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.game.PlayerWrapper;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
            final PlayerWrapper database = SBAHypixelify.getWrapperService().getWrapper(player);
            switch (identifier.substring(10).toLowerCase()){
                case "level":
                    return Integer.toString(database.getLevel());
            }
        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
