package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.BedWarsAPIWrapper;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import net.kyori.adventure.sound.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.SpecialSoundKey;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.item.meta.PotionEffectHolder;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public class MinerTrapTask extends AbstractGameTaskImpl {
    private final int radius;

    public MinerTrapTask() {
        radius = (int) Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
    }

    @Override
    public void run() {
        if (!gameWrapper.getStorage().areMinerTrapEnabled()) {
            return;
        }

        final var storage = gameWrapper.getStorage();
        for (var runningTeam : gameWrapper.getRunningTeams()) {
            if (!storage.areMinerTrapEnabled(runningTeam)) {
                continue;
            }

            for (var gamePlayer : gameWrapper.getConnectedPlayers()) {
                if (BedWarsAPIWrapper.isBedWarsSpectator(gamePlayer)) {
                    continue;
                }

                if (runningTeam.getConnectedPlayers().contains(gamePlayer)) {
                    continue;
                }

                if (runningTeam.getTargetBlockLocation().isInRange(gamePlayer.getLocation(), radius)) {
                    final var triggeredEvent = new SBATeamTrapTriggeredEvent(gamePlayer, runningTeam, gameWrapper);
                    EventManager.fire(triggeredEvent);

                    if (triggeredEvent.isCancelled()) {
                        return;
                    }

                    gameWrapper.getStorage().setPurchasedMinerTrap(runningTeam, false);
                    gamePlayer.addPotionEffect(PotionEffectHolder.of(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 10, 2)));

                    if (gameWrapper.isPlayerHidden(gamePlayer)) {
                        gameWrapper.removeHiddenPlayer(gamePlayer);
                    }

                    Message.of(LangKeys.TEAM_MINER_TRAP_TRIGGERED_MESSAGE)
                            .placeholder("team", gameWrapper.getTeamOfPlayer(gamePlayer).getName())
                            .send(PlayerMapper.wrapPlayer(gamePlayer));

                    var title = Message.of(LangKeys.TEAM_MINER_TRAP_TRIGGERED_TITLE).asComponent();
                    var subTitle = Message.of(LangKeys.TEAM_MINER_TRAP_TRIGGERED_SUBTITLE).asComponent();

                    for (var teamPlayer : runningTeam.getConnectedPlayers()) {
                        teamPlayer.playSound(Sound.sound(
                                SpecialSoundKey.key(SBAConfig.getInstance().node("sounds", "on_trap_triggered").getString()),
                                Sound.Source.AMBIENT,
                                1,
                                1
                        ));
                        teamPlayer.sendTitle(title, subTitle, 20, 60, 0);
                    }
                }
            }
        }

    }
}
