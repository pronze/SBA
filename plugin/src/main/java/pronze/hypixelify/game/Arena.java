package pronze.hypixelify.game;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.ext.configurate.serialize.SerializationException;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.player.PlayerWrapper;
import org.screamingsandals.bedwars.utils.TitleUtils;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.data.GamePlayerData;
import pronze.hypixelify.api.game.IArena;
import pronze.hypixelify.api.manager.ScoreboardManager;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.scoreboard.GameScoreboardManagerImpl;
import pronze.hypixelify.utils.SBAUtil;

import java.util.*;
import java.util.stream.Collectors;


@Getter
public class Arena implements IArena {
    private final Map<UUID, InvisiblePlayer> invisiblePlayers = new HashMap<>();
    private final Map<UUID, GamePlayerData> playerDataMap = new HashMap<>();
    private final GameScoreboardManagerImpl scoreboardManager;
    private final double radius;
    private final Game game;
    private final GameStorage storage;
    private final GameTask gameTask;

    public Arena(Game game) {
        radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
        this.game = game;
        storage = new GameStorage(game);
        gameTask = new GameTask(this);
        scoreboardManager = new GameScoreboardManagerImpl(this);
        game.getConnectedPlayers().forEach(this::registerPlayerData);
    }

    @Override
    public List<Player> getInvisiblePlayers() {
        return invisiblePlayers
                .values()
                .stream()
                .map(InvisiblePlayer::getPlayer)
                .collect(Collectors.toList());
    }

    @Override
    public void addHiddenPlayer(Player player) {
        if (invisiblePlayers.containsKey(player.getUniqueId())) return;
        final var invisiblePlayer = new InvisiblePlayer(player, this);
        invisiblePlayer.vanish();
        invisiblePlayers.put(player.getUniqueId(), invisiblePlayer);
    }

    @Override
    public void removeHiddenPlayer(Player player) {
        final var invisiblePlayer = invisiblePlayers.get(player.getUniqueId());
        if (invisiblePlayer != null) {
            invisiblePlayer.setHidden(false);
            invisiblePlayers.remove(player.getUniqueId());
        }
    }

    @Override
    public boolean isPlayerHidden(Player player) {
        return invisiblePlayers.containsKey(player.getUniqueId());
    }

    private void registerPlayerData(Player player) {
        putPlayerData(player.getUniqueId(), GamePlayerData.from(player));
    }

    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final var team = e.getTeam();
        // send bed destroyed message to all players of the team
        final var title = LanguageService
                .getInstance()
                .get(MessageKeys.BED_DESTROYED_TITLE)
                .toString();
        final var subtitle = LanguageService
                .getInstance()
                .get(MessageKeys.BED_DESTROYED_SUBTITLE)
                .toString();

        team.getConnectedPlayers().forEach(player ->
                SBAUtil.sendTitle(PlayerMapper.wrapPlayer(e.getPlayer()), title, subtitle, 0, 40, 20)
        );

        final var destroyer = e.getPlayer();
        if (destroyer != null) {
            // increment bed destroy data for the destroyer
            getPlayerData(destroyer.getUniqueId())
                    .ifPresent(destroyerData -> destroyerData.setBedDestroys(destroyerData.getBedDestroys() + 1));
        }
    }

    public void onOver(BedwarsGameEndingEvent e) {
        // destroy scoreboard manager instance and GameTask, we do not need these anymore
        scoreboardManager.destroy();
        gameTask.cancel();
        final var winner = e.getWinningTeam();
        if (winner != null) {
            final var nullStr = LanguageService
                    .getInstance()
                    .get(MessageKeys.NONE)
                    .toString();

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

            var victoryTitle = LanguageService
                    .getInstance()
                    .get(MessageKeys.VICTORY_TITLE)
                    .toString();

            final var WinTeamPlayers = new ArrayList<String>();
            winner.getConnectedPlayers().forEach(player -> WinTeamPlayers.add(player.getDisplayName()));
            winner.getConnectedPlayers().forEach(pl ->
                    SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), victoryTitle, "", 0, 90, 0));

            LanguageService
                    .getInstance()
                    .get(MessageKeys.OVERSTATS_MESSAGE)
                    .replace("%color%",
                            org.screamingsandals.bedwars.game.TeamColor.valueOf(winner.getColor().name()).chatColor.toString())
                    .replace("%win_team%", winner.getName())
                    .replace("%winners%", WinTeamPlayers.toString())
                    .replace("%first_killer_name%", firstKillerName)
                    .replace("%second_killer_name%", secondKillerName)
                    .replace("%third_killer_name%", thirdKillerName)
                    .replace("%first_killer_score%", String.valueOf(firstKillerScore))
                    .replace("%second_killer_score%", String.valueOf(secondKillerScore))
                    .replace("%third_killer_score%", String.valueOf(thirdKillerScore))
                    .send(game.getConnectedPlayers().stream().map(PlayerMapper::wrapPlayer).toArray(PlayerWrapper[]::new));
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
