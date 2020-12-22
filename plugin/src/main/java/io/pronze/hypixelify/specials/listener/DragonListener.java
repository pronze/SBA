package io.pronze.hypixelify.specials.listener;
import io.pronze.hypixelify.specials.Dragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.config.ConfigurationContainer;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.special.SpecialItem;
import org.screamingsandals.bedwars.utils.MiscUtils;

import java.util.List;

public class DragonListener implements Listener {

    @EventHandler
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }

        final var eventDragon = (EnderDragon) event.getEntity();
        Main.getGameNames().forEach(name-> {
            final var game = Main.getGame(name);
            if (game.getStatus() == GameStatus.RUNNING && eventDragon.getWorld().equals(game.getGameWorld())) {
                List<SpecialItem> dragons = game.getActivedSpecialItems(Dragon.class);
                for (var item : dragons) {
                    if (item instanceof Dragon) {
                        final var dragon = (Dragon) item;
                        if (dragon.getEntity().equals(eventDragon)) {
                            if (event.getDamager() instanceof Player) {
                                Player player = (Player) event.getDamager();
                                if (Main.isPlayerInGame(player)) {
                                    if (dragon.getTeam() != game.getTeamOfPlayer(player)) {
                                        return;
                                    }
                                }
                            } else if (event.getDamager() instanceof Projectile) {
                                ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                                if (shooter instanceof Player) {
                                    Player player = (Player) shooter;
                                    if (Main.isPlayerInGame(player)) {
                                        if (dragon.getTeam() != game.getTeamOfPlayer(player)) {
                                            return;
                                        }
                                    }
                                }
                            }

                            event.setCancelled(game.getConfigurationContainer().getOrDefault(ConfigurationContainer.FRIENDLY_FIRE, Boolean.class, false));
                            return;
                        }
                        return;
                    }
                }
            }
        });
    }



    @EventHandler
    public void onEnderDragonTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }

        final var enderDragon = (EnderDragon) event.getEntity();


        Main.getGameNames().forEach(gameName-> {
            final var game = Main.getGame(gameName);
            if ((game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) && enderDragon.getWorld().equals(game.getWorld())) {
                List<SpecialItem> dragons = game.getActivedSpecialItems(Dragon.class);
                dragons.forEach(item-> {
                    if (item instanceof Dragon) {
                        final var dragon = (Dragon) item;
                        if (dragon.getEntity().equals(enderDragon)) {
                            if (event.getTarget() instanceof Player) {
                                final var player = (Player) event.getTarget();
                                if (game.isProtectionActive(player)) {
                                    event.setCancelled(true);
                                    return;
                                }

                                if (Main.isPlayerInGame(player)) {
                                    if (dragon.getTeam() == game.getTeamOfPlayer(player)) {
                                        event.setCancelled(true);

                                        final var enemyTarget = MiscUtils.findTarget(game, player, 40);
                                        if (enemyTarget != null) {
                                            enderDragon.setTarget(enemyTarget);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

    }

    @EventHandler
    public void onDragonTargetDeath(PlayerDeathEvent event) {
        if (Main.isPlayerInGame(event.getEntity())) {
            final var game = Main.getPlayerGameProfile(event.getEntity()).getGame();

            final var dragons = game.getActivedSpecialItems(Dragon.class);
            for (var item : dragons) {
                final var dragon = (Dragon) item;
                final var enderDragon = (EnderDragon) dragon.getEntity();
                if (enderDragon.getTarget() != null && enderDragon.getTarget().equals(event.getEntity())) {
                    enderDragon.setTarget(null);
                }
            }
        }
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }

        EnderDragon eventDragon = (EnderDragon) event.getEntity();
        Main.getGameNames().forEach(name-> {
            final var game = Main.getGame(name);
            if ((game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) && eventDragon.getWorld().equals(game.getGameWorld())) {
                List<SpecialItem> dragons = game.getActivedSpecialItems(Dragon.class);
                for (SpecialItem item : dragons) {
                    if (item instanceof EnderDragon) {
                        Dragon golem = (Dragon) item;
                        if (golem.getEntity().equals(eventDragon)) {
                            event.getDrops().clear();
                        }
                    }
                }
            }
        });
    }

}
