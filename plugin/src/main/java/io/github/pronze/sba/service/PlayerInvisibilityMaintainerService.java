package io.github.pronze.sba.service;

import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.bukkit.utils.nms.ClassStorage;
import org.screamingsandals.lib.bukkit.utils.nms.network.AutoPacketOutboundListener;
import org.screamingsandals.lib.nms.accessors.ClientboundSetEquipmentPacketAccessor;
import org.screamingsandals.lib.nms.accessors.ServerboundInteractPacketAccessor;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.manager.ArenaManager;

@Service
public class PlayerInvisibilityMaintainerService {

    @OnPostEnable
    public void addListener() {
        new AutoPacketOutboundListener(SBA.getPluginInstance()) {
            @Override
            protected Object handle(Player player, Object packet) {
                if (ClientboundSetEquipmentPacketAccessor.getType().isInstance(packet)) {
                    final var entityId = (int) Reflect.getField(packet, ServerboundInteractPacketAccessor.getFieldEntityId());

                    Player equipper = null;
                    final var playerGame = Main.getInstance().getGameOfPlayer(player);
                    if (playerGame == null) {
                        return packet;
                    }

                    for (var gamePlayer : playerGame.getConnectedPlayers()) {
                        if (gamePlayer.getEntityId() == entityId) {
                            equipper = gamePlayer;
                            break;
                        }
                    }

                    if (equipper == null) {
                        return packet;
                    }

                    final var maybeArena = ArenaManager
                            .getInstance()
                            .get(playerGame.getName());

                    if (maybeArena.isEmpty()) {
                        return packet;
                    }

                    if (maybeArena.get().isPlayerHidden(equipper)  && playerGame.getTeamOfPlayer(equipper) != playerGame.getTeamOfPlayer(player)) {
                        return null;
                    }

                }
                return packet;
            }
        };
    }
}
