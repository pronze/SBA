package io.github.pronze.sba.game;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import lombok.Data;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.NameTagVisibility;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.slot.EquipmentSlotMapping;

import org.screamingsandals.lib.packet.SClientboundSetEquipmentPacket;
import org.screamingsandals.lib.player.PlayerMapper;

@Data
public class InvisiblePlayer {
    private final Player hiddenPlayer;
    private final Arena arena;

    private boolean isHidden;
    protected BukkitTask armorHider;

    public void vanish() {
        final var team = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        final var invisTeamName = "i-" + team.getName();

        // hide nametag
        arena.getGame().getConnectedPlayers().forEach(connectedPlayers -> {
            final var gameScoreboardManager = (GameScoreboardManager) arena.getScoreboardManager();
            final var maybeHolder = gameScoreboardManager.getScoreboard(connectedPlayers.getUniqueId());
            if (maybeHolder.isEmpty()) {
                return;
            }

            final var holder = maybeHolder.get().getHolder();

            if (!holder.hasTeamEntry(invisTeamName)) {
                holder.addTeam(invisTeamName, TeamColor.fromApiColor(team.getColor()).chatColor);
                holder.getTeamEntry(invisTeamName).ifPresent(invisibleScoreboardTeam -> invisibleScoreboardTeam.setNameTagVisibility(NameTagVisibility.NEVER));
            }
            final var invisibleScoreboardTeam = holder.getTeamOrRegister(invisTeamName);

            holder.getTeamEntry(team.getName()).ifPresent(entry -> {
                if (entry.hasEntry(hiddenPlayer.getName())) {
                    entry.removeEntry(hiddenPlayer.getName());
                }
            });
            if (!invisibleScoreboardTeam.hasEntry(hiddenPlayer.getName())) {
                invisibleScoreboardTeam.addEntry(hiddenPlayer.getName());
            }
        });


        Logger.trace("Hiding player: {} for invisibility", hiddenPlayer.getName());
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
               && hiddenPlayer.getGameMode() == GameMode.SURVIVAL
               && hiddenPlayer.isOnline()
               && hiddenPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)
               && arena.getGame().getConnectedPlayers().contains(hiddenPlayer);
    }

    private void showArmor() {
        final var boots = hiddenPlayer.getInventory().getBoots();
        final var helmet = hiddenPlayer.getInventory().getHelmet();
        final var chestplate = hiddenPlayer.getInventory().getChestplate();
        final var leggings = hiddenPlayer.getInventory().getLeggings();

        arena.getGame()
                .getConnectedPlayers()
                .forEach(pl -> getEquipPacket(
                        convert(helmet),
                        convert(chestplate),
                        convert(leggings),
                        convert(boots)).sendPacket(PlayerMapper.wrapPlayer(pl)));
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
                .forEach(pl -> getEquipPacket(airStack, airStack, airStack, airStack).sendPacket(PlayerMapper.wrapPlayer(pl)));
    }

    private SClientboundSetEquipmentPacket getEquipPacket(Item helmet, Item chestPlate, Item leggings, Item boots) {
        final var packet = new SClientboundSetEquipmentPacket();
        packet.entityId(hiddenPlayer.getEntityId());
        final var slots = packet.slots();
        slots.put(EquipmentSlotMapping.resolve("HEAD").orElseThrow(), helmet);
        slots.put(EquipmentSlotMapping.resolve("CHEST").orElseThrow(), chestPlate);
        slots.put(EquipmentSlotMapping.resolve("LEGS").orElseThrow(), leggings);
        slots.put(EquipmentSlotMapping.resolve("FEET").orElseThrow(), boots);
        return packet;
    }

    public void showPlayer() {
        final var team = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        final var invisTeamName = "i-" + team.getName();

        //show nametag
        arena.getGame().getConnectedPlayers().forEach(connectedPlayers -> {
            final var gameScoreboardManager = (GameScoreboardManager) arena.getScoreboardManager();
            final var maybeHolder = gameScoreboardManager.getScoreboard(connectedPlayers.getUniqueId());
            if (maybeHolder.isEmpty()) {
                return;
            }

            final var holder = maybeHolder.get().getHolder();

            if (!holder.hasTeamEntry(invisTeamName)) {
                holder.addTeam(invisTeamName, TeamColor.fromApiColor(team.getColor()).chatColor);
                holder.getTeamEntry(invisTeamName).ifPresent(invisibleScoreboardTeam -> invisibleScoreboardTeam.setNameTagVisibility(NameTagVisibility.NEVER));
            }
            final var invisibleScoreboardTeam = holder.getTeamOrRegister(invisTeamName);

            if (invisibleScoreboardTeam.hasEntry(hiddenPlayer.getName())) {
                invisibleScoreboardTeam.removeEntry(hiddenPlayer.getName());
            }
            holder.getTeamEntry(team.getName()).ifPresent(entry -> {
                if (!entry.hasEntry(hiddenPlayer.getName())) {
                    entry.addEntry(hiddenPlayer.getName());
                }
            });
        });
        SBAUtil.cancelTask(armorHider);
        showArmor();
        isHidden = false;
        Logger.trace("Un hiding player: {}", hiddenPlayer.getName());
        hiddenPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
