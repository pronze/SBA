package pronze.hypixelify.game;


import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.lib.bukkit.utils.nms.ClassStorage;
import org.screamingsandals.bedwars.lib.utils.Pair;
import org.screamingsandals.bedwars.lib.utils.math.Vector3D;
import org.screamingsandals.bedwars.lib.utils.reflect.Reflect;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.utils.SBAUtil;
import pronze.lib.core.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class InvisiblePlayer {
    private final Player player;
    private final Arena arena;

    private boolean isHidden;
    private Vector3D lastLocation;
    protected BukkitTask footStepSoundTracker;
    protected BukkitTask armorHider;

    public void vanish() {
        Logger.trace("Hiding player: {} for invisibility", player.getName());
        if (isHidden) return;
        isHidden = true;
        lastLocation = new Vector3D(
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ()
        );
        footStepSoundTracker = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isElligble() || !isHidden) {
                    arena.removeHiddenPlayer(player);
                    showPlayer();
                    this.cancel();
                    return;
                }
                final var currentLocation = new Vector3D(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ()
                );
                if (currentLocation.equals(lastLocation)) {
                    return;
                }

                Location location = player.getLocation();
                location.setY(Math.floor(location.getY()));

                if (!player.getLocation().clone().subtract(0, 1, 0).getBlock().isEmpty()) {
                    //TODO: play effect
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 10L);

        armorHider = new BukkitRunnable() {
            @Override
            public void run() {
                if (isElligble() && isHidden) {
                    hideArmor();
                } else {
                    showArmor();
                    this.cancel();
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 1L);
    }

    private boolean isElligble() {
        return arena.getGame().getStatus() == GameStatus.RUNNING
                && player.isOnline()
                && player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && arena.getGame().getConnectedPlayers().contains(player);
    }


    private void showArmor() {
        final var boots = player.getInventory().getBoots();
        final var helmet = player.getInventory().getHelmet();
        final var chestplate = player.getInventory().getChestplate();
        final var leggings = player.getInventory().getLeggings();

        final var nmsBoot = stackAsNMS(boots == null ? new ItemStack(Material.AIR) : boots);
        final var nmsChestPlate = stackAsNMS(chestplate == null ? new ItemStack(Material.AIR) : boots);
        final var nmsLeggings = stackAsNMS(leggings == null ? new ItemStack(Material.AIR) : leggings);
        final var nmsHelmet = stackAsNMS(boots == null ? new ItemStack(Material.AIR) : helmet);

        arena
                .getGame()
                .getConnectedPlayers()
                .forEach(pl -> ClassStorage.sendPacket(pl, getPackets(nmsBoot, nmsChestPlate, nmsLeggings, nmsHelmet)));
    }

    private void hideArmor() {
        final var airStack = stackAsNMS(new ItemStack(Material.AIR));
        arena
                .getGame()
                .getConnectedPlayers()
                .stream().filter(pl -> !pl.equals(player))
                .forEach(pl -> ClassStorage.sendPacket(pl, getPackets(airStack, airStack, airStack, airStack)));
    }

    private List<Object> getPackets(Object nmsBoot, Object nmsChestPlate, Object nmsLeggings, Object nmsHelmet) {
        final var packets = new ArrayList<>();

        final var headSlot = Reflect
                .getMethod(ClassStorage.NMS.CraftEquipmentSlot, "getNMS", EquipmentSlot.class)
                .invokeStatic(EquipmentSlot.HEAD);
        final var chestplateSlot = Reflect
                .getMethod(ClassStorage.NMS.CraftEquipmentSlot, "getNMS", EquipmentSlot.class)
                .invokeStatic(EquipmentSlot.CHEST);

        final var legsSlot = Reflect
                .getMethod(ClassStorage.NMS.CraftEquipmentSlot, "getNMS", EquipmentSlot.class)
                .invokeStatic(EquipmentSlot.LEGS);

        final var feetSlot = Reflect
                .getMethod(ClassStorage.NMS.CraftEquipmentSlot, "getNMS", EquipmentSlot.class)
                .invokeStatic(EquipmentSlot.FEET);

        packets.add(getEquipmentPacket(player, nmsHelmet, headSlot));
        packets.add(getEquipmentPacket(player, nmsChestPlate, chestplateSlot));
        packets.add(getEquipmentPacket(player, nmsLeggings, legsSlot));
        packets.add(getEquipmentPacket(player, nmsBoot, feetSlot));
        return packets;
    }

    private Object getEquipmentPacket(Player entity, Object stack, Object chestplateSlot) {
        final var reference = new AtomicReference<>();

        Reflect.constructor(ClassStorage.NMS.PacketPlayOutEntityEquipment, int.class, List.class)
                .ifPresentOrElse(
                        constructor ->
                                reference.set(constructor.construct(entity.getEntityId(), List.of(Pair.of(chestplateSlot, stack)))),
                        () ->
                                reference.set(
                                        Reflect.constructor(ClassStorage.NMS.PacketPlayOutEntityEquipment, int.class, ClassStorage.NMS.EnumItemSlot, ClassStorage.NMS.ItemStack)
                                                .construct(chestplateSlot, chestplateSlot, stack)
                                )
                );
        return reference.get();
    }

    private void showPlayer() {
        isHidden = false;
        SBAUtil.cancelTask(footStepSoundTracker);
    }

    private Object stackAsNMS(ItemStack item) {
        return Reflect.getMethod(ClassStorage.NMS.CraftItemStack, "asNMSCopy", ItemStack.class).invokeStatic(item);
    }
}
