package pronze.hypixelify.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.SBAHypixelify;

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
            final var wrapperOptional = SBAHypixelify.getInstance().getPlayerWrapperService().get(player);
            if (wrapperOptional.isPresent()) {
                final var database = wrapperOptional.get();
                switch (identifier.substring(10).toLowerCase()) {
                    case "level":
                        return Integer.toString(database.getLevel());
                }
            }

        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
