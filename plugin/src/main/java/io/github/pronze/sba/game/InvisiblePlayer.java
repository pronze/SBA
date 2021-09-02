package io.github.pronze.sba.game;


import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.material.builder.ItemFactory;
import org.screamingsandals.lib.material.slot.EquipmentSlotHolder;
import org.screamingsandals.lib.material.slot.EquipmentSlotMapping;
import org.screamingsandals.lib.packet.AbstractPacket;
import org.screamingsandals.lib.packet.SClientboundSetEquipmentPacket;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;

import java.util.ArrayList;
import java.util.List;

@Data
public class InvisiblePlayer {
    private final Player player;
    private final IArena arena;

    private boolean isHidden;
    protected BukkitTask armorHider;

    public void vanish() {
        Logger.trace("Hiding player: {} for invisibility", player.getName());
        if (isHidden) return;
        isHidden = true;
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
                .forEach(pl -> sendPackets(PlayerMapper.wrapPlayer(player), getPackets(boots, chestplate, leggings, helmet)));
    }

    private void hideArmor() {
        var playerTeam = arena.getGame().getTeamOfPlayer(player);
        final var airStack = new ItemStack(Material.AIR);
        arena.getGame()
                .getConnectedPlayers()
                .stream()
                .filter(pl -> !playerTeam.getConnectedPlayers().contains(pl))
                .forEach(pl -> sendPackets(PlayerMapper.wrapPlayer(pl), getPackets(airStack, airStack, airStack, airStack)));
    }

    public void sendPackets(PlayerWrapper player, List<AbstractPacket> packets) {
        packets.forEach(packet -> packet.sendPacket(player));
    }

    private List<AbstractPacket> getPackets(ItemStack boot, ItemStack chestPlate, ItemStack leggings, ItemStack helmet) {
        final var packets = new ArrayList<AbstractPacket>();
        packets.add(getEquipmentPacket(helmet, EquipmentSlotMapping.resolve("HEAD").orElseThrow()));
        packets.add(getEquipmentPacket(chestPlate, EquipmentSlotMapping.resolve("CHEST").orElseThrow()));
        packets.add(getEquipmentPacket(leggings, EquipmentSlotMapping.resolve("LEGS").orElseThrow()));
        packets.add(getEquipmentPacket(boot, EquipmentSlotMapping.resolve("FEET").orElseThrow()));
        return packets;
    }

    private SClientboundSetEquipmentPacket getEquipmentPacket(ItemStack stack, EquipmentSlotHolder slot) {
        var packet = new SClientboundSetEquipmentPacket();
        packet.entityId(player.getEntityId())
                .slots()
                .put(slot, ItemFactory.build(stack).orElse(null));
        return packet;
    }

    public void showPlayer() {
        SBAUtil.cancelTask(armorHider);
        showArmor();
        isHidden = false;
        Logger.trace("Un hiding player: {}", player.getName());
    }
}
