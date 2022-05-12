package io.github.pronze.sba.visuals;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.DateUtils;
import io.github.pronze.sba.utils.Logger;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.SBA;
import io.github.pronze.lib.pronzelib.scoreboards.Scoreboard;
import io.github.pronze.lib.pronzelib.scoreboards.ScoreboardManager;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LobbyScoreboardManager implements Listener {
    private final Map<UUID, Scoreboard> scoreboardMap = new HashMap<>();

    public static LobbyScoreboardManager getInstance() {
        return ServiceManager.get(LobbyScoreboardManager.class);
    }

    @OnPostEnable
    public void registerListener() {
        if (!SBAConfig.getInstance().node("lobby-scoreboard", "enabled").getBoolean(true)) {
            return;
        }
        SBA.getInstance().registerListener(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        if(e.getGame().getConnectedPlayers().contains(player))
            if (e.getGame().getStatus() == GameStatus.WAITING) {
                Bukkit.getScheduler().runTaskLater(SBA.getPluginInstance(), () -> createBoard(player, e.getGame()), 3L);
            }
    }

    private void createBoard(Player player, Game game) {
        Logger.trace("Creating board for player: {}", player.getName());

        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);

        var animatedTitle = LanguageService
                .getInstance()
                .get(MessageKeys.ANIMATED_BEDWARS_TITLE)
                .toStringList();

        final var scoreboard = Scoreboard.builder()
                .animate(true)
                .player(player)
                .displayObjective("bwa-lobby")
                .updateInterval(20L)
                .animationInterval(2L)
                .animatedTitle(animatedTitle)
                .updateCallback(board -> {
                    board.setLines(process(player, game, board));
                    return true;
                })
                .build();
        scoreboardMap.put(player.getUniqueId(), scoreboard);
    }

    @EventHandler
    public void onPlayerLeave(BedwarsPlayerLeaveEvent e) {
        remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    private void remove(Player player) {
        if (scoreboardMap.containsKey(player.getUniqueId())) {
            final var scoreboard = scoreboardMap.get(player.getUniqueId());
            if (scoreboard != null) {
                scoreboard.destroy();
                scoreboardMap.remove(player.getUniqueId());
            }
        }
    }

    private List<String> process(Player player, Game game, Scoreboard scoreboard) {
        final var lines = new ArrayList<String>();
        String state = LanguageService
                .getInstance()
                .get(MessageKeys.LOBBY_SCOREBOARD_STATE_WAITING)
                .toString();

        int needplayers = game.getMinPlayers() - game.getConnectedPlayers().size();
        needplayers = Math.max(needplayers, 0);
        // int s = SBAConfig.game_size.getOrDefault(game.getName(), 4);
        String mode;

        int s = game.getAvailableTeams().get(0).getMaxPlayers();

        if (game.getAvailableTeams().stream().allMatch(t -> t.getMaxPlayers() == 1)) {
            mode = LanguageService
                    .getInstance()
                    .get(MessageKeys.LOBBY_SCOREBOARD_SOLO_PREFIX)
                    .toString();
        }
        else if (game.getAvailableTeams().stream().allMatch(t -> t.getMaxPlayers() == 2)) {
            mode = LanguageService
                    .getInstance()
                    .get(MessageKeys.LOBBY_SCOREBOARD_DOUBLES_PREFIX)
                    .toString();
        }
        else if (game.getAvailableTeams().stream().allMatch(t -> t.getMaxPlayers() == 3)) {
            mode = LanguageService
                    .getInstance()
                    .get(MessageKeys.LOBBY_SCOREBOARD_TRIPLES_PREFIX)
                    .toString();
        }
        else if (game.getAvailableTeams().stream().allMatch(t -> t.getMaxPlayers() == 4)) {
            mode = LanguageService
                    .getInstance()
                    .get(MessageKeys.LOBBY_SCOREBOARD_SQUADS_PREFIX)
                    .toString();
        } else {
            List<String> teamSize = game.getAvailableTeams().stream().map(m -> m.getMaxPlayers()).map(String::valueOf)
                    .collect(Collectors.toList());
            mode = String.join("v", teamSize);
        }
        // mode = s + "v" + s + "v" + s + "v" + s;

        if (game.countConnectedPlayers() >= game.getMinPlayers()
                && game.getStatus() == GameStatus.WAITING) {
            final var gameImpl = ((org.screamingsandals.bedwars.game.Game) game);
            final var time = gameImpl.getFormattedTimeLeft();
            if (!time.contains("0-1")) {
                final var units = time.split(":");
                var seconds = Integer.parseInt(units[1]) + 1 + Integer.parseInt(units[0])*60;
                state = LanguageService
                        .getInstance()
                        .get(MessageKeys.LOBBY_SCOREBOARD_STATE)
                        .replace("%countdown%", seconds<=60? String.valueOf(seconds):gameImpl.getFormattedTimeLeft())
                        .toString();
            }
        }

        final var finalState = state;
        final var finalNeedplayers = needplayers;

        var lobbyScoreboardLines = LanguageService
                .getInstance()
                .get(MessageKeys.LOBBY_SCOREBOARD_LINES)
                .toStringList();

        lobbyScoreboardLines.forEach(line -> {
            line = line
                    .replace("%sba_version%", SBA.getInstance().getVersion())
                    .replace("%date%", DateUtils.getFormattedDate())
                    .replace("%state%", finalState)
                    .replace("%game%", game.getName())
                    .replace("%players%", String.valueOf(game.getConnectedPlayers().size()))
                    .replace("%maxplayers%", String.valueOf(game.getMaxPlayers()))
                    .replace("%minplayers%", String.valueOf(game.getMinPlayers()))
                    .replace("%needplayers%", String.valueOf(finalNeedplayers))
                    .replace("%mode%", mode);
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                line = PlaceholderAPI.setPlaceholders(player, line);
            lines.add(line);
        });

        /*final var holder = scoreboard.getHolder();
        game.getRunningTeams().forEach(team -> {
            if (!holder.getTeam(team.getName()).isPresent()) {
                holder.team(team.getName()).color(NamedTextColor.NAMES.value(TeamColor.fromApiColor(team.getColor()).chatColor.toString()));
            }
            final var scoreboardTeam = holder.getTeam(team.getName()).orElse(holder.team(team.getName()));

            new HashSet<>(scoreboardTeam.players())
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(teamPlayer -> {
                        if (!team.getConnectedPlayers().contains(teamPlayer.as(Player.class))) {
                            scoreboardTeam.removePlayer(teamPlayer);
                        }
                    });

            team.getConnectedPlayers()
                    .stream()
                    .map(PlayerMapper::wrapPlayer)
                    .filter(playerName -> !scoreboardTeam.players().contains(playerName))
                    .forEach(scoreboardTeam::player);
        });*/
        return lines;
    }
}
