package io.github.pronze.sba.utils;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MockEntity implements LivingEntity {

    @Override
    public boolean equals(Object obj) {
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
        return false;
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
    public @NotNull Location getLocation() {
        return null;
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
    public @NotNull String getName() {
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
        return null;
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
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile) {
        return null;
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile, @Nullable Vector velocity) {
        return null;
    }
}
