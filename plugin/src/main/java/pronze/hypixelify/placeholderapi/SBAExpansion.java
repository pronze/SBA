package pronze.hypixelify.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.service.PlayerWrapperService;
import pronze.lib.core.annotations.AutoInitialize;

@AutoInitialize
public class SBAExpansion extends PlaceholderExpansion {

    public SBAExpansion() {
        register();
    }

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
            final var playerWrapper = PlayerWrapperService.getInstance().get(player).orElseThrow();
            switch (identifier.substring(10).toLowerCase()) {
                case "level":
                    return Integer.toString(playerWrapper.getLevel());
                case "xp":
                    return Integer.toString(playerWrapper.getXP());
                case "progress":
                    return playerWrapper.getStringProgress();
                case "shout_timeout":
                    return Integer.toString(playerWrapper.getShoutTimeOut());
            }
        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
