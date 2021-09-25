package io.github.pronze.sba.game;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.game.tasks.BaseGameTask;
import io.github.pronze.sba.game.tasks.GameTaskManager;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.manager.ScoreboardManager;
import io.github.pronze.sba.service.NPCStoreService;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.NPCSkin;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.lib.world.LocationMapper;

import java.util.*;
import java.util.stream.Collectors;

public class Arena implements IArena {
    private static LivingEntity mockEntity = null;
    private final List<IRotatingGenerator> rotatingGenerators;
    private final Map<UUID, InvisiblePlayer> invisiblePlayers;
    private final Map<UUID, GamePlayerData> playerDataMap;
    private final List<BaseGameTask> gameTasks;
    private final List<NPC> storeNPCS;
    private final List<NPC> upgradeStoreNPCS;
    private final GameScoreboardManager scoreboardManager;
    private final Game game;
    private final IGameStorage storage;

    public Arena(@NotNull Game game) {
        this.game = game;
        this.rotatingGenerators = new ArrayList<>();
        this.invisiblePlayers = new HashMap<>();
        this.playerDataMap = new HashMap<>();
        this.gameTasks = new ArrayList<>();
        this.storeNPCS = new ArrayList<>();
        this.upgradeStoreNPCS = new ArrayList<>();

        this.storage = new GameStorage(game);
        this.gameTasks.addAll(GameTaskManager.getInstance().startTasks(this));
        this.scoreboardManager = new GameScoreboardManager(this);
        this.game.getConnectedPlayers().forEach(player -> registerPlayerData(player.getUniqueId(), GamePlayerData.of(player)));
    }

    @NotNull
    @Override
    public List<Player> getInvisiblePlayers() {
        return invisiblePlayers
                .values()
                .stream()
                .map(InvisiblePlayer::getHiddenPlayer)
                .collect(Collectors.toList());
    }

    @Override
    public void addHiddenPlayer(@NotNull Player player) {
        if (invisiblePlayers.containsKey(player.getUniqueId())) {
            return;
        }
        final var invisiblePlayer = new InvisiblePlayerImpl(player, this);
        invisiblePlayer.vanish();
        invisiblePlayers.put(player.getUniqueId(), invisiblePlayer);
    }

    @Override
    public void removeHiddenPlayer(@NotNull Player player) {
        final var invisiblePlayer = invisiblePlayers.get(player.getUniqueId());
        if (invisiblePlayer != null) {
            invisiblePlayer.setHidden(false);
            invisiblePlayers.remove(player.getUniqueId());
        }
    }

    @Override
    public void registerPlayerData(@NotNull UUID uuid, @NotNull GamePlayerData data) {
        if (playerDataMap.containsKey(uuid)) {
            throw new UnsupportedOperationException("PlayerData of uuid: " + uuid + " is already registered!");
        }
        playerDataMap.put(uuid, data);
    }

    @Override
    public void unregisterPlayerData(@NotNull UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            throw new UnsupportedOperationException("PlayerData of uuid: " + uuid + " is not registered!");
        }
        playerDataMap.remove(uuid);
    }

    @Override
    public Optional<GamePlayerData> getPlayerData(@NotNull UUID uuid) {
        return Optional.ofNullable(playerDataMap.get(uuid));
    }

    @Override
    public @NotNull IGameStorage getStorage() {
        return storage;
    }

    @Override
    public @NotNull Game getGame() {
        return game;
    }

    @NotNull
    @Override
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    @Override
    public boolean isPlayerHidden(@NotNull Player player) {
        return invisiblePlayers.containsKey(player.getUniqueId());
    }


    public void onGameStarted() {
        // send game start message
        LanguageService
                .getInstance()
                .get(MessageKeys.GAME_START_MESSAGE)
                .send(game.getConnectedPlayers().stream().map(PlayerMapper::wrapPlayer).toArray(PlayerWrapper[]::new));


        // spawn rotating generators
        if (SBAConfig.getInstance().node("floating-generator", "enabled").getBoolean()) {
            game.getItemSpawners()
                    .forEach(itemSpawner -> {
                        for (var entry : SBAConfig.getInstance().node("floating-generator", "mapping").childrenMap().entrySet()) {
                            if (itemSpawner.getItemSpawnerType().getMaterial().name().equalsIgnoreCase(((String) entry.getKey()).toUpperCase())) {
                                createRotatingGenerator((ItemSpawner) itemSpawner, Material.valueOf(entry.getValue().getString("AIR")));
                            }
                        }
                    });
        }

        if (SBAConfig.getInstance().node("replace-stores-with-npc").getBoolean(true)) {
            game.getGameStores().forEach(store -> {
                final var nonAPIStore = (GameStore) store;
                final var villager = nonAPIStore.kill();
                if (villager != null) {
                    Main.unregisterGameEntity(villager);
                }

                if (mockEntity == null) {
                    // find a better version independent way to mock entities lol
                    mockEntity = (Bat) game.getGameWorld().spawnEntity(game.getSpectatorSpawn().clone().add(0, 300, 0), EntityType.BAT);
                    mockEntity.setAI(false);
                }

                // set fake entity to avoid bw listener npe
                Reflect.setField(nonAPIStore, "entity", mockEntity);

                NPCSkin skin = null;
                List<Component> name = null;
                final var file = store.getShopFile();
                try {
                    if (file != null && file.equalsIgnoreCase("upgradeShop.yml")) {
                        skin = NPCStoreService.getInstance().getUpgradeShopSkin();
                        name = NPCStoreService.getInstance().getUpgradeShopText();
                    } else {
                        skin = NPCStoreService.getInstance().getShopSkin();
                        name = NPCStoreService.getInstance().getShopText();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                final var npc = NPC.of(LocationMapper.wrapLocation(store.getStoreLocation()))
                        .setDisplayName(name)
                        .setShouldLookAtViewer(true)
                        .setTouchable(true)
                        .setSkin(skin);

                if (file != null && file.equals("upgradeShop.yml")) {
                    upgradeStoreNPCS.add(npc);
                } else {
                    storeNPCS.add(npc);
                }

                game.getConnectedPlayers()
                        .stream()
                        .map(PlayerMapper::wrapPlayer)
                        .forEach(npc::addViewer);
                npc.show();
            });
        }
    }

    // non api event handler
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
                SBAUtil.sendTitle(PlayerMapper.wrapPlayer(player), title, subtitle, 0, 40, 20)
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
        gameTasks.forEach(BaseGameTask::stop);
        gameTasks.clear();

        rotatingGenerators.forEach(IRotatingGenerator::destroy);
        rotatingGenerators.clear();

        storeNPCS.forEach(NPC::destroy);
        upgradeStoreNPCS.forEach(NPC::destroy);

        storeNPCS.clear();
        upgradeStoreNPCS.clear();

        getInvisiblePlayers().forEach(this::removeHiddenPlayer);

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
    public void createRotatingGenerator(@NotNull ItemSpawner itemSpawner, @NotNull Material rotationMaterial) {
        final var generator = new RotatingGenerator(itemSpawner, new ItemStack(rotationMaterial), itemSpawner.getLocation());
        generator.spawn(game.getConnectedPlayers());
        rotatingGenerators.add(generator);
    }

    @NotNull
    @Override
    public List<NPC> getStoreNPCS() {
        return List.copyOf(storeNPCS);
    }

    @NotNull
    @Override
    public List<NPC> getUpgradeStoreNPCS() {
        return List.copyOf(upgradeStoreNPCS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseGameTask> Optional<T> getTask(@NotNull Class<T> taskClass) {
        return (Optional<T>) getGameTasks()
                .stream()
                .filter(gameTask -> gameTask.getClass().isAssignableFrom(taskClass))
                .findAny();
    }

    @Override
    public List<BaseGameTask> getGameTasks() {
        return List.copyOf(gameTasks);
    }

    @Override
    public List<IRotatingGenerator> getRotatingGenerators() {
        return List.copyOf(rotatingGenerators);
    }

    @Override
    public Optional<InvisiblePlayer> getHiddenPlayer(UUID playerUUID) {
        return Optional.ofNullable(invisiblePlayers.get(playerUUID));
    }
}
