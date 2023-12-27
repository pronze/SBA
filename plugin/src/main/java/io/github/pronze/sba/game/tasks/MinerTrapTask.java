package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.player.Players;

public class MinerTrapTask extends BaseGameTask {
    private final double radius;

    public MinerTrapTask() {
        radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
    }

    @Override
    public void run() {
        if (!arena.getStorage().areMinerTrapEnabled()) {
            return;
        }

        arena.getGame().getRunningTeams()
                .stream()
                .filter(arena.getStorage()::areMinerTrapEnabled)
                .forEach(team -> arena.getGame().getConnectedPlayers()
                        .stream()
                        .filter(player -> !Main.getPlayerGameProfile(player).isSpectator)
                        .filter(player -> !team.getConnectedPlayers().contains(player))
                        .forEach(player -> {

                            if (arena.getStorage().getTargetBlockLocation(team).orElseThrow()
                                    .distanceSquared(player.getLocation()) <= radius) {
                                final var triggeredEvent = new SBATeamTrapTriggeredEvent(player, team, arena);
                                SBA.getPluginInstance().getServer().getPluginManager().callEvent(triggeredEvent);

                                if (triggeredEvent.isCancelled()) {
                                    return;
                                }

                                arena.getStorage().setPurchasedMinerTrap(team, false);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 10, 2));

                                if (arena.isPlayerHidden(player)) {
                                    arena.removeHiddenPlayer(player);
                                }

                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.TEAM_MINER_TRAP_TRIGGERED_MESSAGE)
                                        .replace("%team%", team.getName())
                                        .send(Players.wrapPlayer(player).as(SBAPlayerWrapper.class));

                                var title = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.TEAM_MINER_TRAP_TRIGGERED_TITLE)
                                        .toComponent();

                                var subTitle = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.TEAM_MINER_TRAP_TRIGGERED_SUBTITLE)
                                        .toComponent();

                                team.getConnectedPlayers().forEach(pl -> {
                                    String sound = SBAConfig.getInstance().getString("sounds.on_trap_triggered",
                                            "ENTITY_ENDER_DRAGON_GROWL");
                                    if (sound == null)
                                        sound = "ENTITY_ENDER_DRAGON_GROWL";
                                    Sounds.playSound(pl, pl.getLocation(),
                                            sound,
                                            Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    SBAUtil.sendTitle(Players.wrapPlayer(pl), title, subTitle, 20, 60, 0);
                                });
                            }
                        }));

    }
}
