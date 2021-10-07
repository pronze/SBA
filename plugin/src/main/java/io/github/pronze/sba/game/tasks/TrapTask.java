package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.player.PlayerMapper;

public class TrapTask extends BaseGameTask {
    private final double radius;

    public TrapTask() {
        radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
    }

    @Override
    public void run() {
        if (!arena.getStorage().areBlindTrapEnabled()) {
            return;
        }

        arena.getGame().getRunningTeams()
                .stream()
                .filter(arena.getStorage()::areBlindTrapEnabled)
                .forEach(team -> arena.getGame().getConnectedPlayers()
                        .stream()
                        .filter(player -> !Main.getPlayerGameProfile(player).isSpectator)
                        .filter(player -> !team.getConnectedPlayers().contains(player))
                        .forEach(player -> {

                            if (arena.getStorage().getTargetBlockLocation(team).orElseThrow().distanceSquared(player.getLocation()) <= radius) {
                                final var triggeredEvent = new SBATeamTrapTriggeredEvent(player, team, arena);
                                SBA.getPluginInstance().getServer().getPluginManager().callEvent(triggeredEvent);

                                if (triggeredEvent.isCancelled()) {
                                    return;
                                }

                                arena.getStorage().setPurchasedBlindTrap(team, false);
                                player.addPotionEffect(new PotionEffect
                                        (PotionEffectType.BLINDNESS, 20 * 3, 2));

                                if (arena.isPlayerHidden(player)) {
                                    arena.removeHiddenPlayer(player);
                                }

                                SBALanguageService
                                        .getInstance()
                                        .get(LangKeys.TEAM_BLIND_TRAP_TRIGGERED_MESSAGE).replace("%team%", arena.getGame().getTeamOfPlayer(player).getName())
                                        .send(PlayerMapper.wrapPlayer(player).as(SBAPlayerWrapper.class));

                                var title = SBALanguageService
                                        .getInstance()
                                        .get(LangKeys.TEAM_BLIND_TRAP_TRIGGERED_TITLE)
                                        .toString();

                                var subTitle = SBALanguageService
                                        .getInstance()
                                        .get(LangKeys.TEAM_BLIND_TRAP_TRIGGERED_SUBTITLE)
                                        .toString();

                                team.getConnectedPlayers().forEach(pl -> {
                                    Sounds.playSound(pl, pl.getLocation(), Main.getInstance().getConfig().getString("sounds.on_trap_triggered"),
                                            Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), title, subTitle, 20, 60, 0);
                                });
                            }
                        }));

    }
}
