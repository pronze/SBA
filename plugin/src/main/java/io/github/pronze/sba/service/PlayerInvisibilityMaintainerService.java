package io.github.pronze.sba.service;

import io.github.pronze.sba.game.Arena;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerPacketEvent;
import org.screamingsandals.lib.nms.accessors.ClientboundSetEquipmentPacketAccessor;
import org.screamingsandals.lib.nms.accessors.ServerboundInteractPacketAccessor;
import org.screamingsandals.lib.utils.PacketMethod;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.reflect.Reflect;
import io.github.pronze.sba.game.ArenaManager;

@Service
public class PlayerInvisibilityMaintainerService {

    //@OnEvent
    public void onPacketRead(SPlayerPacketEvent event) {
        if (event.getMethod() != PacketMethod.OUTBOUND) {
            return;
        }

        final var player = event.getPlayer().as(Player.class);
        final var packet = event.getPacket();

        if (ClientboundSetEquipmentPacketAccessor.getType().isInstance(packet)) {
            final var maybeEntityId = Reflect.getField(packet, ServerboundInteractPacketAccessor.getFieldEntityId());
            if (maybeEntityId == null) {
                return;
            }

            final var entityId = (int) maybeEntityId;

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
                final var hiddenEquipper = ((Arena) arena).getHiddenPlayer(equipper.getUniqueId()).get();
                if (hiddenEquipper.isJustEquipped()) {
                    hiddenEquipper.setJustEquipped(false);
                    return;
                }
                event.setCancelled(true);
            }
        }
    }
}
