package io.github.pronze.sba.utils.citizens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.screamingsandals.bedwars.api.game.Game;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.service.AIService;
import io.github.pronze.sba.utils.Logger;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.trait.Trait;
import org.screamingsandals.lib.impl.bukkit.utils.Version;

public class FakeDeathTrait extends Trait {

    // Objective priority
    // 1.Attack nearby player
    // 2.Build bed protection
    // 3.Getting blocks from shops
    // 4.Collect ressource

    class Pickup18 {
        public void pick(Item itemEntity, ItemStack is, int space) {
            PlayerPickupItemEvent pickupEvent = new PlayerPickupItemEvent(
                    (Player) npc.getEntity(),
                    itemEntity,
                    Math.max(0, is.getAmount() - space));

            Bukkit.getPluginManager().callEvent(pickupEvent);

            if (!pickupEvent.isCancelled()) {
                Logger.trace("NPC Pickup {}", itemEntity.getItemStack());
                getNpcEntity().getInventory().addItem(pickupEvent.getItem().getItemStack());
                blockPlace().getBlock(getNpcEntity().getInventory());
                if (pickupEvent.getRemaining() > 0) {
                    itemEntity.getItemStack().setAmount(pickupEvent.getRemaining());
                } else {
                    itemEntity.remove();
                }
            }
        }
    }

    class Pickup112 {
        public void pick(Item itemEntity, ItemStack is, int space) {
            EntityPickupItemEvent pickupEvent = new EntityPickupItemEvent(
                    (LivingEntity) npc.getEntity(),
                    itemEntity,
                    Math.max(0, is.getAmount() - space));
            Bukkit.getPluginManager().callEvent(pickupEvent);
            if (!pickupEvent.isCancelled()) {
                Logger.trace("NPC Pickup {}", itemEntity.getItemStack());
                getNpcEntity().getInventory().addItem(pickupEvent.getItem().getItemStack());
                blockPlace().getBlock(getNpcEntity().getInventory());
                if (pickupEvent.getRemaining() > 0) {
                    itemEntity.getItemStack().setAmount(pickupEvent.getRemaining());
                } else {
                    itemEntity.remove();
                }
            }
        }
    }

    private List<AiGoal> goals = new ArrayList<>();
    @Getter
    @Setter
    private Strategy strategy = Strategy.ANY;

    private Player oldPlayerObject = null;

    public FakeDeathTrait() {
        super("FakeDeathTrait");
    }

    private BedwarsBlockPlace blockPlace_;

    BedwarsBlockPlace blockPlace() {
        if (blockPlace_ != null)
            return blockPlace_;

        npc.getTraits().forEach(t -> {
            if (t instanceof BedwarsBlockPlace)
                blockPlace_ = (BedwarsBlockPlace) t;
        });
        return blockPlace_;
    }

    public Player getNpcEntity() {
        return (Player) npc.getEntity();
    }

    // Run code when the NPC is spawned. Note that npc.getEntity() will be null
    // until this method is called.
    // This is called AFTER onAttach and AFTER Load when the server is started.
    @Override
    public void onSpawn() {
        if (!(npc.getEntity() instanceof Player))
            return;
        try {
            Player npcEntity = (Player) npc.getEntity();
            if (oldPlayerObject != null && npcEntity != oldPlayerObject) {
                if (game != null) {
                    game.getRunningTeams().forEach(
                            team -> {
                                if (team.isPlayerInTeam(oldPlayerObject)) {
                                    game.leaveFromGame(oldPlayerObject);
                                }
                            });
                }
            }
            oldPlayerObject = npcEntity;
            npcEntity.setMetadata("FakeDeath", new FixedMetadataValue(SBA.getPluginInstance(), true));

            Logger.trace("Initializing AI in mode{}", strategy);
            Random r = new Random();
            if (strategy == Strategy.ANY) {
                var possibilities = List.of(Strategy.AGRESSIVE, Strategy.DEFENSIVE, Strategy.BALANCED);
                strategy = possibilities.get(r.nextInt(possibilities.size()));
            }

            goals.clear();
            if (strategy != Strategy.NONE) {
                goals.add(new DontCancelBlockBreak(this));
                goals.add(new GatherBlocks(this));
                goals.add(new AttackNearbyPlayerGoal(this));
                if (strategy == Strategy.DEFENSIVE)
                    goals.add(new BuildBedDefenseGoal(this));
                else if (strategy == Strategy.AGRESSIVE)
                    goals.add(new AttackOtherGoal(this));
                else if (strategy == Strategy.BALANCED)
                    goals.add(new BalancedGoal(this));
                goals.add(new GatherRessource(this));
                goals.add(new CancelNavigation(this));
            }
        } catch (ClassCastException cc) {
            npc.removeTrait(FakeDeathTrait.class);
        }
    }

    @Override
    public void onDespawn() {

    }

    public Player getPlayerObject() {
        return oldPlayerObject;
    }

    int timer = 0;
    int timerPickup = 5;
    private Game game;

    List<Entity> getNearbyEntities(int range) {
        return npc.getEntity().getNearbyEntities(range, range, range);
    }

    @Override
    public void run() {
        if (game != null) {
            if (!game.isLocationInArena(getPlayerObject().getLocation())
                    && getPlayerObject().getGameMode() == GameMode.SURVIVAL) {
                AIService.getInstance().die(getPlayerObject());
            }
        } else
            return;

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
            for (Entity entity : entities) {
                if (entity instanceof Item) {
                    Item itemEntity = (Item) entity;
                    ItemStack is = itemEntity.getItemStack();
                    int space = getAmountOfSpaceFor(is, getNpcEntity().getInventory());
                    if (space > 0) {
                        if (Version.isVersion(1, 12)) {
                            new Pickup112().pick(itemEntity, is, space);
                        } else {
                            new Pickup18().pick(itemEntity, is, space);
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
        ItemStack[] inventoryContent = null;
        try {
            inventoryContent = inv.getStorageContents();
        } catch (Throwable t) {
            inventoryContent = inv.getContents();
        }

        for (int index = 0; index < inventoryContent.length; index++) {
            var itemStack = inventoryContent[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                space += oneStack;
            }
        }

        var inventoryStock = inv.all(m.getType());
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

    public void joinBedwarsGame(Game game) {
        this.game = game;
    }
}
