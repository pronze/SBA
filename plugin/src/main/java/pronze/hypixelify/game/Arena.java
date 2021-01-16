package pronze.hypixelify.game;

import pronze.hypixelify.Configurator;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.scoreboard.ScoreBoard;
import pronze.hypixelify.utils.SBAUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerKilledEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.*;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;
import static pronze.hypixelify.lib.lang.I.i18n;

@Getter
public class Arena implements pronze.hypixelify.api.game.Arena {
    private final List<String> generatorHoloText;

    private static final String diamondHoloText = "§bDiamond";
    private static final String emeraldHoloText = "§aEmerald";

    public final double radius;

    private final Game game;

    private final ScoreBoard scoreboard;
    private final GameStorage storage;

    private final List<RotatingGenerators> rotatingGenerators = new ArrayList<>();
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public GameTask gameTask;

    public Arena(Game game) {
        generatorHoloText = SBAHypixelify.getConfigurator().getStringList("floating-generator.holo-text");
        radius = Math.pow(SBAHypixelify.getConfigurator()
                .config.getInt("upgrades.trap-detection-range", 7), 2);
        this.game = game;
        storage = new GameStorage(game);
        gameTask = new GameTask(this);
        scoreboard = new ScoreBoard(this);

        game.getConnectedPlayers()
                .forEach(player -> playerDataMap.put(player.getUniqueId(), new PlayerData()));
    }

    @Override
    public List<RotatingGenerators> getRotatingGenerators() {
        return rotatingGenerators;
    }

    @Override
    public Game getGame() {
        return game;
    }

    public void onGameStarted(BedwarsGameStartedEvent e) {
        final var game = e.getGame();
        if (!game.equals(this.game)) return;
        game.getConnectedPlayers().forEach(player -> Configurator.gamestart_message
                .stream()
                .filter(Objects::nonNull)
                .forEach(player::sendMessage));
        SBAUtil.destroySpawnerArmorStandEntitiesFrom(this.game);
        initalizeGenerators();
    }

    public void initalizeGenerators() {
        if (SBAHypixelify.getConfigurator().config
                .getBoolean("floating-generator.enabled", true)) {

            game.getItemSpawners()
                    .stream()
                    .filter(RotatingGenerators::canBeUsed)
                    .forEach(spawner -> {
                        final var spawnerMaterial = spawner.getItemSpawnerType().getMaterial();
                        final var rotationStack = spawnerMaterial == Material.DIAMOND ?
                                new ItemStack(Material.DIAMOND_BLOCK) :
                                new ItemStack(Material.EMERALD_BLOCK);

                        final var genHolo = new ArrayList<String>();
                        generatorHoloText.forEach(text -> genHolo.add(text.replace("{material}",
                                spawnerMaterial == Material.DIAMOND ? diamondHoloText
                                        : emeraldHoloText)));

                        rotatingGenerators.add(new RotatingGenerators(spawner,
                                rotationStack, genHolo).spawn(game.getConnectedPlayers()));
                    });
        }
    }

    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final var team = e.getTeam();
        team.getConnectedPlayers().forEach(player -> sendTitle(player, i18n("bed-destroyed.title"),
                i18n("bed-destroyed.sub-title"), 0, 40, 20));

        final var destroyer = e.getPlayer();
        if (destroyer != null) {
            final var data = playerDataMap.get(destroyer.getUniqueId());
            final var currentDestroys = data.getBedDestroys();
            data.setBedDestroys(currentDestroys + 1);
        }
    }

    public void onPreRebuildingEvent() {
        try {
            if (gameTask != null && !gameTask.isCancelled()) {
                gameTask.cancel();
                gameTask = null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        RotatingGenerators.destroy(rotatingGenerators);
        rotatingGenerators.clear();
    }

    public void onOver(BedwarsGameEndingEvent e) {
        final var game = e.getGame();

        if (!this.game.equals(game)) {
            return;
        }

        try {
            if (gameTask != null && !gameTask.isCancelled()) {
                gameTask.cancel();
                gameTask = null;
            }
        } catch (IllegalStateException ignored) {
        }

        final var winner = e.getWinningTeam();

        if (winner != null) {
            final var dataKills = new HashMap<String, Integer>();
            playerDataMap
                    .keySet()
                    .stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach((player) -> {
                        dataKills.put(player.getDisplayName(), playerDataMap.get(player.getUniqueId()).getKills());
                    });


            int kills_1 = 0;
            int kills_2 = 0;
            int kills_3 = 0;
            String kills_1_player = "none";
            String kills_2_player = "none";
            String kills_3_player = "none";


            for (String player : dataKills.keySet()) {
                int k = dataKills.get(player);
                if (k > 0 && k > kills_1) {
                    kills_1_player = player;
                    kills_1 = k;
                }
            }
            for (String player : dataKills.keySet()) {
                int k = dataKills.get(player);
                if (k > kills_2 && k <= kills_1 && !player.equals(kills_1_player)) {
                    kills_2_player = player;
                    kills_2 = k;
                }
            }

            for (String player : dataKills.keySet()) {
                int k = dataKills.get(player);
                if (k > kills_3 && k <= kills_2 && !player.equals(kills_1_player) &&
                        !player.equals(kills_2_player)) {
                    kills_3_player = player;
                    kills_3 = k;
                }
            }

            final var WinTeamPlayers = new ArrayList<String>();

            winner.getConnectedPlayers().forEach(player -> WinTeamPlayers.add(player.getDisplayName()));
            winner.getConnectedPlayers().forEach(pl ->
                    sendTitle(pl, i18n("victory-title"),
                            "", 0, 90, 0));

            for (Player player : game.getConnectedPlayers()) {
                for (String message : Configurator.overstats_message) {
                    if (message == null) {
                        return;
                    }

                    player.sendMessage(message.replace("{color}",
                            org.screamingsandals.bedwars.game.TeamColor.valueOf(winner.getColor().name()).chatColor.toString())
                            .replace("{win_team}", winner.getName())
                            .replace("{win_team_players}", WinTeamPlayers.toString())
                            .replace("{first_1_kills_player}", kills_1_player)
                            .replace("{first_2_kills_player}", kills_2_player)
                            .replace("{first_3_kills_player}", kills_3_player)
                            .replace("{first_1_kills}", String.valueOf(kills_1))
                            .replace("{first_2_kills}", String.valueOf(kills_2))
                            .replace("{first_3_kills}", String.valueOf(kills_3)));
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
        final var game = e.getGame();

        final var victim = e.getPlayer();
        final var victimData = playerDataMap.get(victim.getUniqueId());
        victimData.setDeaths(victimData.getDeaths() + 1);
        final var killer = e.getKiller();
        if (killer == null) {
            return;
        }
        final var gVictim = Main.getPlayerGameProfile(victim);
        if (gVictim == null || gVictim.isSpectator) {
            return;
        }
        final var team = Main.getGame(game.getName()).getPlayerTeam(gVictim);
        if (team == null) {
            return;
        }
        final var killerData = playerDataMap.get(killer.getUniqueId());
        killerData.setKills(killerData.getKills() + 1);
        if (!team.isBed) {
            killerData.setFinalKills(killerData.getFinalKills() + 1);
        }
    }


}
