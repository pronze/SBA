package io.github.pronze.sba.game;

import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import io.github.pronze.sba.wrapper.game.GameWrapper;
import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.potion.PotionEffectType;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.item.meta.PotionEffectHolder;
import org.screamingsandals.lib.packet.SClientboundSetEquipmentPacket;
import org.screamingsandals.lib.packet.SClientboundSetPlayerTeamPacket;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.player.gamemode.GameModeHolder;
import org.screamingsandals.lib.slot.EquipmentSlotMapping;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskState;
import org.screamingsandals.lib.tasker.task.TaskerTask;

import java.util.Objects;

@Data
public class InvisiblePlayerImpl implements InvisiblePlayer {
    private final PlayerWrapper player;
    private final GameWrapper arena;
    private boolean justEquipped;
    private boolean isHidden;
    protected TaskerTask task;

    @Override
    public void vanish() {
        Logger.trace("Hiding player: {} for invisibility", player.getName());
        if (isHidden) {
            return;
        }

        final var team = arena.getTeamOfPlayer(player);
        if (team == null) {
            return;
        }

        final var invisTeamName = "i-" + team.getName();

        hideArmor();

        arena.getConnectedPlayers().forEach(connectedPlayers -> {
            final var sidebar = GameScoreboardManager.getInstance().getSidebar(arena);
            if (sidebar.getTeam(invisTeamName).isEmpty()) {
                sidebar.team(invisTeamName)
                        .nameTagVisibility(SClientboundSetPlayerTeamPacket.TagVisibility.NEVER)
                        .friendlyFire(false)
                        .color(NamedTextColor.NAMES.value(team.getChatColor().name().toLowerCase()));
            }

            var sidebarTeam = sidebar.getTeam(team.getName()).orElseThrow();
            if (sidebarTeam.players().contains(player)) {
                sidebarTeam.removePlayer(player);
            }

            var invisibleScoreboardTeam = sidebar.getTeam(invisTeamName).orElseThrow();
            if (!invisibleScoreboardTeam.players().contains(player)) {
                invisibleScoreboardTeam.player(player);
            }
        });

        if (task != null) {
            if (task.getState() != TaskState.CANCELLED) {
                try {
                    task.cancel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        task = Tasker.build(taskBase -> () -> {
            if (!isEligible() || !isHidden) {
                showPlayer();
                taskBase.cancel();
                arena.removeHiddenPlayer(player);
            }
        }).repeat(1L, TaskerTime.SECONDS).start();
        isHidden = true;
    }

    @Override
    public void showPlayer() {
        final var team = arena.getTeamOfPlayer(player);
        if (team == null) {
            return;
        }
        final var invisTeamName = "i-" + team.getName();

        arena.getConnectedPlayers().forEach(connectedPlayers -> {
            final var sidebar = GameScoreboardManager.getInstance().getSidebar(arena);
            if (sidebar.getTeam(invisTeamName).isEmpty()) {
                sidebar.team(invisTeamName)
                        .nameTagVisibility(SClientboundSetPlayerTeamPacket.TagVisibility.NEVER)
                        .friendlyFire(false)
                        .color(NamedTextColor.NAMES.value(team.getChatColor().name().toLowerCase()));
            }

            var invisibleScoreboardTeam = sidebar.getTeam(invisTeamName).orElseThrow();
            if (invisibleScoreboardTeam.players().contains(player)) {
                invisibleScoreboardTeam.removePlayer(player);
            }

            var sidebarTeam = sidebar.getTeam(team.getName()).orElseThrow();
            if (!sidebarTeam.players().contains(player)) {
                sidebarTeam.player(player);
            }
        });

        if (task.getState() != TaskState.CANCELLED) {
            try {
                task.cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        showArmor();
        Logger.trace("Un hiding player: {}", player.getName());
        player.removePotionEffect(PotionEffectHolder.of(PotionEffectType.INVISIBILITY));
        isHidden = false;
    }

    private boolean isEligible() {
        return player.isOnline()
                && arena.getStatus() == GameStatus.RUNNING
                && player.getGameMode() == GameModeHolder.of("SURVIVAL")
                && player.hasPotionEffect(PotionEffectHolder.of(PotionEffectType.INVISIBILITY))
                && arena.isPlayerConnected(player);
    }

    private void showArmor() {
        final var boots = player.getPlayerInventory().getBoots();
        final var helmet = player.getPlayerInventory().getHelmet();
        final var chestplate = player.getPlayerInventory().getChestplate();
        final var leggings = player.getPlayerInventory().getLeggings();

        for (var gamePlayer : arena.getConnectedPlayers()) {
            getEquipPacket(helmet, chestplate, leggings, boots).sendPacket(gamePlayer);
        }
    }

    protected Item nullSafe(Item item) {
        return Objects.requireNonNullElse(item, ItemFactory.getAir());
    }

    protected void hideArmor() {
        var hiddenPlayerTeam = arena.getTeamOfPlayer(player);
        final var airStack = ItemFactory.getAir();

        for (var gamePlayer : arena.getConnectedPlayers()) {
            if (hiddenPlayerTeam.getConnectedPlayers().contains(gamePlayer)) {
                continue;
            }

            Logger.trace("Hiding player: {} from: {}", player.getName(), gamePlayer.getName());
            getEquipPacket(airStack, airStack, airStack, airStack).sendPacket(gamePlayer);
        }
    }

    protected SClientboundSetEquipmentPacket getEquipPacket(Item helmet, Item chestPlate, Item leggings, Item boots) {
        final var packet = new SClientboundSetEquipmentPacket();
        packet.entityId(player.getEntityId());
        final var slots = packet.slots();
        slots.put(EquipmentSlotMapping.resolve("HEAD").orElseThrow(),  nullSafe(helmet));
        slots.put(EquipmentSlotMapping.resolve("CHEST").orElseThrow(), nullSafe(chestPlate));
        slots.put(EquipmentSlotMapping.resolve("LEGS").orElseThrow(),  nullSafe(leggings));
        slots.put(EquipmentSlotMapping.resolve("FEET").orElseThrow(),  nullSafe(boots));
        return packet;
    }
}
