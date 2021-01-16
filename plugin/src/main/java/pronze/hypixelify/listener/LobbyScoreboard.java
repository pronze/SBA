package pronze.hypixelify.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import pronze.hypixelify.Configurator;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ScoreboardUtil;
import pronze.lib.scoreboards.Scoreboard;
import pronze.lib.scoreboards.ScoreboardManager;

import java.text.SimpleDateFormat;
import java.util.*;

import static pronze.hypixelify.lib.lang.I.i18n;

public class LobbyScoreboard implements Listener {
    private static final String date = new SimpleDateFormat(Configurator.date).format(new Date());
    private final Map<UUID, Scoreboard> scoreboardMap = new HashMap<>();

    public static boolean isInLobby(Player player) {
        final var game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        return game != null && game.getStatus() == GameStatus.WAITING;
    }

    @EventHandler
    public void onPlayerJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        if (e.getGame().getStatus() == GameStatus.WAITING) {
            Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> createBoard(player, e.getGame()), 3L);
        }
    }

    private void createBoard(Player player, Game game) {
        Logger.trace("Creating board for player: {}", player.getName());

        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);

        final var scoreboard = Scoreboard.builder()
                .animate(true)
                .player(player)
                .async(false)
                .displayObjective(ScoreboardUtil.LOBBY_OBJECTIVE_NAME)
                .updateInterval(20L)
                .animationInterval(2L)
                .animatedTitle(SBAHypixelify.getConfigurator()
                        .getStringList("lobby-scoreboard.title"))
                .updateCallback(board -> {
                    board.setLines(process(player, game, board));
                    return true;
                })
                .build();
        scoreboardMap.put(player.getUniqueId(), scoreboard);
    }

    @EventHandler
    public void onPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        if (scoreboardMap.containsKey(player.getUniqueId())) {
            final var scoreboard = scoreboardMap.get(player.getUniqueId());
            if (scoreboard != null) {
                scoreboard.destroy();
                scoreboardMap.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        final var player = e.getPlayer();
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
        String state = i18n("waiting");
        String countdown = "null";
        int needplayers = game.getMinPlayers() - game.getConnectedPlayers().size();
        needplayers = Math.max(needplayers, 0);
        int s = Configurator.game_size.getOrDefault(game.getName(), 4);
        String mode;
        switch (s) {
            case 1:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.solo-prefix", "Solo");
                break;
            case 2:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.doubles-prefix", "Doubles");
                break;
            case 3:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.triples-prefix", "Triples");
                break;
            case 4:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.squads-prefix", "Squads");
                break;
            default:
                mode = s + "v" + s + "v" + s + "v" + s;
        }

        if (game.countConnectedPlayers() >= game.getMinPlayers()
                && game.getStatus() == GameStatus.WAITING) {
            final var time = Main.getGame(game.getName()).getFormattedTimeLeft();
            if (!time.contains("0-1")) {
                final var units = time.split(":");
                var seconds = Integer.parseInt(units[1]) + 1;
                state = i18n("countdown").replace("{countdown}", String.valueOf(seconds));
            }
        }

        final var finalState = state;
        final var finalNeedplayers = needplayers;

        SBAHypixelify.getConfigurator()
                .getStringList("lobby_scoreboard.lines").forEach(line -> {
            line = line
                    .replace("{date}", date).replace("{state}", finalState)
                    .replace("{game}", game.getName())
                    .replace("{players}", String.valueOf(game.getConnectedPlayers().size()))
                    .replace("{maxplayers}", String.valueOf(game.getMaxPlayers()))
                    .replace("{minplayers}", String.valueOf(game.getMinPlayers()))
                    .replace("{needplayers}", String.valueOf(finalNeedplayers))
                    .replace("{countdown}", countdown)
                    .replace("{mode}", mode);
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                line = PlaceholderAPI.setPlaceholders(player, line);
            lines.add(line);
        });

        final var holder = scoreboard.getHolder();
        game.getRunningTeams().forEach(team -> {
            if (!holder.hasTeamEntry(team.getName())) {
                holder.addTeam(team.getName(), TeamColor.fromApiColor(team.getColor()).chatColor);
            }
            final var scoreboardTeam = holder.getTeamOrRegister(team.getName());

            new HashSet<>(scoreboardTeam.getEntries())
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Bukkit::getPlayerExact)
                    .filter(Objects::nonNull)
                    .forEach(teamPlayer -> {
                        if (!team.getConnectedPlayers().contains(teamPlayer)) {
                            scoreboardTeam.removeEntry(teamPlayer.getName());
                        }
                    });

            team.getConnectedPlayers()
                    .stream()
                    .map(Player::getName)
                    .filter(playerName -> !scoreboardTeam.hasEntry(playerName))
                    .forEach(scoreboardTeam::addEntry);
        });
        return lines;
    }
}
