package io.github.pronze.sba.utils.citizens;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;

import gnu.trove.impl.unmodifiable.TUnmodifiableShortByteMap;
import io.github.pronze.sba.specials.SpawnerProtection;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Inventory;

public class BedwarsBlockPlace extends Trait {
    public BedwarsBlockPlace() {
        super("BedwarsBlockPlace");
    }

    int cooldown = 10;
    int timerRefresh = 10;
    int blockBreakerCooldown = 0;
    int blockBreakerTotal = 0;
    Block blockToBreak = null;
    Location startBreak = null;
    double cancelBreakMovement = 0.5;

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

        Inventory inv = npc.getOrAddTrait(Inventory.class);
        ItemStack blockToPlace = getBlock(inv);

        Player aiPlayer = (Player) npc.getEntity();
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
                        inv.getInventoryView().remove(blockToPlace);
                    }
                    cooldown = 0;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBreakableBlock(Block b) {
        if (isBreaking())
            return false;
        Player aiPlayer = (Player) npc.getEntity();
        Game g = Main.getInstance().getGameOfPlayer(aiPlayer);
        return g.isBlockAddedDuringGame(b.getLocation());
    }

    public void breakBlock(Block b) {
        Player aiPlayer = (Player) npc.getEntity();
        var destroySpeed = b.getDestroySpeed(aiPlayer.getItemInHand());

        blockBreakerCooldown = (int) (100 / destroySpeed);
        blockBreakerTotal = (int) (100 / destroySpeed);
        blockToBreak = b;
        startBreak = aiPlayer.getLocation();

        npc.getNavigator().cancelNavigation();
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
                        otherPlayer.sendBlockDamage(blockToBreak.getLocation().toBlockLocation(),
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

    private ItemStack getBlock(Inventory inv) {
        ItemStack is = null;
        for (var item : inv.getContents()) {
            if (item != null) {
                if (item.getType().isBlock())
                    is = item;
            }
        }
        return new ItemStack(Material.OAK_PLANKS);
        //return is;
    }


    public boolean isJumpPlacable(Location currentLocation)
    {
        Block b1 = currentLocation.getBlock();

        return isPlacable(currentLocation) && isEmpty(b1.getRelative(BlockFace.UP))
                && isEmpty(b1.getRelative(BlockFace.UP).getRelative(BlockFace.UP));
    }

    public boolean isPlacable(Location currentLocation) { 
        Player aiPlayer = (Player) npc.getEntity();

        return !SpawnerProtection.getInstance().isProtected(Main.getInstance().getGameOfPlayer(aiPlayer),
                currentLocation)
        && getAgainst(currentLocation.getBlock())!=null && isEmpty(currentLocation.getBlock());
    }

}
