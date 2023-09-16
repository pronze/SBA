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
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.item.builder.ItemStackFactory;
import org.screamingsandals.lib.packet.ClientboundSetEquipmentPacket;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.slot.EquipmentSlot;
import org.screamingsandals.lib.tasker.DefaultThreads;
import org.screamingsandals.lib.tasker.Tasker;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

        final var invisTeamName = "i-" + team.getName();

        // hide nametag
        arena.getGame().getConnectedPlayers().forEach(connectedPlayers -> {
            final var gameScoreboardManager = (GameScoreboardManager) arena.getScoreboardManager();
            final var maybeHolder = gameScoreboardManager.getScoreboard(connectedPlayers.getUniqueId());
            if (maybeHolder.isEmpty()) {
                return;
            }

            final var holder = maybeHolder.get().getHolder().of(connectedPlayers);
            if (holder != null) {
                if (!holder.hasTeamEntry(invisTeamName)) {
                    holder.addTeam(invisTeamName, TeamColor.fromApiColor(team.getColor()).chatColor);
                    holder.getTeamEntry(invisTeamName).ifPresent(invisibleScoreboardTeam -> invisibleScoreboardTeam
                            .setNameTagVisibility(NameTagVisibility.NEVER));
                }
                final var invisibleScoreboardTeam = holder.getTeamOrRegister(invisTeamName);
                invisibleScoreboardTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
                holder.getTeamEntry(team.getName()).ifPresent(entry -> {
                    if (entry.hasEntry(hiddenPlayer.getName())) {
                        entry.removeEntry(hiddenPlayer.getName());
                    }
                });
                if (!invisibleScoreboardTeam.hasEntry(hiddenPlayer.getName())) {
                    invisibleScoreboardTeam.addEntry(hiddenPlayer.getName());
                }
            }
        });

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
                && (hiddenPlayer.getGameMode() == GameMode.SURVIVAL || hiddenPlayer.getGameMode() == GameMode.CREATIVE
                        || hiddenPlayer.getGameMode() == GameMode.ADVENTURE)
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
                        convert(boots), convert(currentHand)).sendPacket(Players.wrapPlayer(pl)));
    }

    public org.screamingsandals.lib.item.ItemStack convert(ItemStack itemStack) {
        return Objects.requireNonNullElse(ItemStackFactory.build(itemStack), ItemStackFactory.getAir());
    }

    private void hideArmor() {
        var hiddenPlayerTeam = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        final var airStack = ItemStackFactory.getAir();
        arena.getGame()
                .getConnectedPlayers()
                .stream()
                .filter(pl -> !hiddenPlayerTeam.getConnectedPlayers().contains(pl))
                .forEach(pl -> {
                    getEquipPacket(airStack, airStack, airStack, airStack,
                            convert(hiddenPlayer.getInventory().getItemInMainHand()))
                            .sendPacket(Players.wrapPlayer(pl));
                });
    }

    public void refresh() {
        // TODO Auto-generated method stub
        final var airStack = ItemStackFactory.getAir();
        var hiddenPlayerTeam = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        Tasker.run(DefaultThreads.GLOBAL_THREAD, () -> {
            arena.getGame()
                    .getConnectedPlayers()
                    .stream()
                    .filter(pl -> hiddenPlayerTeam == null || !hiddenPlayerTeam.getConnectedPlayers().contains(pl))
                    .forEach(pl -> {
                        getEquipPacket(airStack, airStack, airStack, airStack,
                                convert(hiddenPlayer.getInventory().getItemInMainHand()))
                                .sendPacket(Players.wrapPlayer(pl));
                    });
        });

    }

    private ClientboundSetEquipmentPacket getEquipPacket(org.screamingsandals.lib.item.ItemStack helmet, org.screamingsandals.lib.item.ItemStack chestPlate,
                                                         org.screamingsandals.lib.item.ItemStack leggings, org.screamingsandals.lib.item.ItemStack boots,
                                                         org.screamingsandals.lib.item.ItemStack hand) {
        final var packet = ClientboundSetEquipmentPacket.builder();
        packet.entityId(hiddenPlayer.getEntityId());
        final var slots = new HashMap<EquipmentSlot, org.screamingsandals.lib.item.ItemStack>();
        if (hand != null)
            slots.put(EquipmentSlot.of("HAND"), hand);
        if (helmet != null)
            slots.put(EquipmentSlot.of("HEAD"), helmet);
        if (chestPlate != null)
            slots.put(EquipmentSlot.of("CHEST"), chestPlate);
        if (leggings != null)
            slots.put(EquipmentSlot.of("LEGS"), leggings);
        if (boots != null)
            slots.put(EquipmentSlot.of("FEET"), boots);
        packet.slots(slots);
        return packet.build();
    }

    @Override
    public void showPlayer() {
        final var team = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        if (team == null) {
            return;
        }
        final var invisTeamName = "i-" + team.getName();
        // show nametag
        arena.getGame().getConnectedPlayers().forEach(connectedPlayers -> {
            final var gameScoreboardManager = (GameScoreboardManager) arena.getScoreboardManager();
            final var maybeHolder = gameScoreboardManager.getScoreboard(connectedPlayers.getUniqueId());
            if (maybeHolder.isEmpty()) {
                return;
            }

            final var holder = maybeHolder.get().getHolder().of(connectedPlayers);
            if (holder != null) {
                if (!holder.hasTeamEntry(invisTeamName)) {
                    holder.addTeam(invisTeamName, TeamColor.fromApiColor(team.getColor()).chatColor);
                    holder.getTeamEntry(invisTeamName).ifPresent(invisibleScoreboardTeam -> invisibleScoreboardTeam
                            .setNameTagVisibility(NameTagVisibility.NEVER));
                }
                final var invisibleScoreboardTeam = holder.getTeamOrRegister(invisTeamName);
                invisibleScoreboardTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);

                if (invisibleScoreboardTeam.hasEntry(hiddenPlayer.getName())) {
                    invisibleScoreboardTeam.removeEntry(hiddenPlayer.getName());
                }
                holder.getTeamEntry(team.getName()).ifPresent(entry -> {
                    if (!entry.hasEntry(hiddenPlayer.getName())) {
                        entry.addEntry(hiddenPlayer.getName());
                    }
                });
            }
        });
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
