package pronze.hypixelify.placeholderapi;

import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.SBAHypixelify;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.game.GameStorage;
import pronze.hypixelify.api.manager.IArenaManager;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.service.PlayerWrapperService;

import java.util.Arrays;

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
        String[] identifiers = identifier.split("_", 0);
        if(identifiers.length <= 1) return null;

        if (identifiers[0].equalsIgnoreCase("player")) {
            final PlayerWrapper database = PlayerWrapperService.getInstance().get(player).get();
            switch (identifiers[1]){
                case "level":
                    return Integer.toString(database.getLevel());
                case "xp":
                    return Integer.toString(database.getXP());
                case "progress":
                    return Integer.toString(database.getIntegerProgress());
            }
        }else if(identifiers[0].equalsIgnoreCase("game")) {
            if(identifiers.length <=2) return null;
            final Game game = Main.getInstance().getGameByName(identifiers[1]);
            switch (identifiers[2]){
                case "status":
                    return game.getStatus().toString();
                case "teams":
                    return Integer.toString(game.countRunningTeams());
                case "players":
                    return Integer.toString(game.countConnectedPlayers());
                case "time":
                    return Integer.toString(game.getArenaTime().time);
                case "gametime":
                    return Integer.toString(game.getGameTime());
                case "minplayers":
                    return Integer.toString(game.getMinPlayers());
                case "maxplayers":
                    return Integer.toString(game.getMaxPlayers());
            }
        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
