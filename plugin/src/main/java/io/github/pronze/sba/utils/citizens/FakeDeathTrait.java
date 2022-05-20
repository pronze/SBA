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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.bukkit.utils.nms.Version;
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

    // Objective priority
    // 1.Attack nearby player
    // 2.Build bed protection
    // 3.Getting blocks from shops
    // 4.Collect ressource

    private List<AiGoal> goals = new ArrayList<>();

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

            goals.clear();
            goals.add(new AttackNearbyPlayerGoal());
            goals.add(new BuildBedDefenseGoal());
            goals.add(new GatherBlocks());
            goals.add(new GatherRessource());
            goals.add(new CancelNavigation());
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
        if (timer-- <= 0) {
            timer = 5;
            for (AiGoal goal : goals) {
                if (goal.isAvailable()) {
                    goal.doGoal();
                    break;
                }
            }
        }

        if (timerPickup-- <= 0) {
            timerPickup = 5;
            var entities = getNearbyEntities(3);
            Inventory inv = npc.getOrAddTrait(Inventory.class);
            for (Entity entity : entities) {
                if (entity instanceof Item) {
                    Item itemEntity = (Item) entity;
                    ItemStack is = itemEntity.getItemStack();
                    int space = getAmountOfSpaceFor(is, inv);
                    if (space > 0) {
                        if (Version.isVersion(1, 12)) {
                            EntityPickupItemEvent pickupEvent = new EntityPickupItemEvent(
                                    (LivingEntity) npc.getEntity(),
                                    itemEntity,
                                    Math.max(0, is.getAmount() - space));
                            Bukkit.getPluginManager().callEvent(pickupEvent);
                            if (!pickupEvent.isCancelled()) {
                                inv.getInventoryView().addItem(pickupEvent.getItem().getItemStack());
                                if (pickupEvent.getRemaining() > 0) {
                                    itemEntity.getItemStack().setAmount(pickupEvent.getRemaining());
                                } else {
                                    itemEntity.remove();
                                }
                            }
                        } else {
                            PlayerPickupItemEvent pickupEvent = new PlayerPickupItemEvent(
                                    (Player) npc.getEntity(),
                                    itemEntity,
                                    Math.max(0, is.getAmount() - space));
                            Bukkit.getPluginManager().callEvent(pickupEvent);

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

    public interface AiGoal {
        boolean isAvailable();

        void doGoal();
    }

    public class AttackNearbyPlayerGoal implements AiGoal {
        Player target;

        @Override
        public boolean isAvailable() {
            target = null;
            double distance = Double.MAX_VALUE;

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
            return target != null;
        }

        @Override
        public void doGoal() {
            npc.getNavigator().setTarget(target, true);
        }
    }

    public class BuildBedDefenseGoal implements AiGoal {
        @Override
        public boolean isAvailable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void doGoal() {
            // TODO Auto-generated method stub

        }
    }

    public class GatherBlocks implements AiGoal {
        @Override
        public boolean isAvailable() {
            // TODO Auto-generated method stub

            // Try locate a shop
            // Check if enough to buy Wool
            return false;
        }

        @Override
        public void doGoal() {
            // TODO Auto-generated method stub

            // Target the shop
            // If distance is close enough to the shop get the block
        }
    }

    public class GatherRessource implements AiGoal {
        Item target;

        @Override
        public boolean isAvailable() {
            target = null;
            double distance = Double.MAX_VALUE;

            var entities = getNearbyEntities(10);
            for (Entity entity : entities) {
                if (entity instanceof Item) {
                    Item possibleTarget = (Item) entity;

                    double possibleDistance = possibleTarget.getLocation()
                            .distance(npc.getEntity().getLocation());
                    if (possibleDistance < distance) {
                        distance = possibleDistance;
                        target = possibleTarget;
                    }
                }
            }
            return target != null;
        }

        @Override
        public void doGoal() {
            npc.getNavigator().setTarget(target, false);
        }

    }

    public class CancelNavigation implements AiGoal {

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public void doGoal() {
            // Basically do nothing if it can't find another goal
            npc.getNavigator().cancelNavigation();
        }

    }
}
