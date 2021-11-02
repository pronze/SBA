package io.github.pronze.sba.wrapper.game;

import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.data.GameStoreData;
import io.github.pronze.sba.game.GameStorage;
import io.github.pronze.sba.game.InvisiblePlayer;
import io.github.pronze.sba.game.RotatingGenerator;
import io.github.pronze.sba.game.tasks.GameTask;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.wrapper.store.GameStoreWrapper;
import io.github.pronze.sba.wrapper.team.RunningTeamWrapper;
import io.github.pronze.sba.wrapper.team.TeamWrapper;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.WorldHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface GameWrapper {

    static GameWrapper of(@NotNull Game game) {
        return AddonAPI.getInstance().getGameWrapperManager().wrapGame(game);
    }

    /**
     * Gets the GameStorage of the current arena.
     *
     * @return game storage of the current arena
     */
    @NotNull
    GameStorage getStorage();

    /**
     * Gets the Game instance that has been linked to this Arena.
     *
     * @return game object of this arena
     */
    @NotNull
    Game getGame();

    /**
     * Gets an optional that may or may not be empty depending on if the player data has been registered in the arena.
     *
     * @param playerUUID the uuid of the player to query
     * @return an optional {@link GamePlayerData} instance
     */
    Optional<GamePlayerData> getPlayerData(@NotNull UUID playerUUID);

    /**
     * Registers the player data to the arena.
     *
     * @param uuid the uuid instance of the player to register
     * @param data the value of the uuid key to be used
     */
    void registerPlayerData(@NotNull UUID uuid, @NotNull GamePlayerData data);

    /**
     * Unregisters the player data from the arena.
     *
     * @param uuid the uuid instance of the player to unregister
     */
    void unregisterPlayerData(@NotNull UUID uuid);

    /**
     * Gets whether the player is hidden from game players or not.
     *
     * @param player the player instance to query
     * @return true if the player is hidden from game players, false otherwise
     */
    boolean isPlayerHidden(@NotNull PlayerWrapper player);

    /**
     * Removes the player from its hidden state.
     *
     * @param player the player instance to remove
     */
    void removeHiddenPlayer(@NotNull PlayerWrapper player);

    /**
     * Adds the player as a hidden player in the arena.
     *
     * @param player the player instance to add
     */
    void addHiddenPlayer(@NotNull PlayerWrapper player);

    /**
     * Gets a list of players that are current invisible in the arena.
     *
     * @return a list of players that are currently invisible in the arena
     */
    @NotNull
    List<PlayerWrapper> getInvisiblePlayers();

    /**
     * Creates a floating generator above the location of spawner. This generator is upgradable.
     *
     * @param itemSpawner the item spawner instance to be rotated.
     * @param rotationMaterial the material used as the head of armor stand
     */
    void createRotatingGenerator(@NotNull ItemSpawner itemSpawner, @NotNull Material rotationMaterial);

    /**
     * @return a list containing all the normal store npc's registered to the arena.
     */
    @NotNull
    List<NPC> getStoreNPCS();

    /**
     * @return a list containing all the upgrade store npc's registered to the arena.
     */
    @NotNull
    List<NPC> getUpgradeStoreNPCS();

    /**
     * Gets the GameTask for this arena.
     *
     * @param <T> The Type of the task to query
     * @return an optional containing the task instance if present, empty otherwise.
     */
    <T extends GameTask> Optional<T> getTask(@NotNull Class<T> taskClass);

    /**
     *
     * @return
     */
    List<GameTask> getGameTasks();

    /**
     *
     * @return a list containing all the rotating generators registered to the arena.
     */
    List<RotatingGenerator> getRotatingGenerators();

    Optional<InvisiblePlayer> getHiddenPlayer(@NotNull UUID playerUUID);

    List<SBAPlayerWrapper> getConnectedPlayers();

    List<ItemSpawner> getItemSpawners();

    List<GameStoreWrapper> getGameStores();

    WorldHolder getGameWorld();

    LocationHolder getSpectatorSpawn();

    void registerUpgradeStoreNPC(@NotNull NPC npc);

    void registerStoreNPC(@NotNull NPC npc);

    void unregisterUpgradeStoreNPC(@NotNull NPC npc);

    void unregisterStoreNPC(@NotNull NPC npc);

    void start();

    void stop();

    Map<UUID, GamePlayerData> getPlayerDataMap();

    @NotNull
    List<RunningTeamWrapper> getRunningTeams();

    RunningTeamWrapper getTeamOfPlayer(@NotNull PlayerWrapper player);

    @NotNull
    String getName();

    GameStatus getStatus();

    int countAvailableTeams();

    String getFormattedTimeLeft();

    List<TeamWrapper> getAvailableTeams();

    boolean isPlayerConnected(@NotNull UUID queryId);

    boolean isPlayerConnected(@NotNull PlayerWrapper player);

    int getMinPlayers();

    @NotNull
    List<GameStoreData> getGameStoreData();
}
