package io.github.pronze.sba.utils.citizens;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.destroystokyo.paper.profile.PlayerProfile;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
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

import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.GameModeTrait;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public class AIPlayer implements Player {

    @Getter
    private NPC npc;

    private Player getCurrentPlayerObject() {
        return (Player) npc.getEntity();
    }

    public AIPlayer(NPC npc) {
        this.npc = npc;

    }

    @Override
    public @NotNull String getName() {
        return npc.getName();
    }

    @Override
    public @NotNull PlayerInventory getInventory() {
        return getCurrentPlayerObject().getInventory();
    }

    @Override
    public @NotNull Inventory getEnderChest() {
        return getCurrentPlayerObject().getEnderChest();
    }

    @Override
    public @NotNull MainHand getMainHand() {
        return getCurrentPlayerObject().getMainHand();
    }

    @Override
    public boolean setWindowProperty(@NotNull Property prop, int value) {
        return getCurrentPlayerObject().setWindowProperty(prop, value);
    }

    @Override
    public @NotNull InventoryView getOpenInventory() {
        return getCurrentPlayerObject().getOpenInventory();
    }

    @Override
    public @Nullable InventoryView openInventory(@NotNull Inventory inventory) {
        return getCurrentPlayerObject().openInventory(inventory);
    }

    @Override
    public @Nullable InventoryView openWorkbench(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openWorkbench(location, force);
    }

    @Override
    public @Nullable InventoryView openEnchanting(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openEnchanting(location, force);
    }

    @Override
    public void openInventory(@NotNull InventoryView inventory) {
        getCurrentPlayerObject().openInventory(inventory);
    }

    @Override
    public @Nullable InventoryView openMerchant(@NotNull Villager trader, boolean force) {
        return getCurrentPlayerObject().openMerchant(trader, force);
    }

    @Override
    public @Nullable InventoryView openMerchant(@NotNull Merchant merchant, boolean force) {
        return getCurrentPlayerObject().openMerchant(merchant, force);
    }

    @Override
    public @Nullable InventoryView openAnvil(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openAnvil(location, force);
    }

    @Override
    public @Nullable InventoryView openCartographyTable(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openCartographyTable(location, force);
    }

    @Override
    public @Nullable InventoryView openGrindstone(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openGrindstone(location, force);
    }

    @Override
    public @Nullable InventoryView openLoom(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openLoom(location, force);
    }

    @Override
    public @Nullable InventoryView openSmithingTable(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openSmithingTable(location, force);
    }

    @Override
    public @Nullable InventoryView openStonecutter(@Nullable Location location, boolean force) {
        return getCurrentPlayerObject().openStonecutter(location, force);
    }

    @Override
    public void closeInventory() {
        getCurrentPlayerObject().closeInventory();
    }

    @Override
    public void closeInventory(@NotNull Reason reason) {
        getCurrentPlayerObject().closeInventory(reason);
    }

    @Override
    public @NotNull ItemStack getItemInHand() {
        return getCurrentPlayerObject().getItemInHand();
    }

    @Override
    public void setItemInHand(@Nullable ItemStack item) {
        getCurrentPlayerObject().setItemInHand(item);
    }

    @Override
    public @NotNull ItemStack getItemOnCursor() {
        return getCurrentPlayerObject().getItemOnCursor();
    }

    @Override
    public void setItemOnCursor(@Nullable ItemStack item) {
        getCurrentPlayerObject().setItemOnCursor(item);
    }

    @Override
    public boolean hasCooldown(@NotNull Material material) {
        return getCurrentPlayerObject().hasCooldown(material);
    }

    @Override
    public int getCooldown(@NotNull Material material) {
        return getCurrentPlayerObject().getCooldown(material);
    }

    @Override
    public void setCooldown(@NotNull Material material, int ticks) {
        getCurrentPlayerObject().setCooldown(material, ticks);
    }

    @Override
    public boolean isDeeplySleeping() {
        return getCurrentPlayerObject().isDeeplySleeping();
    }

    @Override
    public int getSleepTicks() {
        return getCurrentPlayerObject().getSleepTicks();
    }

    @Override
    public @Nullable Location getPotentialBedLocation() {
        return getCurrentPlayerObject().getPotentialBedLocation();
    }

    @Override
    public boolean sleep(@NotNull Location location, boolean force) {
        return getCurrentPlayerObject().sleep(location, force);
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {
        getCurrentPlayerObject().wakeup(setSpawnLocation);
    }

    @Override
    public @NotNull Location getBedLocation() {
        return getCurrentPlayerObject().getBedLocation();
    }

    @Override
    public @NotNull GameMode getGameMode() {
        var gameModeTrait = npc.getOrAddTrait(GameModeTrait.class);
        return gameModeTrait.getGameMode();
    }

    @Override
    public void setGameMode(@NotNull GameMode mode) {
        var gameModeTrait = npc.getOrAddTrait(GameModeTrait.class);
        gameModeTrait.setGameMode(mode);
    }

    @Override
    public boolean isBlocking() {
        return getCurrentPlayerObject().isBlocking();
    }

    @Override
    public boolean isHandRaised() {
        return getCurrentPlayerObject().isHandRaised();
    }

    @Override
    public int getExpToLevel() {
        return 0;
    }

    @Override
    public @Nullable Entity releaseLeftShoulderEntity() {
        return getCurrentPlayerObject().releaseLeftShoulderEntity();
    }

    @Override
    public @Nullable Entity releaseRightShoulderEntity() {
        return getCurrentPlayerObject().releaseRightShoulderEntity();
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
        getCurrentPlayerObject().openSign(sign);
    }

    @Override
    public boolean dropItem(boolean dropAll) {
        return getCurrentPlayerObject().dropItem(dropAll);
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
        return getCurrentPlayerObject().getFoodLevel();
    }

    @Override
    public void setFoodLevel(int value) {
        getCurrentPlayerObject().setFoodLevel(value);
    }

    @Override
    public int getSaturatedRegenRate() {
        return getCurrentPlayerObject().getSaturatedRegenRate();
    }

    @Override
    public void setSaturatedRegenRate(int ticks) {
        getCurrentPlayerObject().setSaturatedRegenRate(ticks);
    }

    @Override
    public int getUnsaturatedRegenRate() {
        return getCurrentPlayerObject().getUnsaturatedRegenRate();
    }

    @Override
    public void setUnsaturatedRegenRate(int ticks) {
        getCurrentPlayerObject().setUnsaturatedRegenRate(ticks);
    }

    @Override
    public int getStarvationRate() {
        return 0;
    }

    @Override
    public void setStarvationRate(int ticks) {

    }

    @Override
    public double getEyeHeight() {
        return getCurrentPlayerObject().getEyeHeight();
    }

    @Override
    public double getEyeHeight(boolean ignorePose) {
        return getCurrentPlayerObject().getEyeHeight(ignorePose);
    }

    @Override
    public @NotNull Location getEyeLocation() {
        return getCurrentPlayerObject().getEyeLocation();
    }

    @Override
    public @NotNull List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
        return getCurrentPlayerObject().getLineOfSight(transparent, maxDistance);
    }

    @Override
    public @NotNull Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
        return getCurrentPlayerObject().getTargetBlock(transparent, maxDistance);
    }

    @Override
    public @Nullable Block getTargetBlock(int maxDistance, @NotNull FluidMode fluidMode) {
        return getCurrentPlayerObject().getTargetBlock(maxDistance, fluidMode);
    }

    @Override
    public @Nullable BlockFace getTargetBlockFace(int maxDistance, @NotNull FluidMode fluidMode) {
        return getCurrentPlayerObject().getTargetBlockFace(maxDistance, fluidMode);
    }

    @Override
    public @Nullable TargetBlockInfo getTargetBlockInfo(int maxDistance, @NotNull FluidMode fluidMode) {
        return getCurrentPlayerObject().getTargetBlockInfo(maxDistance, fluidMode);
    }

    @Override
    public @Nullable Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        return getCurrentPlayerObject().getTargetEntity(maxDistance, ignoreBlocks);
    }

    @Override
    public @Nullable TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
        return getCurrentPlayerObject().getTargetEntityInfo(maxDistance, ignoreBlocks);
    }

    @Override
    public @NotNull List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
        return getCurrentPlayerObject().getLastTwoTargetBlocks(transparent, maxDistance);
    }

    @Override
    public @Nullable Block getTargetBlockExact(int maxDistance) {
        return getCurrentPlayerObject().getTargetBlockExact(maxDistance);
    }

    @Override
    public @Nullable Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return getCurrentPlayerObject().getTargetBlockExact(maxDistance, fluidCollisionMode);
    }

    @Override
    public @Nullable RayTraceResult rayTraceBlocks(double maxDistance) {
        return getCurrentPlayerObject().rayTraceBlocks(maxDistance);
    }

    @Override
    public @Nullable RayTraceResult rayTraceBlocks(double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return getCurrentPlayerObject().rayTraceBlocks(maxDistance, fluidCollisionMode);
    }

    @Override
    public int getRemainingAir() {
        return getCurrentPlayerObject().getRemainingAir();
    }

    @Override
    public void setRemainingAir(int ticks) {
        getCurrentPlayerObject().setRemainingAir(ticks);
    }

    @Override
    public int getMaximumAir() {
        return getCurrentPlayerObject().getMaximumAir();
    }

    @Override
    public void setMaximumAir(int ticks) {
        getCurrentPlayerObject().setMaximumAir(ticks);
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
        return getCurrentPlayerObject().getMaximumNoDamageTicks();
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        getCurrentPlayerObject().setMaximumNoDamageTicks(ticks);
        ;
    }

    @Override
    public double getLastDamage() {
        return getCurrentPlayerObject().getLastDamage();
    }

    @Override
    public void setLastDamage(double damage) {
        getCurrentPlayerObject().setLastDamage(damage);
    }

    @Override
    public int getNoDamageTicks() {
        return getCurrentPlayerObject().getNoDamageTicks();
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        getCurrentPlayerObject().setNoDamageTicks(ticks);
    }

    Player killer;

    @Override
    public @Nullable Player getKiller() {
        if (killer != null)
            return killer;
        else
            return getCurrentPlayerObject().getKiller();
    }

    @Override
    public void setKiller(@Nullable Player killer) {
        this.killer = killer;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect) {
        return getCurrentPlayerObject().addPotionEffect(effect);
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
        return getCurrentPlayerObject().addPotionEffect(effect, force);

    }

    @Override
    public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
        return getCurrentPlayerObject().addPotionEffects(effects);
    }

    @Override
    public boolean hasPotionEffect(@NotNull PotionEffectType type) {
        return getCurrentPlayerObject().hasPotionEffect(type);
    }

    @Override
    public @Nullable PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
        return getCurrentPlayerObject().getPotionEffect(type);
    }

    @Override
    public void removePotionEffect(@NotNull PotionEffectType type) {
        getCurrentPlayerObject().removePotionEffect(type);
    }

    @Override
    public @NotNull Collection<PotionEffect> getActivePotionEffects() {
        return getCurrentPlayerObject().getActivePotionEffects();
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity other) {
        return getCurrentPlayerObject().hasLineOfSight(other);
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location location) {
        return getCurrentPlayerObject().hasLineOfSight(location);
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return getCurrentPlayerObject().getRemoveWhenFarAway();
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
        getCurrentPlayerObject().setRemoveWhenFarAway(remove);

    }

    @Override
    public @Nullable EntityEquipment getEquipment() {
        return getCurrentPlayerObject().getEquipment();
    }

    @Override
    public void setCanPickupItems(boolean pickup) {
        getCurrentPlayerObject().setCanPickupItems(pickup);
    }

    @Override
    public boolean getCanPickupItems() {
        return getCurrentPlayerObject().getCanPickupItems();
    }

    @Override
    public boolean isLeashed() {
        return getCurrentPlayerObject().isLeashed();
    }

    @Override
    public @NotNull Entity getLeashHolder() throws IllegalStateException {
        return getCurrentPlayerObject().getLeashHolder();
    }

    @Override
    public boolean setLeashHolder(@Nullable Entity holder) {
        return getCurrentPlayerObject().setLeashHolder(holder);
    }

    @Override
    public boolean isGliding() {
        return getCurrentPlayerObject().isGliding();
    }

    @Override
    public void setGliding(boolean gliding) {
        getCurrentPlayerObject().setGliding(gliding);
    }

    @Override
    public boolean isSwimming() {
        return getCurrentPlayerObject().isSwimming();
    }

    @Override
    public void setSwimming(boolean swimming) {
        getCurrentPlayerObject().setSwimming(swimming);
    }

    @Override
    public boolean isRiptiding() {
        return getCurrentPlayerObject().isRiptiding();
    }

    @Override
    public boolean isSleeping() {
        return getCurrentPlayerObject().isSleeping();
    }

    @Override
    public void setAI(boolean ai) {

    }

    @Override
    public boolean hasAI() {
        return true;
    }

    @Override
    public void attack(@NotNull Entity target) {
        getCurrentPlayerObject().attack(target);
    }

    @Override
    public void swingMainHand() {
        getCurrentPlayerObject().swingMainHand();
    }

    @Override
    public void swingOffHand() {
        getCurrentPlayerObject().swingOffHand();
    }

    @Override
    public void setCollidable(boolean collidable) {
        getCurrentPlayerObject().setCollidable(collidable);
    }

    @Override
    public boolean isCollidable() {
        return getCurrentPlayerObject().isCollidable();
    }

    @Override
    public @NotNull Set<UUID> getCollidableExemptions() {
        return getCurrentPlayerObject().getCollidableExemptions();
    }

    @Override
    public <T> @Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
        return getCurrentPlayerObject().getMemory(memoryKey);
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {
        getCurrentPlayerObject().setMemory(memoryKey, memoryValue);
    }

    @Override
    public @NotNull EntityCategory getCategory() {
        return getCurrentPlayerObject().getCategory();
    }

    @Override
    public void setInvisible(boolean invisible) {
        getCurrentPlayerObject().setInvisible(invisible);
    }

    @Override
    public boolean isInvisible() {
        return getCurrentPlayerObject().isInvisible();
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
        return getCurrentPlayerObject().getActiveItem();
    }

    @Override
    public void clearActiveItem() {
        getCurrentPlayerObject().clearActiveItem();
    }

    @Override
    public int getItemUseRemainingTime() {
        return getCurrentPlayerObject().getItemUseRemainingTime();
    }

    @Override
    public int getHandRaisedTime() {
        return getCurrentPlayerObject().getHandRaisedTime();
    }

    @Override
    public @NotNull EquipmentSlot getHandRaised() {
        return getCurrentPlayerObject().getHandRaised();
    }

    @Override
    public boolean isJumping() {
        return getCurrentPlayerObject().isJumping();
    }

    @Override
    public void setJumping(boolean jumping) {
        getCurrentPlayerObject().setJumping(jumping);
    }

    @Override
    public void playPickupItemAnimation(@NotNull Item item, int quantity) {
        getCurrentPlayerObject().playPickupItemAnimation(item, quantity);
    }

    @Override
    public float getHurtDirection() {
        return getCurrentPlayerObject().getHurtDirection();
    }

    @Override
    public void setHurtDirection(float hurtDirection) {
        getCurrentPlayerObject().setHurtDirection(hurtDirection);
    }

    @Override
    public @Nullable AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return getCurrentPlayerObject().getAttribute(attribute);
    }

    @Override
    public void registerAttribute(@NotNull Attribute attribute) {
        getCurrentPlayerObject().registerAttribute(attribute);
    }

    @Override
    public void damage(double amount) {
        getCurrentPlayerObject().damage(amount);
    }

    @Override
    public void damage(double amount, @Nullable Entity source) {
        getCurrentPlayerObject().damage(amount, source);
    }

    @Override
    public double getHealth() {
        if (getCurrentPlayerObject() == null)
            return 0;
        return getCurrentPlayerObject().getHealth();
    }

    @Override
    public void setHealth(double health) {
        getCurrentPlayerObject().setHealth(health);
    }

    @Override
    public double getAbsorptionAmount() {
        return getCurrentPlayerObject().getAbsorptionAmount();
    }

    @Override
    public void setAbsorptionAmount(double amount) {
        getCurrentPlayerObject().setAbsorptionAmount(amount);
    }

    @Override
    public double getMaxHealth() {
        return getCurrentPlayerObject().getMaxHealth();
    }

    @Override
    public void setMaxHealth(double health) {
        getCurrentPlayerObject().setMaxHealth(health);
    }

    @Override
    public void resetMaxHealth() {
        getCurrentPlayerObject().resetMaxHealth();
    }

    @Override
    public @NotNull Location getLocation() {
        return npc.getOrAddTrait(CurrentLocation.class).getLocation();
    }

    @Override
    public @Nullable Location getLocation(@Nullable Location loc) {
        return getCurrentPlayerObject().getLocation(loc);
    }

    @Override
    public void setVelocity(@NotNull Vector velocity) {
        getCurrentPlayerObject().setVelocity(velocity);
    }

    @Override
    public @NotNull Vector getVelocity() {
        return getCurrentPlayerObject().getVelocity();
    }

    @Override
    public double getHeight() {
        return getCurrentPlayerObject().getHeight();
    }

    @Override
    public double getWidth() {
        return getCurrentPlayerObject().getWidth();
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return getCurrentPlayerObject().getBoundingBox();
    }

    @Override
    public boolean isInWater() {
        return getCurrentPlayerObject().isInWater();
    }

    @Override
    public @NotNull World getWorld() {
        return npc.getOrAddTrait(CurrentLocation.class).getLocation().getWorld();
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        getCurrentPlayerObject().setRotation(yaw, pitch);
    }

    @Override
    public boolean teleport(@NotNull Location location) {
        return getCurrentPlayerObject().teleport(location);
    }

    @Override
    public boolean teleport(@NotNull Location location, @NotNull TeleportCause cause) {
        return getCurrentPlayerObject().teleport(location, cause);
    }

    @Override
    public boolean teleport(@NotNull Entity destination) {
        return getCurrentPlayerObject().teleport(destination);
    }

    @Override
    public boolean teleport(@NotNull Entity destination, @NotNull TeleportCause cause) {
        return getCurrentPlayerObject().teleport(destination, cause);
    }

    @Override
    public @NotNull List<Entity> getNearbyEntities(double x, double y, double z) {
        return getCurrentPlayerObject().getNearbyEntities(x, y, z);
    }

    @Override
    public int getEntityId() {
        return getCurrentPlayerObject().getEntityId();
    }

    @Override
    public int getFireTicks() {
        return getCurrentPlayerObject().getFireTicks();
    }

    @Override
    public int getMaxFireTicks() {
        return getCurrentPlayerObject().getMaxFireTicks();
    }

    @Override
    public void setFireTicks(int ticks) {
        getCurrentPlayerObject().setFireTicks(ticks);
    }

    @Override
    public void remove() {
        if (getCurrentPlayerObject() != null)
            getCurrentPlayerObject().remove();
        npc.despawn();
    }

    @Override
    public boolean isDead() {
        return !npc.isSpawned() || getCurrentPlayerObject().isDead();
    }

    @Override
    public boolean isValid() {
        return npc.isSpawned();
    }

    @Override
    public @NotNull Server getServer() {
        return Bukkit.getServer();
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
        return getCurrentPlayerObject().isEmpty();
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public float getFallDistance() {
        return getCurrentPlayerObject().getFallDistance();
    }

    @Override
    public void setFallDistance(float distance) {
        getCurrentPlayerObject().setFallDistance(distance);
    }

    @Override
    public void setLastDamageCause(@Nullable EntityDamageEvent event) {
        getCurrentPlayerObject().setLastDamageCause(event);
    }

    @Override
    public @Nullable EntityDamageEvent getLastDamageCause() {
        return getCurrentPlayerObject().getLastDamageCause();
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return npc.getUniqueId();
    }

    @Override
    public int getTicksLived() {
        return getCurrentPlayerObject().getTicksLived();
    }

    @Override
    public void setTicksLived(int value) {
        getCurrentPlayerObject().setTicksLived(value);
    }

    @Override
    public void playEffect(@NotNull EntityEffect type) {

    }

    @Override
    public @NotNull EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public boolean isInsideVehicle() {
        return getCurrentPlayerObject().isInsideVehicle();
    }

    @Override
    public boolean leaveVehicle() {
        return getCurrentPlayerObject().leaveVehicle();
    }

    @Override
    public @Nullable Entity getVehicle() {
        return getCurrentPlayerObject().getVehicle();
    }

    boolean customNameVisible = false;

    @Override
    public void setCustomNameVisible(boolean flag) {
        customNameVisible = flag;
        if (npc.isSpawned())
            getCurrentPlayerObject().setCustomNameVisible(flag);
    }

    @Override
    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    @Override
    public void setGlowing(boolean flag) {
        getCurrentPlayerObject().setGlowing(flag);
    }

    @Override
    public boolean isGlowing() {
        return getCurrentPlayerObject().isGlowing();
    }

    @Override
    public void setInvulnerable(boolean flag) {
        npc.setProtected(flag);
        getCurrentPlayerObject().setInvulnerable(flag);
    }

    @Override
    public boolean isInvulnerable() {
        return npc.isProtected();
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
        return getCurrentPlayerObject().hasGravity();
    }

    @Override
    public void setGravity(boolean gravity) {
        getCurrentPlayerObject().setGravity(gravity);
    }

    @Override
    public int getPortalCooldown() {
        return getCurrentPlayerObject().getPortalCooldown();
    }

    @Override
    public void setPortalCooldown(int cooldown) {
        getCurrentPlayerObject().setPortalCooldown(cooldown);
    }

    @Override
    public @NotNull Set<String> getScoreboardTags() {
        return getCurrentPlayerObject().getScoreboardTags();
    }

    @Override
    public boolean addScoreboardTag(@NotNull String tag) {
        return getCurrentPlayerObject().addScoreboardTag(tag);
    }

    @Override
    public boolean removeScoreboardTag(@NotNull String tag) {
        return getCurrentPlayerObject().removeScoreboardTag(tag);
    }

    @Override
    public @NotNull PistonMoveReaction getPistonMoveReaction() {
        return getCurrentPlayerObject().getPistonMoveReaction();
    }

    @Override
    public @NotNull BlockFace getFacing() {
        return getCurrentPlayerObject().getFacing();
    }

    @Override
    public @NotNull Pose getPose() {
        return getCurrentPlayerObject().getPose();
    }

    @Override
    public @Nullable Location getOrigin() {
        return getCurrentPlayerObject().getOrigin();
    }

    @Override
    public boolean fromMobSpawner() {
        return getCurrentPlayerObject().fromMobSpawner();
    }

    @Override
    public @NotNull Chunk getChunk() {
        return getCurrentPlayerObject().getChunk();
    }

    @Override
    public @NotNull SpawnReason getEntitySpawnReason() {
        return getCurrentPlayerObject().getEntitySpawnReason();
    }

    @Override
    public boolean isInRain() {
        return getCurrentPlayerObject().isInRain();
    }

    @Override
    public boolean isInBubbleColumn() {
        return getCurrentPlayerObject().isInBubbleColumn();
    }

    @Override
    public boolean isInWaterOrRain() {
        return getCurrentPlayerObject().isInWaterOrRain();
    }

    @Override
    public boolean isInWaterOrBubbleColumn() {
        return getCurrentPlayerObject().isInWaterOrBubbleColumn();
    }

    @Override
    public boolean isInWaterOrRainOrBubbleColumn() {
        return getCurrentPlayerObject().isInWaterOrRainOrBubbleColumn();
    }

    @Override
    public boolean isInLava() {
        return getCurrentPlayerObject().isInLava();
    }

    @Override
    public boolean isTicking() {
        return getCurrentPlayerObject().isTicking();
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {
        getCurrentPlayerObject().setMetadata(metadataKey, newMetadataValue);
    }

    @Override
    public @NotNull List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        return getCurrentPlayerObject().getMetadata(metadataKey);
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return getCurrentPlayerObject().hasMetadata(metadataKey);
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {
        getCurrentPlayerObject().removeMetadata(metadataKey, owningPlugin);
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
    public boolean isPermissionSet(@NotNull String name) {
        return getCurrentPlayerObject().isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return getCurrentPlayerObject().isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return getCurrentPlayerObject().hasPermission(name);
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return getCurrentPlayerObject().hasPermission(perm);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return getCurrentPlayerObject().addAttachment(plugin, name, value);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return getCurrentPlayerObject().addAttachment(plugin);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value,
            int ticks) {
        return getCurrentPlayerObject().addAttachment(plugin, name, value, ticks);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return getCurrentPlayerObject().addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        getCurrentPlayerObject().removeAttachment(attachment);

    }

    @Override
    public void recalculatePermissions() {
        getCurrentPlayerObject().recalculatePermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return getCurrentPlayerObject().getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {

    }

    @Override
    public @Nullable Component customName() {
        return getCurrentPlayerObject().customName();
    }

    @Override
    public void customName(@Nullable Component customName) {
        getCurrentPlayerObject().customName(customName);
    }

    @Override
    public @Nullable String getCustomName() {
        return getCurrentPlayerObject().getCustomName();
    }

    @Override
    public void setCustomName(@Nullable String name) {
        getCurrentPlayerObject().setCustomName(name);
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return getCurrentPlayerObject().getPersistentDataContainer();
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile) {
        return getCurrentPlayerObject().launchProjectile(projectile);
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> projectile,
            @Nullable Vector velocity) {
        return getCurrentPlayerObject().launchProjectile(projectile, velocity);
    }

    @Override
    public boolean isConversing() {
        return getCurrentPlayerObject().isConversing();
    }

    @Override
    public void acceptConversationInput(@NotNull String input) {
        getCurrentPlayerObject().acceptConversationInput(input);
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        return getCurrentPlayerObject().beginConversation(conversation);
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
        getCurrentPlayerObject().abandonConversation(conversation);

    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent details) {
        getCurrentPlayerObject().abandonConversation(conversation, details);
    }

    @Override
    public void sendRawMessage(@Nullable UUID sender, @NotNull String message) {

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
        return true;
    }

    @Override
    public void setWhitelisted(boolean value) {

    }

    @Override
    public @Nullable Player getPlayer() {
        return getCurrentPlayerObject();
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
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material)
            throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material)
            throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount)
            throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount)
            throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int newValue)
            throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType)
            throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType)
            throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType)
            throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount)
            throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {

    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue) {

    }

    @Override
    public @NotNull Map<String, Object> serialize() {
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
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public @Nullable InetSocketAddress getVirtualHost() {
        return null;
    }

    @Override
    public @NotNull Component displayName() {
        return getCurrentPlayerObject().displayName();
    }

    @Override
    public void displayName(@Nullable Component displayName) {
        getCurrentPlayerObject().displayName(displayName);

    }

    @Override
    public @NotNull String getDisplayName() {
        return getCurrentPlayerObject().getDisplayName();
    }

    @Override
    public void setDisplayName(@Nullable String name) {
        getCurrentPlayerObject().setDisplayName(name);
    }

    @Override
    public void playerListName(@Nullable Component name) {
        getCurrentPlayerObject().playerListName(name);
    }

    @Override
    public @Nullable Component playerListName() {
        return getCurrentPlayerObject().playerListName();
    }

    @Override
    public @Nullable Component playerListHeader() {
        return getCurrentPlayerObject().playerListHeader();
    }

    @Override
    public @Nullable Component playerListFooter() {
        return getCurrentPlayerObject().playerListFooter();
    }

    @Override
    public @NotNull String getPlayerListName() {
        return getCurrentPlayerObject().getPlayerListName();
    }

    @Override
    public void setPlayerListName(@Nullable String name) {
        getCurrentPlayerObject().setPlayerListName(name);

    }

    @Override
    public @Nullable String getPlayerListHeader() {
        return getCurrentPlayerObject().getPlayerListHeader();
    }

    @Override
    public @Nullable String getPlayerListFooter() {
        return getCurrentPlayerObject().getPlayerListFooter();
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
        getCurrentPlayerObject().setCompassTarget(loc);
    }

    @Override
    public @NotNull Location getCompassTarget() {
        return getCurrentPlayerObject().getCompassTarget();
    }

    @Override
    public @Nullable InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public void sendRawMessage(@NotNull String message) {

    }

    @Override
    public void kickPlayer(@Nullable String message) {
        npc.destroy();

    }

    @Override
    public void kick(@Nullable Component message) {
        npc.destroy();

    }

    @Override
    public void kick(@Nullable Component message, @NotNull Cause cause) {
        npc.destroy();

    }

    @Override
    public void chat(@NotNull String msg) {

    }

    @Override
    public boolean performCommand(@NotNull String command) {
        return false;
    }

    @Override
    public boolean isOnGround() {
        return getCurrentPlayerObject().isOnGround();
    }

    @Override
    public boolean isSneaking() {
        return getCurrentPlayerObject().isSneaking();
    }

    @Override
    public void setSneaking(boolean sneak) {
        getCurrentPlayerObject().setSneaking(sneak);
    }

    @Override
    public boolean isSprinting() {
        return getCurrentPlayerObject().isSprinting();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        getCurrentPlayerObject().setSprinting(sprinting);
    }

    @Override
    public void saveData() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        getCurrentPlayerObject().setSleepingIgnored(isSleeping);
    }

    @Override
    public boolean isSleepingIgnored() {
        return getCurrentPlayerObject().isSleepingIgnored();
    }

    @Override
    public @Nullable Location getBedSpawnLocation() {
        return getCurrentPlayerObject().getBedSpawnLocation();
    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location) {
        getCurrentPlayerObject().setBedSpawnLocation(location);
    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location, boolean force) {
        getCurrentPlayerObject().setBedSpawnLocation(location, force);
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
    public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category,
            float volume, float pitch) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category,
            float volume, float pitch) {

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
        getCurrentPlayerObject().sendBlockChange(loc, material, data);
    }

    @Override
    public void sendBlockChange(@NotNull Location loc, @NotNull BlockData block) {
        getCurrentPlayerObject().sendBlockChange(loc, block);
    }

    @Override
    public void sendBlockDamage(@NotNull Location loc, float progress) {
        getCurrentPlayerObject().sendBlockDamage(loc, progress);

    }

    @Override
    public boolean sendChunkChange(@NotNull Location loc, int sx, int sy, int sz, @NotNull byte[] data) {
        return getCurrentPlayerObject().sendChunkChange(loc, sx, sy, sz, data);
    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable List<Component> lines) throws IllegalArgumentException {
    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable List<Component> lines, @NotNull DyeColor dyeColor)
            throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable String[] lines) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable String[] lines, @NotNull DyeColor dyeColor)
            throws IllegalArgumentException {

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
    public void showTitle(@Nullable BaseComponent[] title, @Nullable BaseComponent[] subtitle, int fadeInTicks,
            int stayTicks, int fadeOutTicks) {

    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int fadeInTicks,
            int stayTicks, int fadeOutTicks) {

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
        getCurrentPlayerObject().updateInventory();
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        getCurrentPlayerObject().setPlayerTime(time, relative);
    }

    @Override
    public long getPlayerTime() {
        return getCurrentPlayerObject().getPlayerTime();
    }

    @Override
    public long getPlayerTimeOffset() {
        return getCurrentPlayerObject().getPlayerTimeOffset();
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return getCurrentPlayerObject().isPlayerTimeRelative();
    }

    @Override
    public void resetPlayerTime() {
        getCurrentPlayerObject().resetPlayerTime();
    }

    @Override
    public void setPlayerWeather(@NotNull WeatherType type) {
        getCurrentPlayerObject().setPlayerWeather(type);
    }

    @Override
    public @Nullable WeatherType getPlayerWeather() {
        return getCurrentPlayerObject().getPlayerWeather();
    }

    @Override
    public void resetPlayerWeather() {
        getCurrentPlayerObject().resetPlayerWeather();
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
        return getCurrentPlayerObject().getAllowFlight();
    }

    @Override
    public void setAllowFlight(boolean flight) {
        getCurrentPlayerObject().setAllowFlight(flight);
    }

    @Override
    public void hidePlayer(@NotNull Player player) {
        getCurrentPlayerObject().hidePlayer(player);
    }

    @Override
    public void hidePlayer(@NotNull Plugin plugin, @NotNull Player player) {
        getCurrentPlayerObject().hidePlayer(plugin, player);
    }

    @Override
    public void showPlayer(@NotNull Player player) {
        getCurrentPlayerObject().showPlayer(player);
    }

    @Override
    public void showPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        getCurrentPlayerObject().showPlayer(plugin, player);
    }

    @Override
    public boolean canSee(@NotNull Player player) {
        return getCurrentPlayerObject().canSee(player);
    }

    @Override
    public boolean isFlying() {
        return getCurrentPlayerObject().isFlying();
    }

    @Override
    public void setFlying(boolean value) {
        getCurrentPlayerObject().setFlying(value);
    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {
        getCurrentPlayerObject().setFlySpeed(value);
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {
        getCurrentPlayerObject().setWalkSpeed(value);
    }

    @Override
    public float getFlySpeed() {
        return getCurrentPlayerObject().getFlySpeed();
    }

    @Override
    public float getWalkSpeed() {
        return getCurrentPlayerObject().getWalkSpeed();
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
        return getCurrentPlayerObject().getScoreboard();
    }

    @Override
    public void setScoreboard(@NotNull Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
        getCurrentPlayerObject().setScoreboard(scoreboard);
    }

    @Override
    public boolean isHealthScaled() {
        return getCurrentPlayerObject().isHealthScaled();
    }

    @Override
    public void setHealthScaled(boolean scale) {
        getCurrentPlayerObject().setHealthScaled(scale);
    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {
        getCurrentPlayerObject().setHealthScale(scale);
    }

    @Override
    public double getHealthScale() {
        return getCurrentPlayerObject().getHealthScale();
    }

    @Override
    public @Nullable Entity getSpectatorTarget() {
        return getCurrentPlayerObject().getSpectatorTarget();
    }

    @Override
    public void setSpectatorTarget(@Nullable Entity entity) {
        getCurrentPlayerObject().setSpectatorTarget(entity);
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
        getCurrentPlayerObject().spawnParticle(particle, location, count);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count) {
        getCurrentPlayerObject().spawnParticle(particle, x, y, z, count);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, @Nullable T data) {
        getCurrentPlayerObject().spawnParticle(particle, location, count, data);
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count,
            @Nullable T data) {
        getCurrentPlayerObject().spawnParticle(particle, x, y, z, count, data);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
            double offsetY, double offsetZ) {
        getCurrentPlayerObject().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ) {
        getCurrentPlayerObject().spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ);

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
            double offsetY, double offsetZ, @Nullable T data) {
                getCurrentPlayerObject().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, data);

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, @Nullable T data) {
                getCurrentPlayerObject().spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, data);

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
            double offsetY, double offsetZ, double extra) {
                getCurrentPlayerObject().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);

    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, double extra) {
                getCurrentPlayerObject().spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra);

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX,
            double offsetY, double offsetZ, double extra, @Nullable T data) {
                getCurrentPlayerObject().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data);

    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, double extra, @Nullable T data) {
                getCurrentPlayerObject().spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data);

    }

    @Override
    public @NotNull AdvancementProgress getAdvancementProgress(@NotNull Advancement advancement) {
        return getCurrentPlayerObject().getAdvancementProgress(advancement);
    }

    @Override
    public int getClientViewDistance() {
        return 0;
    }

    @Override
    public @NotNull Locale locale() {
        return getCurrentPlayerObject().locale();
    }

    @Override
    public int getPing() {
        return 0;
    }

    @Override
    public @NotNull String getLocale() {
        return getCurrentPlayerObject().getLocale();
    }

    @Override
    public boolean getAffectsSpawning() {
        return getCurrentPlayerObject().getAffectsSpawning();
    }

    @Override
    public void setAffectsSpawning(boolean affects) {
        getCurrentPlayerObject().setAffectsSpawning(affects);
    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public void setViewDistance(int viewDistance) {
        getCurrentPlayerObject().setViewDistance(viewDistance);
    }

    @Override
    public void updateCommands() {
        
    }

    @Override
    public void openBook(@NotNull ItemStack book) {
        getCurrentPlayerObject().openBook(book);
    }

    @Override
    public void setResourcePack(@NotNull String url, @NotNull String hash) {
        
    }

    @Override
    public @Nullable Status getResourcePackStatus() {
        return Status.SUCCESSFULLY_LOADED;
    }

    @Override
    public @Nullable String getResourcePackHash() {
        return null;
    }

    @Override
    public boolean hasResourcePack() {
        return true;
    }

    @Override
    public @NotNull PlayerProfile getPlayerProfile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPlayerProfile(@NotNull PlayerProfile profile) {
        getCurrentPlayerObject().setPlayerProfile(profile);
    }

    @Override
    public float getCooldownPeriod() {
        return getCurrentPlayerObject().getCooldownPeriod();
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        return getCurrentPlayerObject().getCooledAttackStrength(adjustTicks);
    }

    @Override
    public void resetCooldown() {
        getCurrentPlayerObject().resetCooldown();

    }

    @Override
    public <T> @NotNull T getClientOption(@NotNull ClientOption<T> option) {
        return getCurrentPlayerObject().getClientOption(option);
    }

    @Override
    public @Nullable Firework boostElytra(@NotNull ItemStack firework) {
        return getCurrentPlayerObject().boostElytra(firework);
    }

    @Override
    public void sendOpLevel(byte level) {
        
    }

    @Override
    public @NotNull Set<Player> getTrackedPlayers() {
        return getCurrentPlayerObject().getTrackedPlayers();
    }

    @Override
    public @Nullable String getClientBrandName() {
        return "AI";
    }

    @Override
    public @NotNull Spigot spigot() {
        return getCurrentPlayerObject().spigot();
    }

}
