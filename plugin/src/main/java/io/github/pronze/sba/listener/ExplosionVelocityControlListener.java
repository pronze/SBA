package io.github.pronze.sba.listener;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import org.bukkit.GameMode;
import org.bukkit.block.data.type.TNT;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

@Service
public class ExplosionVelocityControlListener implements Listener {

    @OnPostEnable
    public void postEnable() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        final var explodedEntity = event.getEntity();
        if (explodedEntity instanceof TNT || explodedEntity instanceof Fireball) {
            final var detectionDistance = SBAConfig.getInstance().node("tnt-fireball-jumping", "detection-distance").getDouble(5.0D);

            explodedEntity.getWorld().getNearbyEntities(explodedEntity.getLocation(), detectionDistance, detectionDistance, detectionDistance)
                    .stream()
                    .filter(entity ->  !entity.equals(explodedEntity))
                    .forEach(entity -> {
                        Vector vector = explodedEntity
                                .getLocation()
                                .clone()
                                .add(0, SBAConfig.getInstance().node("tnt-fireball-jumping", "acceleration-y").getInt(1) ,0)
                                .toVector()
                                .subtract(explodedEntity.getLocation().toVector()).normalize();
                        vector.setY(vector.getY() /  SBAConfig.getInstance().node("tnt-fireball-jumping", "reduce-y").getDouble());
                        vector.multiply(SBAConfig.getInstance().node("tnt-fireball-jumping", "launch-multiplier").getInt());

                        if (entity instanceof Player) {
                            final var player = (Player) entity;
                            if (player.getGameMode() == GameMode.SPECTATOR || !Main.isPlayerInGame(player)) {
                                return;
                            }
                            entity.setVelocity(vector);
                            return;
                        }

                        if (Main.getInstance().isEntityInGame(entity)) {
                            entity.setVelocity(vector);
                        }
                    });
        }
    }
}
