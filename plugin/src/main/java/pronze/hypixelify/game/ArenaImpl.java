package pronze.hypixelify.game;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerKilledEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.data.GamePlayerData;
import pronze.hypixelify.api.manager.ScoreboardManager;
import pronze.hypixelify.scoreboard.GameScoreboardManagerImpl;
import pronze.hypixelify.utils.SBAUtil;

import java.util.*;

import static pronze.hypixelify.lib.lang.I.i18n;

@Getter
public class ArenaImpl implements pronze.hypixelify.api.game.Arena {
    private final Map<UUID, GamePlayerData> playerDataMap = new HashMap<>();
    private final GameScoreboardManagerImpl scoreboardManager;
    private final double radius;
    private final Game game;
    private final GameStorage storage;
    private final GameTask gameTask;

    public ArenaImpl(Game game) {
        radius = Math.pow(SBAHypixelify.getConfigurator().config.getInt("upgrades.trap-detection-range", 7), 2);
        this.game = game;
        storage = new GameStorage(game);
        gameTask = new GameTask(this);
        scoreboardManager = new GameScoreboardManagerImpl(this);
        registerPlayerData(game.getConnectedPlayers().toArray(Player[]::new));
    }

    private void registerPlayerData(Player... players) {
        for (var player : players) {
            putPlayerData(player.getUniqueId(), GamePlayerData.from(player));
        }
    }

    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final var team = e.getTeam();
        // send bed destroyed message to all players of the team
        team.getConnectedPlayers().forEach(player ->
                SBAUtil.sendTitle(
                        PlayerMapper.wrapPlayer(e.getPlayer()),
                        i18n("bed-destroyed.title"),
                        i18n("bed-destroyed.sub-title"), 0, 40, 20)
        );

        final var destroyer = e.getPlayer();
        if (destroyer != null) {
            // increment bed destroy data for the destroyer
            getPlayerData(destroyer.getUniqueId())
                    .ifPresent(destroyerData ->
                            destroyerData.setBedDestroys(destroyerData.getBedDestroys() + 1)
                    );
        }
    }

    public void onOver(BedwarsGameEndingEvent e) {
        // destroy scoreboard manager instance and GameTask, we do not need these anymore
        scoreboardManager.destroy();
        gameTask.cancel();
        final var winner = e.getWinningTeam();
        if (winner != null) {
            final var nullStr = i18n("none");
            String firstKillerName = nullStr;
            int firstKillerScore = 0;

            for (Map.Entry<UUID, GamePlayerData> entry : playerDataMap.entrySet()) {
                final var playerData = playerDataMap.get(entry.getKey());
                final var kills = playerData.getKills();
                if (kills > 0 && kills > firstKillerScore) {
                    firstKillerScore = kills;
                    firstKillerName = playerData.getName();
                }
            }

            String secondKillerName = nullStr;
            int secondKillerScore = 0;

            for (Map.Entry<UUID, GamePlayerData> entry : playerDataMap.entrySet()) {
                final var playerData = playerDataMap.get(entry.getKey());
                final var kills = playerData.getKills();
                final var name = playerData.getName();

                if (kills > 0 && kills > secondKillerScore && !name.equalsIgnoreCase(firstKillerName)) {
                    secondKillerName = name;
                    secondKillerScore = kills;
                }
            }

            String thirdKillerName = nullStr;
            int thirdKillerScore = 0;
            for (Map.Entry<UUID, GamePlayerData> entry : playerDataMap.entrySet()) {
                final var playerData = playerDataMap.get(entry.getKey());
                final var kills = playerData.getKills();
                final var name = playerData.getName();
                if (kills > 0 && kills > thirdKillerScore && !name.equalsIgnoreCase(firstKillerName) &&
                        !name.equalsIgnoreCase(secondKillerName)) {
                    thirdKillerName = name;
                    thirdKillerScore = kills;
                }
            }


            final var WinTeamPlayers = new ArrayList<String>();
            winner.getConnectedPlayers().forEach(player -> WinTeamPlayers.add(player.getDisplayName()));
            winner.getConnectedPlayers().forEach(pl ->
                    SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), i18n("victory-title"),
                            "", 0, 90, 0));

            for (var player : game.getConnectedPlayers()) {
                for (var message : SBAUtil.translateColors(SBAHypixelify.getConfigurator().getStringList("overstats.message"))) {
                    if (message != null) {
                        player.sendMessage(message.replace("{color}",
                                org.screamingsandals.bedwars.game.TeamColor.valueOf(winner.getColor().name()).chatColor.toString())
                                .replace("%win_team%", winner.getName())
                                .replace("%winners%", WinTeamPlayers.toString())
                                .replace("%first_killer_name%", firstKillerName)
                                .replace("%second_killer_name%", secondKillerName)
                                .replace("%third_killer_name%", thirdKillerName)
                                .replace("%first_killer_score%", String.valueOf(firstKillerScore))
                                .replace("%second_killer_score%", String.valueOf(secondKillerScore))
                                .replace("%third_killer_score%", String.valueOf(thirdKillerScore)));
                    }
                }
            }
        }

    }

    @Override
    public void putPlayerData(UUID uuid, GamePlayerData data) {
        playerDataMap.put(uuid, data);
    }

    @Override
    public Optional<GamePlayerData> getPlayerData(UUID uuid) {
        return Optional.ofNullable(playerDataMap.get(uuid));
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
