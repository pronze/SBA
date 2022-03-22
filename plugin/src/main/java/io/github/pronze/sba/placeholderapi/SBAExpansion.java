package io.github.pronze.sba.placeholderapi;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.service.PlayerWrapperService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;

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
        return SBA.getInstance().getVersion();
    }

    /*
        List of placeholders
    
        %sba_player_level%
        %sba_player_xp%
        %sba_player_progress%
    
        %sba_game_status%
        %sba_game_teams%
        %sba_game_players%
        %sba_game_time%
        %sba_game_gametime%
        %sba_game_minplayers%
        %sba_game_maxplayers%

        %sba_game_<GAME>_status%
        %sba_game_<GAME>_teams%
        %sba_game_<GAME>_players%
        %sba_game_<GAME>_time%
        %sba_game_<GAME>_gametime%
        %sba_game_<GAME>_minplayers%
        %sba_game_<GAME>_maxplayers%

        %sba_version%
    */ 
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        Logger.trace("Placeholder '" + identifier + "' was requested.");
        String[] identifiers = identifier.split("_");
        if (identifiers.length <= 1) return null;
        if (identifiers[0].equalsIgnoreCase("player")) {
            if (player == null) {
                return "";
            }
            final SBAPlayerWrapper database = PlayerWrapperService.getInstance().get(player).orElseThrow();
            switch (identifiers[1]) {
                case "level":
                    return Integer.toString(database.getLevel());
                case "xp":
                    return Integer.toString(database.getXP());
                case "progress":
                    return Integer.toString(database.getIntegerProgress());
            }
        } else if (identifiers[0].equalsIgnoreCase("game")) {
            if (identifiers.length < 2) return null;
            final Game game = identifiers.length==2? Main.getInstance().getGameOfPlayer(player) : Main.getInstance().getGameByName(identifiers[1]);
            switch (identifiers.length==2?identifiers[1]:identifiers[2]) {
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
        } else if (identifiers[0].equalsIgnoreCase("version")) {
            return SBA.getInstance().getVersion();
        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
