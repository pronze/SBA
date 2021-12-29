package io.github.pronze.sba.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.service.GameManagerImpl;
import io.github.pronze.sba.service.GameTaskProvider;
import io.github.pronze.sba.service.GameTaskProviderImpl;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.nms.accessors.ClientboundSetEquipmentPacketAccessor;
import org.screamingsandals.lib.nms.accessors.ClientboundUpdateMobEffectPacketAccessor;
import org.screamingsandals.lib.packet.event.SPacketEvent;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.PacketMethod;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;

@RequiredArgsConstructor
@Service
public final class InvisiblePlayerListener implements Listener {
    private final GameManagerImpl gameManager;

    @OnPostEnable
    public void onPostEnable(SBA plugin) {
        plugin.registerListener(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        final var item = event.getItem();
        final var player = event.getPlayer();

        if (!Main.isPlayerInGame(player)) {
            return;
        }

        if (item.getType() == Material.POTION) {
            final var potionMeta = (PotionMeta) item.getItemMeta();
            boolean invis = false;
            if (potionMeta.getBasePotionData().getType() == PotionType.INVISIBILITY) {
                invis = true;
            } else {
                if (potionMeta.hasCustomEffects()) {
                    invis = potionMeta
                            .getCustomEffects()
                            .stream()
                            .anyMatch(potionEffect -> potionEffect.getType() == PotionEffectType.INVISIBILITY);
                }
            }

            if (invis) {
                final var playerGame = Main.getInstance().getGameOfPlayer(player);
                gameManager
                        .getWrappedGame(playerGame.getName())
                        .ifPresent(gameWrapper -> gameWrapper.addInvisiblePlayer(PlayerMapper.wrapPlayer(player).as(GamePlayer.class)));
            }
        }
    }

    @OnEvent
    public void onEquipment(SPacketEvent event) {
        final var packet = event.getPacket();

        if (event.getMethod() != PacketMethod.OUTBOUND
                || !ClientboundSetEquipmentPacketAccessor.getType().isInstance(packet)) {
            return;
        }

        final var player = event.getPlayer().as(Player.class);
        final var game = Main.getInstance().getGameOfPlayer(player);
        if (game == null) {
            return;
        }

        final var entityId = (int) Reflect.getField(packet, ClientboundSetEquipmentPacketAccessor.getFieldEntity());
        Player equipper = null;

        for (var gamePlayer : game.getConnectedPlayers()) {
            if (gamePlayer.getEntityId() == entityId) {
                equipper = gamePlayer;
                break;
            }
        }

        if (equipper == null) {
            return;
        }

        final var maybeGameWrapper = gameManager.getWrappedGame(game);
        if (maybeGameWrapper.isEmpty()) {
            return;
        }

        final var gameWrapper = maybeGameWrapper.get();
        if (gameWrapper.isPlayerInvisible(equipper.getUniqueId())
                && game.getTeamOfPlayer(player) != game.getTeamOfPlayer(equipper)) {
            gameWrapper.getInvisiblePlayer(equipper.getUniqueId())
                    .ifPresent(invisiblePlayer -> {
                        if (!invisiblePlayer.isHidden()
                                || invisiblePlayer.isJustHidden()) {
                            return;
                        }

                        event.setCancelled(true);
                    });
        }
    }

    @OnEvent
    public void onPotionEffects(SPacketEvent event) {
        final var packet = event.getPacket();

        if (event.getMethod() != PacketMethod.OUTBOUND
                || !ClientboundUpdateMobEffectPacketAccessor.getType().isInstance(packet)) {
            return;
        }

        final var player = event.getPlayer().as(Player.class);
        final var game = Main.getInstance().getGameOfPlayer(player);
        if (game == null) {
            return;
        }

        final var entityId = (int) Reflect.getField(packet, ClientboundUpdateMobEffectPacketAccessor.getFieldEntityId());
        Player equipper = null;
        for (var gamePlayer : game.getConnectedPlayers()) {
            if (gamePlayer.getEntityId() == entityId) {
                equipper = gamePlayer;
                break;
            }
        }

        if (equipper == null) {
            return;
        }

        final var maybeGameWrapper = gameManager.getWrappedGame(game);
        if (maybeGameWrapper.isEmpty()) {
            return;
        }

        final var gameWrapper = maybeGameWrapper.get();
        if (gameWrapper.isPlayerInvisible(equipper.getUniqueId())
                && game.getTeamOfPlayer(player) != game.getTeamOfPlayer(equipper)) {
            event.setCancelled(true);
        }
    }
}
