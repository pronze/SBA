package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.PlayerWrapper;
import lombok.RequiredArgsConstructor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.player.PlayerMapper;

@RequiredArgsConstructor
public class TrapTask implements Runnable {

    @NotNull
    private final Arena arena;
    private final double radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);

    @Override
    public void run() {
        if (!arena.getStorage().areTrapsEnabled()) {
            return;
        }

        arena.getGame().getRunningTeams()
                .stream()
                .filter(arena.getStorage()::areTrapsEnabled)
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

                                arena.getStorage().setPurchasedTrap(team, false);
                                player.addPotionEffect(new PotionEffect
                                        (PotionEffectType.BLINDNESS, 20 * 3, 2));

                                if (arena.isPlayerHidden(player)) {
                                    arena.removeHiddenPlayer(player);
                                }

                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.TEAM_TRAP_TRIGGERED_MESSAGE).replace("%team%", arena.getGame().getTeamOfPlayer(player).getName())
                                        .send(PlayerMapper.wrapPlayer(player).as(PlayerWrapper.class));

                                var title = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.TEAM_TRAP_TRIGGERED_TITLE)
                                        .toString();

                                var subTitle = LanguageService
                                        .getInstance()
                                        .get(MessageKeys.TEAM_TRAP_TRIGGERED_SUBTITLE)
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
