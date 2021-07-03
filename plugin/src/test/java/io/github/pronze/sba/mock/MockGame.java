package io.github.pronze.sba.mock;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.*;
import org.screamingsandals.bedwars.api.boss.StatusBar;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.bedwars.api.special.SpecialItem;
import org.screamingsandals.bedwars.api.utils.DelayFactory;

import java.util.List;

@RequiredArgsConstructor
public class MockGame implements Game {
    private final String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GameStatus getStatus() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void joinToGame(Player player) {

    }

    @Override
    public void leaveFromGame(Player player) {

    }

    @Override
    public void selectPlayerTeam(Player player, Team team) {

    }

    @Override
    public void selectPlayerRandomTeam(Player player) {

    }

    @Override
    public World getGameWorld() {
        return null;
    }

    @Override
    public Location getPos1() {
        return null;
    }

    @Override
    public Location getPos2() {
        return null;
    }

    @Override
    public Location getSpectatorSpawn() {
        return null;
    }

    @Override
    public int getGameTime() {
        return 0;
    }

    @Override
    public int getMinPlayers() {
        return 0;
    }

    @Override
    public int getMaxPlayers() {
        return 0;
    }

    @Override
    public int countConnectedPlayers() {
        return 0;
    }

    @Override
    public List<Player> getConnectedPlayers() {
        return null;
    }

    @Override
    public List<GameStore> getGameStores() {
        return null;
    }

    @Override
    public int countGameStores() {
        return 0;
    }

    @Override
    public Team getTeamFromName(String s) {
        return null;
    }

    @Override
    public List<Team> getAvailableTeams() {
        return null;
    }

    @Override
    public int countAvailableTeams() {
        return 0;
    }

    @Override
    public List<RunningTeam> getRunningTeams() {
        return null;
    }

    @Override
    public int countRunningTeams() {
        return 0;
    }

    @Override
    public RunningTeam getTeamOfPlayer(Player player) {
        return null;
    }

    @Override
    public boolean isPlayerInAnyTeam(Player player) {
        return false;
    }

    @Override
    public boolean isPlayerInTeam(Player player, RunningTeam runningTeam) {
        return false;
    }

    @Override
    public boolean isLocationInArena(Location location) {
        return false;
    }

    @Override
    public boolean isBlockAddedDuringGame(Location location) {
        return false;
    }

    @Override
    public List<SpecialItem> getActivedSpecialItems() {
        return null;
    }

    @Override
    public List<SpecialItem> getActivedSpecialItems(Class<? extends SpecialItem> aClass) {
        return null;
    }

    @Override
    public List<SpecialItem> getActivedSpecialItemsOfTeam(Team team) {
        return null;
    }

    @Override
    public List<SpecialItem> getActivedSpecialItemsOfTeam(Team team, Class<? extends SpecialItem> aClass) {
        return null;
    }

    @Override
    public SpecialItem getFirstActivedSpecialItemOfTeam(Team team) {
        return null;
    }

    @Override
    public SpecialItem getFirstActivedSpecialItemOfTeam(Team team, Class<? extends SpecialItem> aClass) {
        return null;
    }

    @Override
    public List<SpecialItem> getActivedSpecialItemsOfPlayer(Player player) {
        return null;
    }

    @Override
    public List<SpecialItem> getActivedSpecialItemsOfPlayer(Player player, Class<? extends SpecialItem> aClass) {
        return null;
    }

    @Override
    public SpecialItem getFirstActivedSpecialItemOfPlayer(Player player) {
        return null;
    }

    @Override
    public SpecialItem getFirstActivedSpecialItemOfPlayer(Player player, Class<? extends SpecialItem> aClass) {
        return null;
    }

    @Override
    public List<DelayFactory> getActiveDelays() {
        return null;
    }

    @Override
    public List<DelayFactory> getActiveDelaysOfPlayer(Player player) {
        return null;
    }

    @Override
    public DelayFactory getActiveDelay(Player player, Class<? extends SpecialItem> aClass) {
        return null;
    }

    @Override
    public void registerDelay(DelayFactory delayFactory) {

    }

    @Override
    public void unregisterDelay(DelayFactory delayFactory) {

    }

    @Override
    public boolean isDelayActive(Player player, Class<? extends SpecialItem> aClass) {
        return false;
    }

    @Override
    public void registerSpecialItem(SpecialItem specialItem) {

    }

    @Override
    public void unregisterSpecialItem(SpecialItem specialItem) {

    }

    @Override
    public boolean isRegisteredSpecialItem(SpecialItem specialItem) {
        return false;
    }

    @Override
    public List<ItemSpawner> getItemSpawners() {
        return null;
    }

    @Override
    public Region getRegion() {
        return null;
    }

    @Override
    public StatusBar getStatusBar() {
        return null;
    }

    @Override
    public World getLobbyWorld() {
        return null;
    }

    @Override
    public Location getLobbySpawn() {
        return null;
    }

    @Override
    public int getLobbyCountdown() {
        return 0;
    }

    @Override
    public int countTeamChests() {
        return 0;
    }

    @Override
    public int countTeamChests(RunningTeam runningTeam) {
        return 0;
    }

    @Override
    public RunningTeam getTeamOfChest(Location location) {
        return null;
    }

    @Override
    public RunningTeam getTeamOfChest(Block block) {
        return null;
    }

    @Override
    public boolean isEntityShop(Entity entity) {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getCompassEnabled() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedCompassEnabled() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getJoinRandomTeamAfterLobby() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedJoinRandomTeamAfterLobby() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getJoinRandomTeamOnJoin() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedJoinRandomTeamOnJoin() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getAddWoolToInventoryOnJoin() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedAddWoolToInventoryOnJoin() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getPreventKillingVillagers() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedPreventKillingVillagers() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getPlayerDrops() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedPlayerDrops() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getFriendlyfire() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedFriendlyfire() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getColoredLeatherByTeamInLobby() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedColoredLeatherByTeamInLobby() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getKeepInventory() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedKeepInventory() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getCrafting() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedCrafting() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getLobbyBossbar() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedLobbyBossbar() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getGameBossbar() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedGameBossbar() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getScoreboard() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedScoreaboard() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getLobbyScoreboard() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedLobbyScoreaboard() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getPreventSpawningMobs() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedPreventSpawningMobs() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getSpawnerHolograms() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedSpawnerHolograms() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getSpawnerDisableMerge() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedSpawnerDisableMerge() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getGameStartItems() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedGameStartItems() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getPlayerRespawnItems() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedPlayerRespawnItems() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getSpawnerHologramsCountdown() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedSpawnerHologramsCountdown() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getDamageWhenPlayerIsNotInArena() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedDamageWhenPlayerIsNotInArena() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getRemoveUnusedTargetBlocks() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedRemoveUnusedTargetBlocks() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getAllowBlockFalling() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedAllowBlockFalling() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getHoloAboveBed() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedHoloAboveBed() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getSpectatorJoin() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedSpectatorJoin() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getStopTeamSpawnersOnDie() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedStopTeamSpawnersOnDie() {
        return false;
    }

    @Override
    public boolean getBungeeEnabled() {
        return false;
    }

    @Override
    public ArenaTime getArenaTime() {
        return null;
    }

    @Override
    public WeatherType getArenaWeather() {
        return null;
    }

    @Override
    public BarColor getLobbyBossBarColor() {
        return null;
    }

    @Override
    public BarColor getGameBossBarColor() {
        return null;
    }

    @Override
    public boolean isProtectionActive(Player player) {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getAnchorAutoFill() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedAnchorAutoFill() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getAnchorDecreasing() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedAnchorDecreasing() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getCakeTargetBlockEating() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedCakeTargetBlockEating() {
        return false;
    }

    @Override
    public InGameConfigBooleanConstants getTargetBlockExplosions() {
        return null;
    }

    @Override
    public boolean getOriginalOrInheritedTargetBlockExplosions() {
        return false;
    }

    @Override
    public int getPostGameWaiting() {
        return 0;
    }

    @Override
    public String getCustomPrefix() {
        return null;
    }
}
