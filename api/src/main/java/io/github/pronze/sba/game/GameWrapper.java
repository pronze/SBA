package io.github.pronze.sba.game;

import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.data.GameStoreData;
import io.github.pronze.sba.game.task.GameTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.utils.RawValueHolder;
import org.screamingsandals.lib.utils.Wrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface GameWrapper extends Wrapper, RawValueHolder {

    void destroy();

    void stop();

    void start();

    @NotNull
    Game getGame();

    @NotNull
    Optional<GamePlayerData> getPlayerData(@NotNull UUID uuid);

    void spectatorJoin(@NotNull GamePlayer gamePlayer);

    void leaveFromGame(@NotNull GamePlayer gamePlayer);

    @NotNull
    Optional<Sidebar> getSidebar(@NotNull ArenaType arenaType);

    void registerSidebar(@NotNull ArenaType arenaType,
                         @NotNull Sidebar sidebar);

    @NotNull
    Map<ArenaType, Sidebar> getRegisteredSidebars();

    void unregisterSidebar(@NotNull ArenaType arenaType);

    void addInvisiblePlayer(@NotNull GamePlayer gamePlayer);

    void removeInvisiblePlayer(@NotNull GamePlayer gamePlayer);

    boolean isPlayerInvisible(@NotNull GamePlayer gamePlayer);

    boolean isPlayerInvisible(@NotNull UUID uuid);

    @NotNull
    Optional<InvisiblePlayer> getInvisiblePlayer(@NotNull GamePlayer gamePlayer);

    @NotNull
    Optional<InvisiblePlayer> getInvisiblePlayer(@NotNull UUID uuid);

    void registerGameTask(@NotNull GameTask gameTask);

    void unregisterGameTask(@NotNull GameTask gameTask);

    @NotNull
     <T extends GameTask> Optional<T> getRunningGameTask(@NotNull Class<T> clazz);

    @NotNull
    List<InvisiblePlayer> getInvisiblePlayers();

    @NotNull
    List<GameStoreData> getGameStoreData();

    void registerStoreNPC(@NotNull GameStoreData gameStoreData, @NotNull NPC npc);

    @NotNull
    Map<GameStoreData, NPC> getRegisteredNPCS();
}
