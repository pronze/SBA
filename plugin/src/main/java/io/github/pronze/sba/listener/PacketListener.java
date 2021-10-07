package io.github.pronze.sba.listener;

import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.nms.accessors.ClientboundSetEquipmentPacketAccessor;
import org.screamingsandals.lib.nms.accessors.ClientboundUpdateMobEffectPacketAccessor;
import org.screamingsandals.lib.packet.event.SPacketEvent;
import org.screamingsandals.lib.utils.PacketMethod;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.reflect.Reflect;
import io.github.pronze.sba.game.ArenaManager;

@Service
public class PacketListener {

    @OnEvent
    public void onEffects(SPacketEvent event) {
        if (event.getMethod() != PacketMethod.OUTBOUND) {
            return;
        }

        final var player = event.getPlayer().as(Player.class);
        final var packet = event.getPacket();

        if (ClientboundUpdateMobEffectPacketAccessor.getType().isInstance(packet)) {
            final var entityId = (int) Reflect.getField(packet, ClientboundUpdateMobEffectPacketAccessor.getFieldEntityId());
            final var playerGame = Main.getInstance().getGameOfPlayer(player);
            if (playerGame == null) {
                return;
            }

            Player equipper = null;
            for (var gamePlayer : playerGame.getConnectedPlayers()) {
                if (gamePlayer.getEntityId() == entityId) {
                    equipper = gamePlayer;
                    break;
                }
            }

            if (equipper == null) {
                return;
            }

            final var maybeArena = ArenaManager
                    .getInstance()
                    .get(playerGame.getName());

            if (maybeArena.isEmpty()) {
                return;
            }

            final var arena = maybeArena.get();
            if (arena.isPlayerHidden(equipper) && playerGame.getTeamOfPlayer(equipper) != playerGame.getTeamOfPlayer(player)) {
                event.setCancelled(true);
            }
        }
    }

    @OnEvent
    public void onEquipped(SPacketEvent event) {
        if (event.getMethod() != PacketMethod.OUTBOUND) {
            return;
        }

        final var player = event.getPlayer().as(Player.class);
        final var packet = event.getPacket();

        if (ClientboundSetEquipmentPacketAccessor.getType().isInstance(packet)) {
            final var entityId = (int) Reflect.getField(packet, ClientboundSetEquipmentPacketAccessor.getFieldEntity());

            Player equipper = null;
            final var playerGame = Main.getInstance().getGameOfPlayer(player);
            if (playerGame == null) {
                return;
            }

            for (var gamePlayer : playerGame.getConnectedPlayers()) {
                if (gamePlayer.getEntityId() == entityId) {
                    equipper = gamePlayer;
                    break;
                }
            }

            if (equipper == null) {
                return;
            }

            final var maybeArena = ArenaManager
                    .getInstance()
                    .get(playerGame.getName());

            if (maybeArena.isEmpty()) {
                return;
            }

            final var arena = maybeArena.get();
            if (arena.isPlayerHidden(equipper) && playerGame.getTeamOfPlayer(equipper) != playerGame.getTeamOfPlayer(player)) {
                final var hiddenEquipper = arena.getHiddenPlayer(equipper.getUniqueId()).orElseThrow();
                if (hiddenEquipper.isJustEquipped()) {
                    hiddenEquipper.setJustEquipped(false);
                    return;
                }
                event.setCancelled(true);
            }
        }
    }
}
