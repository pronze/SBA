package io.github.pronze.sba.game;


import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.bukkit.packet.BukkitPacketMapper;
import org.screamingsandals.lib.material.builder.ItemFactory;
import org.screamingsandals.lib.nms.accessors.ClientboundSetEquipmentPacketAccessor;
import org.screamingsandals.lib.packet.SClientboundSetEquipmentPacket;
import org.screamingsandals.lib.packet.SPacket;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.math.Vector3D;
import java.util.ArrayList;
import java.util.List;

@Data
public class InvisiblePlayer {
    private final Player player;
    private final IArena arena;

    private boolean isHidden;
    private Vector3D lastLocation;
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
        hideArmor();
        armorHider = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isElligble() || !isHidden) {
                    showPlayer();
                    this.cancel();
                    arena.removeHiddenPlayer(player);
                }
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 20L);
    }

    private boolean isElligble() {
        return arena.getGame().getStatus() == GameStatus.RUNNING
                && player.getGameMode() == GameMode.SURVIVAL
                && player.isOnline()
                && player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && arena.getGame().getConnectedPlayers().contains(player);
    }


    private void showArmor() {
        final var boots = player.getInventory().getBoots();
        final var helmet = player.getInventory().getHelmet();
        final var chestplate = player.getInventory().getChestplate();
        final var leggings = player.getInventory().getLeggings();
        arena.getGame()
                .getConnectedPlayers()
                .forEach(pl -> sendPackets(pl, getPackets(boots, chestplate, leggings, helmet)));
    }

    private void hideArmor() {
        var playerTeam = arena.getGame().getTeamOfPlayer(player);

        arena.getGame()
                .getConnectedPlayers()
                .stream()
                .filter(pl -> !playerTeam.getConnectedPlayers().contains(pl))
                .forEach(pl -> sendPackets(pl, getPackets(null, null, null, null)));
    }

    public void sendPackets(Player player, List<SPacket> packets) {
        packets.forEach(packet-> packet.sendPacket(PlayerMapper.wrapPlayer(player)));
    }

    private List<SPacket> getPackets(ItemStack nmsBoot, ItemStack nmsChestPlate, ItemStack nmsLeggings, ItemStack nmsHelmet) {
        final var packets = new ArrayList<SPacket>();
        packets.add(getEquipmentPacket(player, nmsHelmet, SClientboundSetEquipmentPacket.Slot.HEAD));
        packets.add(getEquipmentPacket(player, nmsChestPlate, SClientboundSetEquipmentPacket.Slot.CHEST));
        packets.add(getEquipmentPacket(player, nmsLeggings, SClientboundSetEquipmentPacket.Slot.LEGS));
        packets.add(getEquipmentPacket(player, nmsBoot, SClientboundSetEquipmentPacket.Slot.FEET));
        return packets;
    }

    private SPacket getEquipmentPacket(Player entity, ItemStack stack, SClientboundSetEquipmentPacket.Slot slot) {
        final var equipmentPacket = BukkitPacketMapper.createPacket(SClientboundSetEquipmentPacket.class);
        equipmentPacket.setEntityId(entity.getEntityId());
        equipmentPacket.setItemAndSlot(ItemFactory.build(stack).orElse(null), slot);
        return equipmentPacket;
    }

    public void showPlayer() {
        showArmor();
        isHidden = false;
        Logger.trace("Un hiding player: {}", player.getName());
        SBAUtil.cancelTask(armorHider);
    }
}
