package io.github.pronze.sba.listener;
import io.github.pronze.sba.config.SBAConfig;
import org.bukkit.GameMode;
import org.bukkit.block.data.type.TNT;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public class ExplosionVelocityControlListener implements Listener {

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        final var explodedEntity = event.getEntity();
        if (explodedEntity instanceof TNT || explodedEntity instanceof Fireball) {
            explodedEntity.getWorld().getNearbyEntities(explodedEntity.getLocation(), 5, 5, 5)
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
                            if (player.getGameMode() == GameMode.SPECTATOR) {
                                return;
                            }
                            player.setVelocity(vector);
                            return;
                        }


                        entity.setVelocity(vector);
                    });
        }
    }
}
