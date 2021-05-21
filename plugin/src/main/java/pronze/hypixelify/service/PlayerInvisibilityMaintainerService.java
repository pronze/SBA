package pronze.hypixelify.service;

import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.lib.bukkit.utils.nms.ClassStorage;
import org.screamingsandals.bedwars.lib.bukkit.utils.nms.network.AutoPacketInboundListener;
import org.screamingsandals.bedwars.lib.bukkit.utils.nms.network.AutoPacketOutboundListener;
import org.screamingsandals.bedwars.lib.utils.reflect.Reflect;
import org.screamingsandals.bedwars.player.PlayerManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.game.ArenaManager;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnInit;

import java.util.concurrent.atomic.AtomicReference;

@AutoInitialize
public class PlayerInvisibilityMaintainerService {

    @OnInit
    public void addListener() {
        final Class<?> PacketPlayOutEntityEquipment = ClassStorage.safeGetClass("{nms}.PacketPlayOutEntityEquipment");

        new AutoPacketOutboundListener(SBAHypixelify.getInstance()) {
            @Override
            protected Object handle(Player player, Object packet) {
                if (PacketPlayOutEntityEquipment.isInstance(packet)) {
                    final var entityId = (int) Reflect.getField(ClassStorage.NMS.PacketPlayInUseEntity, "a,field_149567_a", packet);

                    final AtomicReference<Player> armorEquipper = new AtomicReference<>();
                    final Game playerGame = PlayerManager.getInstance().getGameOfPlayer(player.getUniqueId()).orElse(null);

                    if (playerGame == null) {
                        return packet;
                    }

                    PlayerManager
                            .getInstance()
                            .getGameOfPlayer(player.getUniqueId())
                            .ifPresent(game -> game.getConnectedPlayers().forEach(pl -> {
                                if (pl.getEntityId() == entityId) {
                                    armorEquipper.set(pl);
                                }
                            }));


                    final var equipper = armorEquipper.get();

                    if (equipper == null) {
                        return packet;
                    }

                    final var maybeArena = ArenaManager.getInstance().get(playerGame.getName());

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
