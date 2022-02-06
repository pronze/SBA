package io.github.pronze.sba.game;

import io.github.pronze.sba.config.MainConfig;
import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.data.GameStoreData;
import io.github.pronze.sba.game.task.GameTask;
import io.github.pronze.sba.service.GameTaskProvider;
import io.github.pronze.sba.visual.generator.RotatingGenerator;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.utils.BasicWrapper;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

public class GameWrapperImpl extends BasicWrapper<Game> implements GameWrapper {
    private final Map<UUID, GamePlayerData> playerDataMap = new HashMap<>();
    private final Map<UUID, InvisiblePlayer> invisiblePlayers = new HashMap<>();
    private final Map<ArenaType, Sidebar> sidebarMap = new HashMap<>();
    private final Map<GameStoreData, NPC> npcMap = new HashMap<>();
    private final List<RotatingGenerator> rotatingGenerators = new ArrayList<>();
    private final List<GameTask> gameTasks = new ArrayList<>();
    private final List<GameStoreData> gameStoreData = new ArrayList<>();

    @Getter
    @Setter
    private boolean running = true;

    public GameWrapperImpl(@NotNull Game game) {
        super(game);
        registerGameStores();
    }

    private void registerGameStores() {
        wrappedObject.getGameStoreList().forEach(gameStore -> {
            final var shopFile = gameStore.getShopFile();

            final var query = MainConfig
                    .getInstance()
                    .node("npc-stores")
                    .childrenList()
                    .stream()
                    .filter(configurationNode -> shopFile.equalsIgnoreCase(configurationNode.node("shop-file").getString()))
                    .findAny();

            ConfigurationNode npcNode;
            if (query.isPresent()) {
                npcNode = query.get();
            } else {
                npcNode = MainConfig.getInstance().node("npc-stores", "standard");
            }

            gameStoreData.add(GameStoreData.of(npcNode, gameStore));
        });
        wrappedObject.getGameStoreList().clear();
    }

    @Override
    public void destroy() {
        stop();
        ArenaType.VALUES.forEach(this::unregisterSidebar);
        running = false;
    }

    @Override
    public void stop() {
        gameTasks.forEach(GameTask::stop);
        gameTasks.clear();

        wrappedObject.stop();
        playerDataMap.clear();

        rotatingGenerators.forEach(RotatingGenerator::destroy);
        rotatingGenerators.clear();

        invisiblePlayers
                .values()
                .forEach(InvisiblePlayer::show);

        invisiblePlayers.clear();

        sidebarMap.computeIfPresent(ArenaType.GAME, ((arenaType, sidebar) -> {
            sidebar.destroy();
            return null;
        }));

        npcMap.values().forEach(NPC::destroy);
        npcMap.clear();

        running = false;
    }

    @Override
    public void start() {
        if (running) {
            stop();
        }

        // start game tasks.
        GameTaskProvider.getInstance().startTasks(this);
        running = true;
    }

    @Override
    public org.screamingsandals.bedwars.api.game.@NotNull Game getGame() {
        return wrappedObject;
    }

    @Override
    public void spectatorJoin(@NotNull GamePlayer gamePlayer) {
        // make sure player gets added to in-game visuals.
        rotatingGenerators.forEach(rotatingGenerator -> rotatingGenerator.addViewer(gamePlayer));

        sidebarMap.computeIfPresent(ArenaType.GAME, ((arenaType, sidebar) -> {
            sidebar.addViewer(gamePlayer);
            return sidebar;
        }));
    }

    @Override
    public void leaveFromGame(@NotNull GamePlayer gamePlayer) {
        rotatingGenerators.forEach(rotatingGenerator -> rotatingGenerator.removeViewer(gamePlayer));

        invisiblePlayers
                .values()
                .forEach(invisiblePlayer -> invisiblePlayer.removeHidden(gamePlayer));

        sidebarMap.computeIfPresent(ArenaType.GAME, ((arenaType, sidebar) -> {
            sidebar.removeViewer(gamePlayer);
            return sidebar;
        }));
    }

    @Override
    public @NotNull Optional<Sidebar> getSidebar(@NotNull ArenaType arenaType) {
        return Optional.ofNullable(sidebarMap.get(arenaType));
    }

    @Override
    public void registerSidebar(@NotNull ArenaType arenaType, @NotNull Sidebar sidebar) {
        sidebarMap.put(arenaType, sidebar);
    }

    @NotNull
    @Override
    public Map<ArenaType, Sidebar> getRegisteredSidebars() {
        return Map.copyOf(sidebarMap);
    }

    @Override
    public void unregisterSidebar(@NotNull ArenaType arenaType) {
        sidebarMap.computeIfPresent(arenaType, (aT, sidebar) -> {
            sidebar.destroy();
            return null;
        });
    }

    @Override
    public void addInvisiblePlayer(@NotNull GamePlayer gamePlayer) {
        invisiblePlayers.computeIfAbsent(gamePlayer.getUniqueId(), uuid -> {
            final var invisiblePlayer = gamePlayer.as(InvisiblePlayerImpl.class);
            if (!invisiblePlayer.isHidden()) {
                invisiblePlayer.hide();
            }
            return invisiblePlayer;
        });
    }

    @Override
    public void removeInvisiblePlayer(@NotNull GamePlayer gamePlayer) {
        invisiblePlayers.computeIfPresent(gamePlayer.getUniqueId(), ((uuid, invisiblePlayer) -> {
            if (invisiblePlayer.isHidden()) {
                invisiblePlayer.show();
            }
            return null;
        }));
    }

    @Override
    public boolean isPlayerInvisible(@NotNull GamePlayer gamePlayer) {
        return isPlayerInvisible(gamePlayer.getUniqueId());
    }

    @Override
    public boolean isPlayerInvisible(@NotNull UUID uuid) {
        final var invisiblePlayer = invisiblePlayers.get(uuid);
        if (invisiblePlayer == null) {
            return false;
        }

        if (!invisiblePlayer.isHidden()) {
            invisiblePlayers.remove(uuid);
            return false;
        }

        return true;
    }

    @NotNull
    @Override
    public Optional<InvisiblePlayer> getInvisiblePlayer(@NotNull GamePlayer gamePlayer) {
        return Optional.ofNullable(invisiblePlayers.get(gamePlayer.getUniqueId()));
    }

    @NotNull
    @Override
    public Optional<InvisiblePlayer> getInvisiblePlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(invisiblePlayers.get(uuid));
    }

    @Override
    public void registerGameTask(@NotNull GameTask gameTask) {
        if (gameTasks.contains(gameTask)) {
            return;
        }
        gameTasks.add(gameTask);
    }

    @Override
    public void unregisterGameTask(@NotNull GameTask gameTask) {
        if (!gameTasks.contains(gameTask)) {
            return;
        }

        if (gameTask.isRunning()) {
            gameTask.stop();
        }
        gameTasks.remove(gameTask);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T extends GameTask> Optional<T> getRunningGameTask(@NotNull Class<T> clazz) {
        return (Optional<T>) gameTasks
                .stream()
                .filter(gameTask -> gameTask.getClass().isAssignableFrom(clazz))
                .findAny();
    }

    @NotNull
    @Override
    public List<InvisiblePlayer> getInvisiblePlayers() {
        return List.copyOf(invisiblePlayers.values());
    }

    @NotNull
    @Override
    public List<GameStoreData> getGameStoreData() {
        return List.copyOf(gameStoreData);
    }

    @Override
    public void registerStoreNPC(@NotNull GameStoreData gameStoreData, @NotNull NPC npc) {
        npcMap.put(gameStoreData, npc);
    }

    @NotNull
    @Override
    public Map<GameStoreData, NPC> getRegisteredNPCS() {
        return Map.copyOf(npcMap);
    }

    @NotNull
    public Optional<GamePlayerData> getPlayerData(@NotNull UUID uuid) {
        return Optional.ofNullable(playerDataMap.get(uuid));
    }
}
