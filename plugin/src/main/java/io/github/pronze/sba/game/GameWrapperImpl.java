package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.data.GameStoreData;
import io.github.pronze.sba.game.tasks.GameTask;
import io.github.pronze.sba.game.tasks.GameTaskManagerImpl;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.wrapper.game.GameWrapper;
import io.github.pronze.sba.wrapper.store.GameStoreWrapper;
import io.github.pronze.sba.wrapper.team.RunningTeamWrapper;
import io.github.pronze.sba.wrapper.team.TeamWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.BasicWrapper;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.LocationMapper;
import org.screamingsandals.lib.world.WorldHolder;
import org.screamingsandals.lib.world.WorldMapper;

import java.util.*;
import java.util.stream.Collectors;

public class GameWrapperImpl extends BasicWrapper<Game> implements GameWrapper {
    private final List<RotatingGenerator> rotatingGenerators;
    private final Map<UUID, InvisiblePlayer> invisiblePlayers;
    private final Map<UUID, GamePlayerData> playerDataMap;
    private final List<GameTask> gameTasks;
    private final List<NPC> storeNPCS;
    private final List<NPC> upgradeStoreNPCS;
    private final List<GameStoreData> gameStoreData;
    private final GameStorage storage;

    protected GameWrapperImpl(@NotNull Game game) {
        super(game);
        this.rotatingGenerators = new ArrayList<>();
        this.invisiblePlayers = new HashMap<>();
        this.playerDataMap = new HashMap<>();
        this.gameTasks = new ArrayList<>();
        this.storeNPCS = new ArrayList<>();
        this.upgradeStoreNPCS = new ArrayList<>();
        this.gameStoreData = new ArrayList<>();
        this.storage = new GameStorageImpl(this);

        final var gameStoreList = ((org.screamingsandals.bedwars.game.Game) game).getGameStoreList();
        gameStoreList.forEach(gameStore -> gameStoreData.add(GameStoreData.of(gameStore)));
        ((org.screamingsandals.bedwars.game.Game) game).getGameStoreList().clear();
    }

    @NotNull
    @Override
    public List<PlayerWrapper> getInvisiblePlayers() {
        return invisiblePlayers
                .values()
                .stream()
                .map(InvisiblePlayer::getPlayer)
                .collect(Collectors.toList());
    }

    @Override
    public void addHiddenPlayer(@NotNull PlayerWrapper player) {
        if (invisiblePlayers.containsKey(player.getUniqueId())) {
            return;
        }
        final var invisiblePlayer = new InvisiblePlayerImpl(player, this);
        invisiblePlayer.vanish();
        invisiblePlayers.put(player.getUniqueId(), invisiblePlayer);
    }

    @Override
    public void removeHiddenPlayer(@NotNull PlayerWrapper player) {
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
    public @NotNull GameStorage getStorage() {
        return storage;
    }

    @Override
    public @NotNull Game getGame() {
        return wrappedObject;
    }

    @Override
    public boolean isPlayerHidden(@NotNull PlayerWrapper player) {
        return invisiblePlayers.containsKey(player.getUniqueId());
    }

    @Override
    public void createRotatingGenerator(@NotNull ItemSpawner itemSpawner, @NotNull Material rotationMaterial) {
        final var generator = new RotatingGeneratorImpl(itemSpawner, new ItemStack(rotationMaterial), LocationMapper.wrapLocation(itemSpawner.getLocation()));
        getConnectedPlayers().forEach(generator::addViewer);
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
    public <T extends GameTask> Optional<T> getTask(@NotNull Class<T> taskClass) {
        return (Optional<T>) getGameTasks()
                .stream()
                .filter(gameTask -> gameTask.getClass().isAssignableFrom(taskClass))
                .findAny();
    }

    @Override
    public List<GameTask> getGameTasks() {
        return List.copyOf(gameTasks);
    }

    @Override
    public List<RotatingGenerator> getRotatingGenerators() {
        return List.copyOf(rotatingGenerators);
    }

    @Override
    public Optional<InvisiblePlayer> getHiddenPlayer(@NotNull UUID playerUUID) {
        return Optional.ofNullable(invisiblePlayers.get(playerUUID));
    }

    @Override
    public List<SBAPlayerWrapper> getConnectedPlayers() {
        return wrappedObject.getConnectedPlayers()
                .stream()
                .map(SBAPlayerWrapper::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<org.screamingsandals.bedwars.api.game.ItemSpawner> getItemSpawners() {
        return wrappedObject.getItemSpawners();
    }

    @Override
    public List<GameStoreWrapper> getGameStores() {
        return wrappedObject.getGameStores()
                .stream()
                .map(gameStore -> (GameStore) gameStore)
                .map(GameStoreWrapper::of)
                .collect(Collectors.toList());
    }

    @Override
    public WorldHolder getGameWorld() {
        return WorldMapper.wrapWorld(wrappedObject.getGameWorld());
    }

    @Override
    public LocationHolder getSpectatorSpawn() {
        return LocationMapper.wrapLocation(wrappedObject.getSpectatorSpawn());
    }

    @Override
    public void registerUpgradeStoreNPC(@NotNull NPC npc) {
        if (!upgradeStoreNPCS.contains(npc)) {
            upgradeStoreNPCS.add(npc);
        }
    }

    @Override
    public void registerStoreNPC(@NotNull NPC npc) {
        if (!storeNPCS.contains(npc)) {
            storeNPCS.add(npc);
        }
    }

    @Override
    public void unregisterUpgradeStoreNPC(@NotNull NPC npc) {
        upgradeStoreNPCS.remove(npc);
    }

    @Override
    public void unregisterStoreNPC(@NotNull NPC npc) {
        storeNPCS.remove(npc);
    }

    @Override
    public void start() {
        this.gameTasks.addAll(GameTaskManagerImpl.getInstance().startTasks(this));
        getConnectedPlayers().forEach(player -> registerPlayerData(player.getUuid(), GamePlayerData.of(player)));
        GameScoreboardManager.of(this);
    }

    @Override
    public void stop() {
        GameScoreboardManager.getInstance().destroy(this);

        gameTasks.forEach(GameTask::stop);
        gameTasks.clear();

        rotatingGenerators.forEach(RotatingGenerator::destroy);
        rotatingGenerators.clear();

        storeNPCS.forEach(NPC::destroy);
        upgradeStoreNPCS.forEach(NPC::destroy);

        storeNPCS.clear();
        upgradeStoreNPCS.clear();

        playerDataMap.clear();
        storage.clear();

        getInvisiblePlayers().forEach(this::removeHiddenPlayer);
    }

    @Override
    public Map<UUID, GamePlayerData> getPlayerDataMap() {
        return Map.copyOf(playerDataMap);
    }

    @NotNull
    @Override
    public List<RunningTeamWrapper> getRunningTeams() {
        return wrappedObject.getRunningTeams()
                .stream()
                .map(RunningTeamWrapper::of)
                .collect(Collectors.toList());
    }

    @Override
    public RunningTeamWrapper getTeamOfPlayer(PlayerWrapper player) {
        return RunningTeamWrapper.of(wrappedObject.getTeamOfPlayer(player.as(Player.class)));
    }

    @Override
    public @NotNull String getName() {
        return wrappedObject.getName();
    }

    @Override
    public GameStatus getStatus() {
        return wrappedObject.getStatus();
    }

    @Override
    public int countAvailableTeams() {
        return wrappedObject.countAvailableTeams();
    }

    @Override
    public String getFormattedTimeLeft() {
        return ((org.screamingsandals.bedwars.game.Game) wrappedObject).getFormattedTimeLeft();
    }

    @Override
    public List<TeamWrapper> getAvailableTeams() {
        return wrappedObject.getAvailableTeams()
                .stream()
                .map(TeamWrapper::of)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPlayerConnected(@NotNull UUID queryId) {
        return getConnectedPlayers()
                .stream()
                .anyMatch(sbaPlayerWrapper -> sbaPlayerWrapper.getUniqueId().equals(queryId));
    }

    @Override
    public boolean isPlayerConnected(@NotNull PlayerWrapper player) {
        return isPlayerConnected(player.getUniqueId());
    }

    @Override
    public int getMinPlayers() {
        return wrappedObject.getMinPlayers();
    }

    @Override
    public @NotNull List<GameStoreData> getGameStoreData() {
        return List.copyOf(gameStoreData);
    }
}