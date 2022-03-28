package io.github.pronze.sba.placeholderapi;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.game.tasks.GeneratorTask;
import io.github.pronze.sba.service.PlayerWrapperService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import javax.lang.model.util.ElementScanner14;

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
        %sba_game_tier%
        %sba_game_tiertime%
        %sba_game_teams%
        %sba_game_players%
        %sba_game_time%
        %sba_game_gametime%
        %sba_game_minplayers%
        %sba_game_maxplayers%
    
        %sba_game_<GAME>_status%
        %sba_game_<GAME>_tier%
        %sba_game_<GAME>_tiertime%
        %sba_game_<GAME>_teams%
        %sba_game_<GAME>_players%
        %sba_game_<GAME>_time%
        %sba_game_<GAME>_gametime%
        %sba_game_<GAME>_minplayers%
        %sba_game_<GAME>_maxplayers%
    
        %sba_team_enchant_protection%
        %sba_team_enchant_efficiency%
        %sba_team_enchant_sharpness%
    
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
            if (identifiers.length < 2) return identifier;
            final Game game = identifiers.length == 2 ? Main.getInstance().getGameOfPlayer(player)
                    : Main.getInstance().getGameByName(identifiers[1]);
            int offset = identifiers.length == 2 ? 1 : 2;

            var arena = ArenaManager.getInstance().get(game.getName());
            var generatorTask = arena.isPresent() ? arena.get().getTask(GeneratorTask.class).orElseThrow() : null;

            switch (identifiers[offset]) {
                case "status":
                    return game.getStatus().toString();
                case "tier":
                    if(generatorTask!=null)
                        return generatorTask.getNextTierName().replace("-", " ");
                    else
                        return "N/A";
                case "tiertime":
                        if(generatorTask!=null)
                            return generatorTask.getTimeLeftForNextEvent();
                        else
                            return "N/A";
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
        } else if (identifiers[0].equalsIgnoreCase("team")) {
            final Game game = Main.getInstance().getGameOfPlayer(player);
            final var gameStorage = ArenaManager.getInstance().getGameStorage(game.getName());
            var team = game.getTeamOfPlayer(player);
            if (gameStorage == null)
                return identifier;
            switch (identifiers[1]) {
                case "enchant":
                switch (identifiers[2]) {
                    case "protection":
                        return gameStorage.get().getProtectionLevel(team).orElse(0).toString();
                    case "efficiency":
                        return gameStorage.get().getEfficiencyLevel(team).orElse(0).toString();
                    case "sharpness":
                        return gameStorage.get().getSharpnessLevel(team).orElse(0).toString();
                }
                break;
            }
        } else if (identifiers[0].equalsIgnoreCase("version")) {
            return SBA.getInstance().getVersion();
        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
