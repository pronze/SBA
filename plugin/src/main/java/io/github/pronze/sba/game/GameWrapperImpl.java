package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.game.tasks.GameTask;
import io.github.pronze.sba.game.tasks.GameTaskManagerImpl;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.utils.BasicWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class GameWrapperImpl extends BasicWrapper<Game> implements GameWrapper {
    private final List<RotatingGenerator> rotatingGenerators;
    private final Map<UUID, InvisiblePlayer> invisiblePlayers;
    private final Map<UUID, GamePlayerData> playerDataMap;
    private final List<GameTask> gameTasks;
    private final List<NPC> storeNPCS;
    private final List<NPC> upgradeStoreNPCS;
    private final GameStorage storage;

    public GameWrapperImpl(@NotNull Game game) {
        super(game);
        this.rotatingGenerators = new ArrayList<>();
        this.invisiblePlayers = new HashMap<>();
        this.playerDataMap = new HashMap<>();
        this.gameTasks = new ArrayList<>();
        this.storeNPCS = new ArrayList<>();
        this.upgradeStoreNPCS = new ArrayList<>();

        this.storage = new GameStorageImpl(game);
        this.gameTasks.addAll(GameTaskManagerImpl.getInstance().startTasks(this));
        getConnectedPlayers().forEach(player -> registerPlayerData(player.getUniqueId(), GamePlayerData.of(player)));
        GameScoreboardManager.of(this);
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
    public @NotNull GameStorage getStorage() {
        return storage;
    }

    @Override
    public @NotNull Game getGame() {
        return wrappedObject;
    }

    @Override
    public boolean isPlayerHidden(@NotNull Player player) {
        return invisiblePlayers.containsKey(player.getUniqueId());
    }

    @Override
    public void createRotatingGenerator(@NotNull ItemSpawner itemSpawner, @NotNull Material rotationMaterial) {
        final var generator = new RotatingGeneratorImpl(itemSpawner, new ItemStack(rotationMaterial), itemSpawner.getLocation());
        generator.spawn(getConnectedPlayers());
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
    public List<Player> getConnectedPlayers() {
        return wrappedObject.getConnectedPlayers();
    }

    @Override
    public List<org.screamingsandals.bedwars.api.game.ItemSpawner> getItemSpawners() {
        return wrappedObject.getItemSpawners();
    }

    @Override
    public List<org.screamingsandals.bedwars.api.game.GameStore> getGameStores() {
        return wrappedObject.getGameStores();
    }

    @Override
    public World getGameWorld() {
        return wrappedObject.getGameWorld();
    }

    @Override
    public Location getSpectatorSpawn() {
        return wrappedObject.getSpectatorSpawn();
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
    public void destroy() {
        GameScoreboardManager.getInstance().destroy(this);

        gameTasks.forEach(GameTask::stop);
        gameTasks.clear();

        rotatingGenerators.forEach(RotatingGenerator::destroy);
        rotatingGenerators.clear();

        storeNPCS.forEach(NPC::destroy);
        upgradeStoreNPCS.forEach(NPC::destroy);

        storeNPCS.clear();
        upgradeStoreNPCS.clear();

        getInvisiblePlayers().forEach(this::removeHiddenPlayer);
    }

    @Override
    public Map<UUID, GamePlayerData> getPlayerDataMap() {
        return Map.copyOf(playerDataMap);
    }
}
