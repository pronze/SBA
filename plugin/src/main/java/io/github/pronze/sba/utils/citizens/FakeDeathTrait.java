package io.github.pronze.sba.utils.citizens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.world.LocationMapper;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.Logger;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.kyori.adventure.text.Component;

@Data()
public class FakeDeathTrait extends Trait {

    Player npcEntity;
    private boolean autoTarget = false;

    public FakeDeathTrait() {
        super("FakeDeathTrait");
    }

    // Run code when the NPC is spawned. Note that npc.getEntity() will be null
    // until this method is called.
    // This is called AFTER onAttach and AFTER Load when the server is started.
    @Override
    public void onSpawn() {
        if (npcEntity == null) {
            npcEntity = (Player) npc.getEntity();
            npcEntity.setMetadata("FakeDeath", new FixedMetadataValue(SBA.getPluginInstance(), true));
        }
    }

    @Override
    public void onDespawn() {

    }

    int timer = 0;
    int timerPickup = 5;

    private List<Entity> getNearbyEntities(int range) {
        return npc.getEntity().getNearbyEntities(range, range, range);
    }

    @Override
    public void run() {
        if (timer-- <= 0 && autoTarget) {
            timer = 5;
            if (npc.isSpawned() && !npc.getNavigator().isNavigating()) {
                var entities = getNearbyEntities(10);
                Player target = null;
                Item itemTarget = null;
                for (Entity entity : entities) {
                    if (entity instanceof Player && !entity.equals(npc.getEntity())) {
                        Player possibleTarget = (Player) entity;
                        if (possibleTarget.getGameMode() != GameMode.SURVIVAL)
                            continue;
                        Game targetGame = Main.getInstance().getGameOfPlayer(possibleTarget);
                        if (targetGame == null)
                            continue;
                        var targetTeam = targetGame.getTeamOfPlayer(possibleTarget);
                        if (targetTeam == null)
                            continue;
                        var ourTeam = targetGame.getTeamOfPlayer((Player) npc.getEntity());
                        if (ourTeam == null)
                            continue;
                        if (!ourTeam.getName().equals(targetTeam.getName()))
                            target = possibleTarget;
                        break;
                    }
                    if (entity instanceof Item && itemTarget == null) {
                        itemTarget = (Item) entity;
                    }
                }
                if (target != null)
                    npc.getNavigator().setTarget(target, true);
                else if (itemTarget != null)
                    npc.getNavigator().setTarget(itemTarget, false);

            }
            if (npc.isSpawned() && npc.getNavigator().isNavigating()) {
                // Trying to find a better target or cancel targets that are too far
                boolean isTargetingItem = (npc.getNavigator().getEntityTarget().getTarget() instanceof Item);
                Player currentTarget = null;
                double distance = Double.MAX_VALUE;
                if (!isTargetingItem) {
                    currentTarget = (Player) (npc.getNavigator().getEntityTarget().getTarget());
                }

                distance = npc.getNavigator().getEntityTarget().getTarget().getLocation().distance(npc.getEntity().getLocation());
                if (distance > 12) {
                    npc.getNavigator().cancelNavigation();
                }

                Player target = null;
                var entities = getNearbyEntities(10);
                for (Entity entity : entities) {
                    if (entity instanceof Player && !entity.equals(npc.getEntity())) {
                        Player possibleTarget = (Player) entity;
                        if (possibleTarget.getGameMode() != GameMode.SURVIVAL)
                            continue;
                        Game targetGame = Main.getInstance().getGameOfPlayer(possibleTarget);
                        if (targetGame == null)
                            continue;
                        var targetTeam = targetGame.getTeamOfPlayer(possibleTarget);
                        if (targetTeam == null)
                            continue;
                        var ourTeam = targetGame.getTeamOfPlayer((Player) npc.getEntity());
                        if (ourTeam == null)
                            continue;
                        if (!ourTeam.getName().equals(targetTeam.getName())) {
                            double possibleDistance = possibleTarget.getLocation()
                                    .distance(npc.getEntity().getLocation());
                            if (possibleDistance < distance) {
                                distance = possibleDistance;
                                target = possibleTarget;
                            }
                        }
                    }
                }
                if (target != null)
                    npc.getNavigator().setTarget(target, true);
            }
        }
        if (timerPickup-- <= 0 && autoTarget) {
            timerPickup = 5;
            var entities = getNearbyEntities(3);
            Inventory inv = npc.getOrAddTrait(Inventory.class);
            for (Entity entity : entities) {
                if (entity instanceof Item) {
                    Item itemEntity = (Item) entity;
                    ItemStack is = itemEntity.getItemStack();
                    int space = getAmountOfSpaceFor(is, inv);
                    if (space > 0) {
                        EntityPickupItemEvent pickupEvent = new EntityPickupItemEvent((LivingEntity) npc.getEntity(),
                                itemEntity,
                                Math.max(0, is.getAmount() - space));
                        pickupEvent.callEvent();
                        if (!pickupEvent.isCancelled()) {
                            inv.getInventoryView().addItem(pickupEvent.getItem().getItemStack());
                            if (pickupEvent.getRemaining() > 0) {
                                itemEntity.getItemStack().setAmount(pickupEvent.getRemaining());
                            } else {
                                itemEntity.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getAmountOfSpaceFor(ItemStack m, Inventory inv) {
        m = new ItemStack(m);
        m.setAmount(1);
        var oneStack = new ItemStack(m).getMaxStackSize();
        int space = 0;

        var inventoryContent = inv.getInventoryView().getStorageContents();

        for (int index = 0; index < inventoryContent.length; index++) {
            var itemStack = inventoryContent[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                space += oneStack;
            }
        }

        var inventoryStock = inv.getInventoryView().all(m.getType());
        for (var stack : inventoryStock.entrySet()) {
            var comparedStack = new ItemStack(stack.getValue());
            comparedStack.setAmount(1);
            if (m.equals(comparedStack))
                space += oneStack - stack.getValue().getAmount();

        }
        return space;
    }
}
