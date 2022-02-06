package io.github.pronze.sba.listener;

import io.github.pronze.sba.config.SBAConfig;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.event.EventPriority;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.entity.SEntityDamageByEntityEvent;
import org.screamingsandals.lib.event.entity.SEntityDamageEvent;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.annotations.Service;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ExplosionJumpListener {
    private final Set<PlayerWrapper> explosionAffectedPlayers = new HashSet<>();
    private final SBAConfig config;

    @OnEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(SEntityDamageEvent event) {
        if (!event.getDamageCause().is("fall")) {
            return;
        }

        final var entity = event.getEntity();
        if (!entity.getEntityType().is("player")) {
            return;
        }

        final var playerWrapper = entity.as(PlayerWrapper.class);
        if (explosionAffectedPlayers.contains(playerWrapper)) {
            event.setDamage(config.node("explosion-jumping", "fall-damage").getDouble(3.0D));
            explosionAffectedPlayers.remove(playerWrapper);
        }
    }

    @OnEvent(priority = EventPriority.HIGHEST)
    public void onExplode(SEntityDamageByEntityEvent event) {
        final var explodedEntity = event.getDamager();

        // TODO: entity type explosive exists in slib?
        if (!explodedEntity.getEntityType().is("explosive")) {
            return;
        }

        final var detectionDistance = config.node("explosion-jumping", "detection-distance").getDouble(5.0D);
        final var bukkitWorld = explodedEntity.getLocation().getWorld().as(World.class);
        final var vector = explodedEntity.getLocation().as(Location.class).toVector();
        final var newVector = explodedEntity
                .getLocation()
                .as(Location.class)
                .clone()
                .add(0, config.node("explosion-jumping", "acceleration-y").getDouble(0.8), 0)
                .toVector()
                .subtract(vector)
                .normalize();

        newVector.setY(newVector.getY() / config.node("explosion-jumping", "y-throttle").getDouble(2.0));
        newVector.multiply(config.node("explosion-jumping", "launch-multiplier").getDouble(3.8));

        // TODO: add World#getNearbyEntities() methods and overloads to SLib.
        bukkitWorld.getNearbyEntities(explodedEntity.getLocation().as(Location.class), detectionDistance, detectionDistance, detectionDistance)
                .stream()
                .filter(entity -> !entity.equals(explodedEntity))
                .forEach(entity -> {
                    if (entity instanceof Player) {
                        final var player = (Player) entity;
                        if (player.getGameMode() == GameMode.SPECTATOR
                                || !Main.isPlayerInGame(player)) {
                            return;
                        }

                        vector.add(new Vector(
                                player.getEyeLocation().getDirection().getX(),
                                0,
                                player.getEyeLocation().getDirection().getZ()
                        ));

                        player.setVelocity(vector);
                        explosionAffectedPlayers.add(PlayerMapper.wrapPlayer(player));
                        return;
                    }

                    if (Main.getInstance().isEntityInGame(entity)) {
                        entity.setVelocity(vector);
                    }
                });
    }
}

