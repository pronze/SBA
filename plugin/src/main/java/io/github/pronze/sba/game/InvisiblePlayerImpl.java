package io.github.pronze.sba.game;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.slot.EquipmentSlotHolder;
import org.screamingsandals.lib.slot.EquipmentSlotMapping;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.packet.SClientboundSetEquipmentPacket;
import org.screamingsandals.lib.packet.SClientboundSetPlayerTeamPacket.TagVisibility;
import org.screamingsandals.lib.player.PlayerMapper;

@Data
public class InvisiblePlayerImpl implements InvisiblePlayer {
    private final Player hiddenPlayer;
    private final Arena arena;
    private boolean justEquipped = false;
    private boolean isHidden;
    protected BukkitTask armorHider;

    @Override
    public void vanish() {
        Logger.trace("InvisiblePlayerImpl.vanish{}", hiddenPlayer);
        final var team = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        if (team == null) {
            return;
        }
        
        if (isHidden) {
            return;
        }
        isHidden = true;
        hideArmor();
        armorHider = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isElligble() || !isHidden) {
                    showPlayer();
                    this.cancel();
                    arena.removeHiddenPlayer(hiddenPlayer);
                }
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 20L);
    }

    private boolean isElligble() {
        return arena.getGame().getStatus() == GameStatus.RUNNING
                && (hiddenPlayer.getGameMode() == GameMode.SURVIVAL || hiddenPlayer.getGameMode() == GameMode.CREATIVE|| hiddenPlayer.getGameMode() == GameMode.ADVENTURE)
                && hiddenPlayer.isOnline()
                && hiddenPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && arena.getGame().getConnectedPlayers().contains(hiddenPlayer);
    }

    private void showArmor() {
        final var boots = hiddenPlayer.getInventory().getBoots();
        final var helmet = hiddenPlayer.getInventory().getHelmet();
        final var chestplate = hiddenPlayer.getInventory().getChestplate();
        final var leggings = hiddenPlayer.getInventory().getLeggings();
        final var currentHand = hiddenPlayer.getInventory().getItemInMainHand();
        arena.getGame()
                .getConnectedPlayers()
                .forEach(pl -> getEquipPacket(
                        convert(helmet),
                        convert(chestplate),
                        convert(leggings),
                        convert(boots), convert(currentHand)).sendPacket(PlayerMapper.wrapPlayer(pl)));
    }

    public Item convert(ItemStack itemStack) {
        return ItemFactory.build(itemStack).orElse(ItemFactory.getAir());
    }

    private void hideArmor() {
        var hiddenPlayerTeam = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        final var airStack = ItemFactory.getAir();
        arena.getGame()
                .getConnectedPlayers()
                .stream()
                .filter(pl -> !hiddenPlayerTeam.getConnectedPlayers().contains(pl))
                .forEach(pl -> {
                    getEquipPacket(airStack, airStack, airStack, airStack,
                            convert(hiddenPlayer.getInventory().getItemInMainHand()))
                                    .sendPacket(PlayerMapper.wrapPlayer(pl));
                });
    }

    public void refresh() {
        // TODO Auto-generated method stub
        final var airStack = ItemFactory.getAir();
        var hiddenPlayerTeam = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        Tasker.build(()->{
        arena.getGame()
                .getConnectedPlayers()
                .stream()
                .filter(pl -> hiddenPlayerTeam==null || !hiddenPlayerTeam.getConnectedPlayers().contains(pl))
                .forEach(pl -> {
                    getEquipPacket(airStack, airStack, airStack, airStack,
                            convert(hiddenPlayer.getInventory().getItemInMainHand()))
                                    .sendPacket(PlayerMapper.wrapPlayer(pl));
                });
        }).afterOneTick().start();

    }

    private SClientboundSetEquipmentPacket getEquipPacket(Item helmet, Item chestPlate, Item leggings, Item boots,
            Item hand) {
        final var packet = new SClientboundSetEquipmentPacket();
        packet.entityId(hiddenPlayer.getEntityId());
        final var slots = packet.slots();
        if (hand != null)
            slots.put(EquipmentSlotMapping.resolve("HAND").orElseThrow(), hand);
        if (helmet != null)
            slots.put(EquipmentSlotMapping.resolve("HEAD").orElseThrow(), helmet);
        if (chestPlate != null)
            slots.put(EquipmentSlotMapping.resolve("CHEST").orElseThrow(), chestPlate);
        if (leggings != null)
            slots.put(EquipmentSlotMapping.resolve("LEGS").orElseThrow(), leggings);
        if (boots != null)
            slots.put(EquipmentSlotMapping.resolve("FEET").orElseThrow(), boots);
        return packet;
    }

    @Override
    public void showPlayer() {
        final var team = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        if (team == null) {
            return;
        }
       
        SBAUtil.cancelTask(armorHider);
        showArmor();
        isHidden = false;
        hiddenPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public Player getHiddenPlayer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setHidden(boolean hidden) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isJustEquipped() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setJustEquipped(boolean justEquipped) {
        // TODO Auto-generated method stub

    }

}
