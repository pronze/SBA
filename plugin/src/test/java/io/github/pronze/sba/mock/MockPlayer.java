package io.github.pronze.sba.mock;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.*;

public class MockPlayer implements Player {
    private static int PLAYER_COUNT = 1;

    private final UUID uuid = UUID.randomUUID();

    public MockPlayer() {
        PLAYER_COUNT += 1;
    }

    @Override
    public @NotNull Component displayName() {
        return Component.empty();
    }

    @Override
    public void displayName(@Nullable Component displayName) {

    }

    @Override
    public @NotNull String getDisplayName() {
        return "SBA_PLAYER_MOCK_" +  PLAYER_COUNT;
    }

    @Override
    public void setDisplayName(@Nullable String name) {

    }

    @Override
    public void playerListName(@Nullable Component name) {

    }

    @Override
    public @Nullable Component playerListName() {
        return null;
    }

    @Override
    public @Nullable Component playerListHeader() {
        return null;
    }

    @Override
    public @Nullable Component playerListFooter() {
        return null;
    }

    @Override
    public @NotNull String getPlayerListName() {
        return null;
    }

    @Override
    public void setPlayerListName(@Nullable String name) {

    }

    @Override
    public @Nullable String getPlayerListHeader() {
        return null;
    }

    @Override
    public @Nullable String getPlayerListFooter() {
        return null;
    }

    @Override
    public void setPlayerListHeader(@Nullable String header) {

    }

    @Override
    public void setPlayerListFooter(@Nullable String footer) {

    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable String header, @Nullable String footer) {

    }

    @Override
    public void setCompassTarget(@NotNull Location loc) {

    }

    @Override
    public @NotNull Location getCompassTarget() {
        return null;
    }

    @Override
    public @Nullable InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public @Nullable InetSocketAddress getVirtualHost() {
        return null;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(@NotNull String input) {

    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {

    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent details) {

    }

    @Override
    public void sendRawMessage(@NotNull String message) {

    }

    @Override
    public void sendRawMessage(@Nullable UUID sender, @NotNull String message) {

    }

    @Override
    public void kickPlayer(@Nullable String message) {

    }

    @Override
    public void kick(@Nullable Component message) {

    }

    @Override
    public void kick(@Nullable Component message, PlayerKickEvent.@NotNull Cause cause) {

    }

    @Override
    public void chat(@NotNull String msg) {

    }

    @Override
    public boolean performCommand(@NotNull String command) {
        return false;
    }

    @Override
    public @NotNull Location getLocation() {
        return new Location(null, 0,0,0,0,0);
    }

    @Override
    public @Nullable Location getLocation(@Nullable Location loc) {
        return null;
    }

    @Override
    public void setVelocity(@NotNull Vector velocity) {

    }

    @Override
    public @NotNull Vector getVelocity() {
        return null;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return null;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public @NotNull World getWorld() {
        return null;
    }

    @Override
    public void setRotation(float yaw, float pitch) {

    }

    @Override
    public boolean teleport(@NotNull Location location) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Location location, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Entity destination) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Entity destination, PlayerTeleportEvent.@NotNull TeleportCause cause) {
        return false;
    }

    @Override
    public @NotNull List<Entity> getNearbyEntities(double x, double y, double z) {
        return null;
    }

    @Override
    public int getEntityId() {
        return 0;
    }

    @Override
    public int getFireTicks() {
        return 0;
    }

    @Override
    public int getMaxFireTicks() {
        return 0;
    }

    @Override
    public void setFireTicks(int ticks) {

    }

    @Override
    public void remove() {

    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void sendMessage(@NotNull String message) {

    }

    @Override
    public void sendMessage(@NotNull String[] messages) {

    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {

    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String[] messages) {

    }

    @Override
    public @NotNull Server getServer() {
        return null;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void setPersistent(boolean persistent) {

    }

    @Override
    public @Nullable Entity getPassenger() {
        return null;
    }

    @Override
    public boolean setPassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public @NotNull List<Entity> getPassengers() {
        return null;
    }

    @Override
    public boolean addPassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public boolean removePassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public float getFallDistance() {
        return 0;
    }

    @Override
    public void setFallDistance(float distance) {

    }

    @Override
    public void setLastDamageCause(@Nullable EntityDamageEvent event) {

    }

    @Override
    public @Nullable EntityDamageEvent getLastDamageCause() {
        return null;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    @Override
    public int getTicksLived() {
        return 0;
    }

    @Override
    public void setTicksLived(int value) {

    }

    @Override
    public void playEffect(@NotNull EntityEffect type) {

    }

    @Override
    public @NotNull EntityType getType() {
        return null;
    }

    @Override
    public boolean isInsideVehicle() {
        return false;
    }

    @Override
    public boolean leaveVehicle() {
        return false;
    }

    @Override
    public @Nullable Entity getVehicle() {
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean flag) {

    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public void setGlowing(boolean flag) {

    }

    @Override
    public boolean isGlowing() {
        return false;
    }

    @Override
    public void setInvulnerable(boolean flag) {

    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public void setSilent(boolean flag) {

    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void setGravity(boolean gravity) {

    }

    @Override
    public int getPortalCooldown() {
        return 0;
    }

    @Override
    public void setPortalCooldown(int cooldown) {

    }

    @Override
    public @NotNull Set<String> getScoreboardTags() {
        return null;
    }

    @Override
    public boolean addScoreboardTag(@NotNull String tag) {
        return false;
    }

    @Override
    public boolean removeScoreboardTag(@NotNull String tag) {
        return false;
    }

    @Override
    public @NotNull PistonMoveReaction getPistonMoveReaction() {
        return null;
    }

    @Override
    public @NotNull BlockFace getFacing() {
        return null;
    }

    @Override
    public @NotNull Pose getPose() {
        return null;
    }

    @Override
    public boolean isSneaking() {
        return false;
    }

    @Override
    public void setSneaking(boolean sneak) {

    }

    @Override
    public boolean isSprinting() {
        return false;
    }

    @Override
    public void setSprinting(boolean sprinting) {

    }

    @Override
    public void saveData() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {

    }

    @Override
    public boolean isSleepingIgnored() {
        return false;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean value) {

    }

    @Override
    public @Nullable Player getPlayer() {
        return this;
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Override
    public @Nullable Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public long getLastLogin() {
        return 0;
    }

    @Override
    public long getLastSeen() {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, int newValue) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int newValue) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue) {

    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location) {

    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location, boolean force) {

    }

    @Override
    public void playNote(@NotNull Location loc, byte instrument, byte note) {

    }

    @Override
    public void playNote(@NotNull Location loc, @NotNull Instrument instrument, @NotNull Note note) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, float volume, float pitch) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String sound, float volume, float pitch) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void stopSound(@NotNull Sound sound) {

    }

    @Override
    public void stopSound(@NotNull String sound) {

    }

    @Override
    public void stopSound(@NotNull Sound sound, @Nullable SoundCategory category) {

    }

    @Override
    public void stopSound(@NotNull String sound, @Nullable SoundCategory category) {

    }

    @Override
    public void playEffect(@NotNull Location loc, @NotNull Effect effect, int data) {

    }

    @Override
    public <T> void playEffect(@NotNull Location loc, @NotNull Effect effect, @Nullable T data) {

    }

    @Override
    public void sendBlockChange(@NotNull Location loc, @NotNull Material material, byte data) {

    }

    @Override
    public void sendBlockChange(@NotNull Location loc, @NotNull BlockData block) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location loc, float progress) {

    }

    @Override
    public boolean sendChunkChange(@NotNull Location loc, int sx, int sy, int sz, @NotNull byte[] data) {
        return false;
    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable List<Component> lines) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable List<Component> lines, @NotNull DyeColor dyeColor) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable String[] lines) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable String[] lines, @NotNull DyeColor dyeColor) throws IllegalArgumentException {

    }

    @Override
    public void sendMap(@NotNull MapView map) {

    }

    @Override
    public void sendActionBar(@NotNull String message) {

    }

    @Override
    public void sendActionBar(char alternateChar, @NotNull String message) {

    }

    @Override
    public void sendActionBar(@NotNull BaseComponent... message) {

    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable BaseComponent[] header, @Nullable BaseComponent[] footer) {

    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable BaseComponent header, @Nullable BaseComponent footer) {

    }

    @Override
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void setSubtitle(BaseComponent[] subtitle) {

    }

    @Override
    public void setSubtitle(BaseComponent subtitle) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent[] title) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent title) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent[] title, @Nullable BaseComponent[] subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void sendTitle(@NotNull Title title) {

    }

    @Override
    public void updateTitle(@NotNull Title title) {

    }

    @Override
    public void hideTitle() {

    }

    @Override
    public void updateInventory() {

    }

    @Override
    public void setPlayerTime(long time, boolean relative) {

    }

    @Override
    public long getPlayerTime() {
        return 0;
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;
    }

    @Override
    public void resetPlayerTime() {

    }

    @Override
    public void setPlayerWeather(@NotNull WeatherType type) {

    }

    @Override
    public @Nullable WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void resetPlayerWeather() {

    }

    @Override
    public void giveExp(int amount, boolean applyMending) {

    }

    @Override
    public int applyMending(int amount) {
        return 0;
    }

    @Override
    public void giveExpLevels(int amount) {

    }

    @Override
    public float getExp() {
        return 0;
    }

    @Override
    public void setExp(float exp) {

    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public int getTotalExperience() {
        return 0;
    }

    @Override
    public void setTotalExperience(int exp) {

    }

    @Override
    public void sendExperienceChange(float progress) {

    }

    @Override
    public void sendExperienceChange(float progress, int level) {

    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public void setAllowFlight(boolean flight) {

    }

    @Override
    public void hidePlayer(@NotNull Player player) {

    }

    @Override
    public void hidePlayer(@NotNull Plugin plugin, @NotNull Player player) {

    }

    @Override
    public void showPlayer(@NotNull Player player) {

    }

    @Override
    public void showPlayer(@NotNull Plugin plugin, @NotNull Player player) {

    }

    @Override
    public boolean canSee(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public void setFlying(boolean value) {

    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {

    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {

    }

    @Override
    public float getFlySpeed() {
        return 0;
    }

    @Override
    public float getWalkSpeed() {
        return 0;
    }

    @Override
    public void setTexturePack(@NotNull String url) {

    }

    @Override
    public void setResourcePack(@NotNull String url) {

    }

    @Override
    public void setResourcePack(@NotNull String url, @NotNull byte[] hash) {

    }

    @Override
    public @NotNull Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public void setScoreboard(@NotNull Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public boolean isHealthScaled() {
        return false;
    }

    @Override
    public void setHealthScaled(boolean scale) {

    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {

    }

    @Override
    public double getHealthScale() {
        return 0;
    }

    @Override
    public @Nullable Entity getSpectatorTarget() {
        return null;
    }

    @Override
    public void setSpectatorTarget(@Nullable Entity entity) {

    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle) {

    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void resetTitle() {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, @Nullable T data) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, @Nullable T data) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {

    }

    @Override
    public @NotNull AdvancementProgress getAdvancementProgress(@NotNull Advancement advancement) {
        return null;
    }

    @Override
    public int getClientViewDistance() {
        return 0;
    }

    @Override
    public @NotNull Locale locale() {
        return null;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @Override
    public @NotNull String getLocale() {
        return null;
    }

    @Override
    public boolean getAffectsSpawning() {
        return false;
    }

    @Override
    public void setAffectsSpawning(boolean affects) {

    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public void setViewDistance(int viewDistance) {

    }

    @Override
    public void updateCommands() {

    }

    @Override
    public void openBook(@NotNull ItemStack book) {

    }

    @Override
    public void setResourcePack(@NotNull String url, @NotNull String hash) {

    }

    @Override
    public PlayerResourcePackStatusEvent.@Nullable Status getResourcePackStatus() {
        return null;
    }

    @Override
    public @Nullable String getResourcePackHash() {
        return null;
    }

    @Override
    public boolean hasResourcePack() {
        return false;
    }

    @Override
    public @NotNull PlayerProfile getPlayerProfile() {
        return null;
    }

    @Override
    public void setPlayerProfile(@NotNull PlayerProfile profile) {

    }

    @Override
    public float getCooldownPeriod() {
        return 0;
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        return 0;
    }

    @Override
    public void resetCooldown() {

    }

    @Override
    public <T> @NotNull T getClientOption(@NotNull ClientOption<T> option) {
        return null;
    }

    @Override
    public @Nullable Firework boostElytra(@NotNull ItemStack firework) {
        return null;
    }

    @Override
    public void sendOpLevel(byte level) {

    }

    @Override
    public @NotNull Set<Player> getTrackedPlayers() {
        return null;
    }

    @Override
    public @Nullable String getClientBrandName() {
        return null;
    }

    @Override
    public @NotNull Spigot spigot() {
        return null;
    }

    @Override
    public @Nullable Location getOrigin() {
        return null;
    }

    @Override
    public boolean fromMobSpawner() {
        return false;
    }

    @Override
    public @NotNull Chunk getChunk() {
        return null;
    }

    @Override
    public CreatureSpawnEvent.@NotNull SpawnReason getEntitySpawnReason() {
        return null;
    }

    @Override
    public boolean isInRain() {
        return false;
    }

    @Override
    public boolean isInBubbleColumn() {
        return false;
    }

    @Override
    public boolean isInWaterOrRain() {
        return false;
    }

    @Override
    public boolean isInWaterOrBubbleColumn() {
        return false;
    }

    @Override
    public boolean isInWaterOrRainOrBubbleColumn() {
        return false;
    }

    @Override
    public boolean isInLava() {
        return false;
    }

    @Override
    public boolean isTicking() {
        return false;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "SBA_PLAYER_MOCK_" + PLAYER_COUNT;
    }

    @Override
    public @NotNull PlayerInventory getInventory() {
        return null;
    }

    @Override
    public @NotNull Inventory getEnderChest() {
        return null;
    }

    @Override
    public @NotNull MainHand getMainHand() {
        return null;
    }

    @Override
    public boolean setWindowProperty(InventoryView.@NotNull Property prop, int value) {
        return false;
    }

    @Override
    public @NotNull InventoryView getOpenInventory() {
        return null;
    }

    @Override
    public @Nullable InventoryView openInventory(@NotNull Inventory inventory) {
        return null;
    }

    @Override
    public @Nullable InventoryView openWorkbench(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openEnchanting(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public void openInventory(@NotNull InventoryView inventory) {

    }

    @Override
    public @Nullable InventoryView openMerchant(@NotNull Villager trader, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openMerchant(@NotNull Merchant merchant, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openAnvil(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openCartographyTable(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openGrindstone(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openLoom(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openSmithingTable(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public @Nullable InventoryView openStonecutter(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public void closeInventory() {

    }

    @Override
    public void closeInventory(InventoryCloseEvent.@NotNull Reason reason) {

    }

    @Override
    public @NotNull ItemStack getItemInHand() {
        return null;
    }

    @Override
    public void setItemInHand(@Nullable ItemStack item) {

    }

    @Override
    public @NotNull ItemStack getItemOnCursor() {
        return null;
    }

    @Override
    public void setItemOnCursor(@Nullable ItemStack item) {

    }

    @Override
    public boolean hasCooldown(@NotNull Material material) {
        return false;
    }

    @Override
    public int getCooldown(@NotNull Material material) {
        return 0;
    }

    @Override
    public void setCooldown(@NotNull Material material, int ticks) {

    }

    @Override
    public boolean isDeeplySleeping() {
        return false;
    }

    @Override
    public int getSleepTicks() {
        return 0;
    }

    @Override
    public @Nullable Location getPotentialBedLocation() {
        return null;
    }

    @Override
    public boolean sleep(@NotNull Location location, boolean force) {
        return false;
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {

    }

    @Override
    public @NotNull Location getBedLocation() {
        return null;
    }

    @Override
    public @NotNull GameMode getGameMode() {
        return null;
    }

    @Override
    public void setGameMode(@NotNull GameMode mode) {

    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public double getEyeHeight() {
        return 0;
    }

    @Override
    public double getEyeHeight(boolean ignorePose) {
        return 0;
    }

    @Override
    public @NotNull Location getEyeLocation() {
        return null;
    }

    @Override
    public @NotNull List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public @NotNull Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public @Nullable Block getTargetBlock(int maxDistance, TargetBlockInfo.@NotNull FluidMode fluidMode) {
        return null;
    }

    @Override
    public @Nullable BlockFace getTargetBlockFace(int maxDistance, TargetBlockInfo.@NotNull FluidMode fluidMode) {
        return null;
    }

    @Override
    public @Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance, TargetBlockInfo.@NotNull FluidMode fluidMode) {
        return null;
    }

    @Override
    public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        return null;
    }

    @Override
    public @Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
        return null;
    }

    @Override
    public @NotNull List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public @Nullable Block getTargetBlockExact(int maxDistance) {
        return null;
    }

    @Override
    public @Nullable Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public @Nullable RayTraceResult rayTraceBlocks(double maxDistance) {
        return null;
    }

    @Override
    public @Nullable RayTraceResult rayTraceBlocks(double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public int getRemainingAir() {
        return 0;
    }

    @Override
    public void setRemainingAir(int ticks) {

    }

    @Override
    public int getMaximumAir() {
        return 0;
    }

    @Override
    public void setMaximumAir(int ticks) {

    }

    @Override
    public int getArrowCooldown() {
        return 0;
    }

    @Override
    public void setArrowCooldown(int ticks) {

    }

    @Override
    public int getArrowsInBody() {
        return 0;
    }

    @Override
    public void setArrowsInBody(int count) {

    }

    @Override
    public int getMaximumNoDamageTicks() {
        return 0;
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {

    }

    @Override
    public double getLastDamage() {
        return 0;
    }

    @Override
    public void setLastDamage(double damage) {

    }

    @Override
    public int getNoDamageTicks() {
        return 0;
    }

    @Override
    public void setNoDamageTicks(int ticks) {

    }

    @Override
    public @Nullable Player getKiller() {
        return null;
    }

    @Override
    public void setKiller(@Nullable Player killer) {

    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect) {
        return false;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
        return false;
    }

    @Override
    public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
        return false;
    }

    @Override
    public boolean hasPotionEffect(@NotNull PotionEffectType type) {
        return true;
    }

    @Override
    public @Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
        return null;
    }

    @Override
    public void removePotionEffect(@NotNull PotionEffectType type) {

    }

    @Override
    public @NotNull Collection<PotionEffect> getActivePotionEffects() {
        return null;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity other) {
        return false;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location location) {
        return false;
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return false;
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {

    }

    @Override
    public @Nullable EntityEquipment getEquipment() {
        return null;
    }

    @Override
    public void setCanPickupItems(boolean pickup) {

    }

    @Override
    public boolean getCanPickupItems() {
        return false;
    }

    @Override
    public boolean isLeashed() {
        return false;
    }

    @Override
    public @NotNull Entity getLeashHolder() throws IllegalStateException {
        return null;
    }

    @Override
    public boolean setLeashHolder(@Nullable Entity holder) {
        return false;
    }

    @Override
    public boolean isGliding() {
        return false;
    }

    @Override
    public void setGliding(boolean gliding) {

    }

    @Override
    public boolean isSwimming() {
        return false;
    }

    @Override
    public void setSwimming(boolean swimming) {

    }

    @Override
    public boolean isRiptiding() {
        return false;
    }

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    public void setAI(boolean ai) {

    }

    @Override
    public boolean hasAI() {
        return false;
    }

    @Override
    public void attack(@NotNull Entity target) {

    }

    @Override
    public void swingMainHand() {

    }

    @Override
    public void swingOffHand() {

    }

    @Override
    public void setCollidable(boolean collidable) {

    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public @NotNull Set<UUID> getCollidableExemptions() {
        return null;
    }

    @Override
    public <T> @Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
        return null;
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {

    }

    @Override
    public @NotNull EntityCategory getCategory() {
        return null;
    }

    @Override
    public void setInvisible(boolean invisible) {

    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public int getArrowsStuck() {
        return 0;
    }

    @Override
    public void setArrowsStuck(int arrows) {

    }

    @Override
    public int getShieldBlockingDelay() {
        return 0;
    }

    @Override
    public void setShieldBlockingDelay(int delay) {

    }

    @Override
    public @Nullable ItemStack getActiveItem() {
        return null;
    }

    @Override
    public void clearActiveItem() {

    }

    @Override
    public int getItemUseRemainingTime() {
        return 0;
    }

    @Override
    public int getHandRaisedTime() {
        return 0;
    }

    @Override
    public boolean isHandRaised() {
        return false;
    }

    @Override
    public @NotNull EquipmentSlot getHandRaised() {
        return null;
    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public void setJumping(boolean jumping) {

    }

    @Override
    public void playPickupItemAnimation(@NotNull Item item, int quantity) {

    }

    @Override
    public float getHurtDirection() {
        return 0;
    }

    @Override
    public void setHurtDirection(float hurtDirection) {

    }

    @Override
    public int getExpToLevel() {
        return 0;
    }

    @Override
    public @Nullable Entity releaseLeftShoulderEntity() {
        return null;
    }

    @Override
    public @Nullable Entity releaseRightShoulderEntity() {
        return null;
    }

    @Override
    public float getAttackCooldown() {
        return 0;
    }

    @Override
    public boolean discoverRecipe(@NotNull NamespacedKey recipe) {
        return false;
    }

    @Override
    public int discoverRecipes(@NotNull Collection<NamespacedKey> recipes) {
        return 0;
    }

    @Override
    public boolean undiscoverRecipe(@NotNull NamespacedKey recipe) {
        return false;
    }

    @Override
    public int undiscoverRecipes(@NotNull Collection<NamespacedKey> recipes) {
        return 0;
    }

    @Override
    public boolean hasDiscoveredRecipe(@NotNull NamespacedKey recipe) {
        return false;
    }

    @Override
    public @NotNull Set<NamespacedKey> getDiscoveredRecipes() {
        return null;
    }

    @Override
    public @Nullable Entity getShoulderEntityLeft() {
        return null;
    }

    @Override
    public void setShoulderEntityLeft(@Nullable Entity entity) {

    }

    @Override
    public @Nullable Entity getShoulderEntityRight() {
        return null;
    }

    @Override
    public void setShoulderEntityRight(@Nullable Entity entity) {

    }

    @Override
    public void openSign(@NotNull Sign sign) {

    }

    @Override
    public boolean dropItem(boolean dropAll) {
        return false;
    }

    @Override
    public float getExhaustion() {
        return 0;
    }

    @Override
    public void setExhaustion(float value) {

    }

    @Override
    public float getSaturation() {
        return 0;
    }

    @Override
    public void setSaturation(float value) {

    }

    @Override
    public int getFoodLevel() {
        return 0;
    }

    @Override
    public void setFoodLevel(int value) {

    }

    @Override
    public int getSaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setSaturatedRegenRate(int ticks) {

    }

    @Override
    public int getUnsaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setUnsaturatedRegenRate(int ticks) {

    }

    @Override
    public int getStarvationRate() {
        return 0;
    }

    @Override
    public void setStarvationRate(int ticks) {

    }

    @Override
    public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return null;
    }

    @Override
    public void registerAttribute(@NotNull Attribute attribute) {

    }

    @Override
    public void damage(double amount) {

    }

    @Override
    public void damage(double amount, @Nullable Entity source) {

    }

    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public void setHealth(double health) {

    }

    @Override
    public double getAbsorptionAmount() {
        return 0;
    }

    @Override
    public void setAbsorptionAmount(double amount) {

    }

    @Override
    public double getMaxHealth() {
        return 0;
    }

    @Override
    public void setMaxHealth(double health) {

    }

    @Override
    public void resetMaxHealth() {

    }

    @Override
    public @Nullable Component customName() {
        return null;
    }

    @Override
    public void customName(@Nullable Component customName) {

    }

    @Override
    public @Nullable String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(@Nullable String name) {

    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {

    }

    @Override
    public @NotNull List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        return null;
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return false;
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {

    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return false;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return false;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return null;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return null;
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {

    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return null;
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, @NotNull byte[] message) {

    }

    @Override
    public @NotNull Set<String> getListeningPluginChannels() {
        return null;
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile) {
        return null;
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile, @Nullable Vector velocity) {
        return null;
    }
}
