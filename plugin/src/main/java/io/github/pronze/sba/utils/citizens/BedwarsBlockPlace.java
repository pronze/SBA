package io.github.pronze.sba.utils.citizens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.specials.SpawnerProtection;
import io.github.pronze.sba.utils.Logger;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.trait.Trait;

public class BedwarsBlockPlace extends Trait {
    public BedwarsBlockPlace() {
        super("BedwarsBlockPlace");

        useStores = SBAConfig.getInstance().ai().useStores();
        fallback = Material.matchMaterial(SBAConfig.getInstance().ai().infiniteItem());
    }

    boolean useStores;
    Material fallback;
    int cooldown = 10;
    int timerRefresh = 10;
    int blockBreakerCooldown = 0;
    int blockBreakerTotal = 0;
    Block blockToBreak = null;
    Location startBreak = null;
    double cancelBreakMovement = 0.5;
    @Getter
    @Setter
    private boolean isInNeedOfBlock = false;

    public Block getAgainst(Block toPlace) {
        for (Block testBlock : List.of(
                toPlace.getRelative(BlockFace.DOWN),
                toPlace.getRelative(BlockFace.EAST),
                toPlace.getRelative(BlockFace.WEST),
                toPlace.getRelative(BlockFace.NORTH),
                toPlace.getRelative(BlockFace.SOUTH))) {
            if (testBlock.getType().isSolid())
                return testBlock;
        }
        return null;
    }

    public boolean placeBlockIfPossible(Location currentLocation) {
        if (cooldown > 0)
            return false;
        var block = currentLocation.getBlock();
        Player aiPlayer = (Player) npc.getEntity();
        ItemStack blockToPlace = getBlock(aiPlayer.getInventory());

        if (blockToPlace != null) {

            var against = getAgainst(block);
            if (against != null) {
                BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), against, blockToPlace,
                        aiPlayer,
                        true);
                Bukkit.getPluginManager().callEvent(placeEvent);
                if (!placeEvent.isCancelled()) {
                    block.setType(blockToPlace.getType());

                    if (blockToPlace.getAmount() > 1) {
                        blockToPlace.setAmount(blockToPlace.getAmount() - 1);
                    } else {
                        aiPlayer.getInventory().remove(blockToPlace);
                    }
                    cooldown = 0;
                    return true;
                }
            }
        }
        return false;
    }

    public Block viewedBlock(Block b) {
        Player aiPlayer = (Player) getNPC().getEntity();
        if (aiPlayer == null)
            return null;
        World w = aiPlayer.getWorld();

        var rayTraceCheck = w.rayTraceBlocks(aiPlayer.getEyeLocation(),
                aiPlayer.getEyeLocation().subtract(b.getLocation()).getDirection(),
                10);
        if (rayTraceCheck == null)
            return null;

        return rayTraceCheck.getHitBlock();
    }

    public boolean isBreakableBlock(Block b) {
        if (isBreaking())
            return false;
        Player aiPlayer = (Player) npc.getEntity();
        Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
        if (g == null)
            return false;
        if (!g.isBlockAddedDuringGame(b.getLocation()))
            return false;
        return isBlockVisible(b);
    }

    public boolean isBlockVisible(Block b) {
        try {
            Player aiPlayer = (Player) getNPC().getEntity();
            if (aiPlayer == null)
                return false;
            World w = aiPlayer.getWorld();
            var rayTraceCheck = w.rayTraceBlocks(aiPlayer.getEyeLocation(),
                    aiPlayer.getEyeLocation().subtract(b.getLocation()).getDirection(),
                    10);
            if (rayTraceCheck == null)
                return false;
            return rayTraceCheck.getHitBlock().equals(b);
        } catch (Throwable t) {
            // 1.8.8
            return true;
        }
    }

    /*
     * public float getDestroySpeed(ItemStack itemStack, boolean considerEnchants) {
     * net.minecraft.world.item.ItemStack nmsItemStack;
     * if (itemStack instanceof CraftItemStack) {
     * nmsItemStack = ((CraftItemStack) itemStack).handle;
     * if (nmsItemStack == null) {
     * nmsItemStack = net.minecraft.world.item.ItemStack.EMPTY;
     * }
     * } else {
     * nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
     * }
     * float speed =
     * nmsItemStack.getDestroySpeed(this.getNMS().getBlock().defaultBlockState());
     * if (speed > 1.0F && considerEnchants) {
     * int enchantLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.
     * getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.
     * BLOCK_EFFICIENCY, nmsItemStack);
     * if (enchantLevel > 0) {
     * speed += enchantLevel * enchantLevel + 1;
     * }
     * }
     * return speed;
     * }
     */

    public void breakBlock(Block b) {
        if (b != null) {
            Player aiPlayer = (Player) npc.getEntity();

            float destroySpeed = 1;
            if (Reflect.hasMethod(b.getClass(), "getDestroySpeed", ItemStack.class, boolean.class)) {
                var method = Reflect.getMethod(b.getClass(), "getDestroySpeed", ItemStack.class, boolean.class);
                destroySpeed = (float) method.invokeInstance(b, aiPlayer.getItemInHand(), true);
            }

            blockBreakerCooldown = (int) (100 / destroySpeed);
            blockBreakerTotal = (int) (100 / destroySpeed);
            blockToBreak = b;
            startBreak = aiPlayer.getLocation();

            npc.getNavigator().cancelNavigation();
        }
    }

    public boolean isBreaking() {
        return blockToBreak != null;
    }

    @Override
    public void run() {
        if (cooldown-- < 0) {
            cooldown = 0;
        }

        if (blockToBreak != null) {
            Player aiPlayer = (Player) npc.getEntity();
            if (aiPlayer.getLocation().distance(startBreak) > cancelBreakMovement) {
                blockToBreak = null;
                blockBreakerCooldown = 0;
            } else if (blockBreakerCooldown-- <= 0) {
                BlockBreakEvent bbe = new BlockBreakEvent(blockToBreak, aiPlayer);
                Bukkit.getPluginManager().callEvent(bbe);
                if (!bbe.isCancelled()) {
                    blockToBreak.breakNaturally(aiPlayer.getItemInHand());
                }

                blockToBreak = null;
            } else {
                if (timerRefresh++ > 10) {
                    timerRefresh = 0;

                    Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
                    g.getConnectedPlayers().forEach(otherPlayer -> {
                        otherPlayer.sendBlockDamage(blockLocation(blockToBreak.getLocation()),
                                1 - ((float) blockBreakerCooldown / (float) blockBreakerTotal));
                    });
                }
            }
        }
    }

    public boolean isEmpty(Block testBlock) {
        return testBlock.getType() == Material.AIR || testBlock.getType() == Material.LAVA
                || testBlock.getType() == Material.WATER;
    }

    public ItemStack getBlock(Inventory inv) {

        ItemStack is = null;
        if (useStores) {
            for (var item : inv.getContents()) {
                if (item != null) {
                    if (item.getType().isBlock())
                        is = item;
                }
            }
            isInNeedOfBlock = is == null;
            if (is == null) {
                Logger.trace("NPC {} needs blocks", getNPC().getName());
            }
        } else {
            return new ItemStack(Material.getMaterial(SBAConfig.getInstance().ai().infiniteItem()));
        }
        return is;
    }

    public boolean isJumpPlacable(Location currentLocation) {
        Block b1 = currentLocation.getBlock();

        return isPlacable(currentLocation) && isEmpty(b1.getRelative(BlockFace.UP))
                && isEmpty(b1.getRelative(BlockFace.UP).getRelative(BlockFace.UP));
    }

    public boolean isPlacable(Location currentLocation) {
        Player aiPlayer = (Player) npc.getEntity();

        return !SpawnerProtection.getInstance().isProtected(Main.getInstance().getGameOfPlayer(aiPlayer),
                currentLocation)
                && getAgainst(currentLocation.getBlock()) != null && isEmpty(currentLocation.getBlock());
    }

    public boolean teleport(Player aiPlayer, Location l) {
        try {
            if (isEmpty(l.getBlock()) && isEmpty(l.getBlock().getRelative(BlockFace.UP))) {
                aiPlayer.teleport(l);
                return true;
            }
        } catch (Throwable ignored) {

        }
        return false;
    }

    public Location blockLocation(Location l) {
        return new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

}
