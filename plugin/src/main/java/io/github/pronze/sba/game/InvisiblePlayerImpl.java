package io.github.pronze.sba.game;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.packet.SClientboundSetEquipmentPacket;
import org.screamingsandals.lib.packet.SClientboundSetPlayerTeamPacket;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.slot.EquipmentSlotMapping;

@Data
public class InvisiblePlayerImpl implements InvisiblePlayer {
    private final Player hiddenPlayer;
    private final GameWrapperImpl arena;
    private boolean justEquipped;
    private boolean isHidden;
    protected BukkitTask armorHider;

    @Override
    public void vanish() {
        Logger.trace("Hiding player: {} for invisibility", hiddenPlayer.getName());
        if (isHidden) {
            return;
        }

        final var wrappedPlayer = PlayerMapper.wrapPlayer(hiddenPlayer);
        final var team = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        if (team == null) {
            return;
        }

        final var invisTeamName = "i-" + team.getName();

        arena.getConnectedPlayers().forEach(connectedPlayers -> {
            final var maybeHolder = GameScoreboardManager.getInstance().getSidebar(arena);
            if (maybeHolder.isEmpty()) {
                return;
            }

            final var sidebar = maybeHolder.get();
            if (sidebar.getTeam(invisTeamName).isEmpty()) {
                sidebar.team(invisTeamName)
                        .nameTagVisibility(SClientboundSetPlayerTeamPacket.TagVisibility.NEVER)
                        .friendlyFire(false)
                        .color(NamedTextColor.NAMES.value(TeamColor.fromApiColor(team.getColor()).chatColor.name().toLowerCase()));
            }

            var sidebarTeam = sidebar.getTeam(team.getName()).orElseThrow();
            if (sidebarTeam.players().contains(wrappedPlayer)) {
                sidebarTeam.removePlayer(wrappedPlayer);
            }

            var invisibleScoreboardTeam = sidebar.getTeam(invisTeamName).orElseThrow();
            if (!invisibleScoreboardTeam.players().contains(wrappedPlayer)) {
                invisibleScoreboardTeam.player(wrappedPlayer);
            }
        });

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

        isHidden = true;
    }

    private boolean isElligble() {
        return arena.getGame().getStatus() == GameStatus.RUNNING
                && hiddenPlayer.getGameMode() == GameMode.SURVIVAL
                && hiddenPlayer.isOnline()
                && hiddenPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && arena.getConnectedPlayers().contains(hiddenPlayer);
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
                .forEach(pl -> {
                    Logger.trace("Sending hide packets to player: {} for hider: {}", pl.getName(), hiddenPlayer.getName());
                    getEquipPacket(airStack, airStack, airStack, airStack).sendPacket(PlayerMapper.wrapPlayer(pl));
                });
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

    @Override
    public void showPlayer() {
        final var wrappedPlayer = PlayerMapper.wrapPlayer(hiddenPlayer);
        final var team = arena.getGame().getTeamOfPlayer(hiddenPlayer);
        if (team == null) {
            return;
        }
        final var invisTeamName = "i-" + team.getName();

        //show nametag
        arena.getConnectedPlayers().forEach(connectedPlayers -> {
            final var maybeHolder = GameScoreboardManager.getInstance().getSidebar(arena);
            if (maybeHolder.isEmpty()) {
                return;
            }

            final var sidebar = maybeHolder.get();

            if (sidebar.getTeam(invisTeamName).isEmpty()) {
                sidebar.team(invisTeamName)
                        .nameTagVisibility(SClientboundSetPlayerTeamPacket.TagVisibility.NEVER)
                        .friendlyFire(false)
                        .color(NamedTextColor.NAMES.value(TeamColor.fromApiColor(team.getColor()).chatColor.name().toLowerCase()));
            }

            var invisibleScoreboardTeam = sidebar.getTeam(invisTeamName).orElseThrow();
            if (invisibleScoreboardTeam.players().contains(wrappedPlayer)) {
                invisibleScoreboardTeam.removePlayer(wrappedPlayer);
            }

            var sidebarTeam = sidebar.getTeam(team.getName()).orElseThrow();
            if (!sidebarTeam.players().contains(wrappedPlayer)) {
                sidebarTeam.player(wrappedPlayer);
            }
        });
        SBAUtil.cancelTask(armorHider);
        showArmor();
        isHidden = false;
        Logger.trace("Un hiding player: {}", hiddenPlayer.getName());
        hiddenPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
