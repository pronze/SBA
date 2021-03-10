package pronze.hypixelify.game;

import lombok.Getter;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerKilledEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.utils.TitleUtils;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.data.PlayerData;
import pronze.hypixelify.api.manager.ScoreboardManager;
import pronze.hypixelify.scoreboard.GameScoreboardManagerImpl;
import pronze.hypixelify.utils.SBAUtil;

import java.util.*;

import static pronze.hypixelify.lib.lang.I.i18n;

@Getter
public class ArenaImpl implements pronze.hypixelify.api.game.Arena {
    private final double radius;
    private final Game game;
    private final GameScoreboardManagerImpl scoreboardManager;
    private final GameStorage storage;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final GameTask gameTask;

    public ArenaImpl(Game game) {
        radius = Math.pow(SBAHypixelify.getConfigurator().config.getInt(
                "upgrades.trap-detection-range", 7), 2);
        this.game = game;
        storage = new GameStorage(game);
        gameTask = new GameTask(this);
        scoreboardManager = new GameScoreboardManagerImpl(this);
        game.getConnectedPlayers()
                .forEach(player -> putPlayerData(player.getUniqueId(), PlayerData.from(player)));
    }

    public void onGameStarted() {
        game.getConnectedPlayers().forEach(player -> SBAUtil.translateColors(SBAHypixelify.getConfigurator()
                .getStringList("game-start.message"))
                .stream()
                .filter(Objects::nonNull)
                .forEach(player::sendMessage));
    }


    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final var team = e.getTeam();
        team.getConnectedPlayers().forEach(player ->
                SBAUtil.sendTitle(
                        PlayerMapper.wrapPlayer(e.getPlayer()),
                        i18n("bed-destroyed.title"),
                        i18n("bed-destroyed.sub-title"), 0, 40, 20)
        );

        final var destroyer = e.getPlayer();
        if (destroyer != null) {
            final var data = playerDataMap.get(destroyer.getUniqueId());
            final var currentDestroys = data.getBedDestroys();
            data.setBedDestroys(currentDestroys + 1);
        }
    }

    public void onOver(BedwarsGameEndingEvent e) {
        scoreboardManager.destroy();
        gameTask.cancel();
        final var winner = e.getWinningTeam();
        if (winner != null) {
            final var nullStr = i18n("none");
            String firstKillerName = nullStr;
            int firstKillerScore = 0;

            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
                final var playerData = playerDataMap.get(entry.getKey());
                final var kills = playerData.getKills();
                if (kills > 0 && kills > firstKillerScore) {
                    firstKillerScore = kills;
                    firstKillerName = playerData.getName();
                }
            }

            String secondKillerName = nullStr;
            int secondKillerScore = 0;

            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
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
            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
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

    public void putPlayerData(UUID uuid, PlayerData data) {
        playerDataMap.put(uuid, data);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public void onBedWarsPlayerKilled(BedwarsPlayerKilledEvent e) {
        final var victim = e.getPlayer();
        final var victimData = playerDataMap.get(victim.getUniqueId());
        victimData.setDeaths(victimData.getDeaths() + 1);

        final var killer = e.getKiller();
        if (killer != null) {
            final var gVictim = Main.getPlayerGameProfile(victim);
            if (gVictim == null || gVictim.isSpectator) return;

            final var team = game.getTeamOfPlayer(gVictim.player);
            if (team != null) {
                final var killerData = playerDataMap.get(killer.getUniqueId());
                killerData.setKills(killerData.getKills() + 1);
                if (!team.isAlive()) killerData.setFinalKills(killerData.getFinalKills() + 1);
            }
        }
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
