package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public class TrapTask extends AbstractGameTaskImpl {
    private final double radius;

    public TrapTask() {
        radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
    }

    @Override
    public void run() {
        if (!arena.getStorage().areBlindTrapEnabled()) {
            return;
        }

        final var storage = arena.getStorage();
        for (var runningTeam : arena.getGame().getRunningTeams()) {
            if (!storage.areBlindTrapEnabled(runningTeam)) {
                continue;
            }

            for (var gamePlayer : arena.getConnectedPlayers()) {
                if (Main.getPlayerGameProfile(gamePlayer).isSpectator) {
                    continue;
                }

                if (runningTeam.getConnectedPlayers().contains(gamePlayer)) {
                    continue;
                }

                if (runningTeam.getTargetBlock().distanceSquared(gamePlayer.getLocation()) <= radius) {
                    final var triggeredEvent = new SBATeamTrapTriggeredEvent(gamePlayer, runningTeam, arena);
                    SBA.getPluginInstance().getServer().getPluginManager().callEvent(triggeredEvent);

                    if (triggeredEvent.isCancelled()) {
                        return;
                    }

                    arena.getStorage().setPurchasedBlindTrap(runningTeam, false);
                    gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 2));

                    if (arena.isPlayerHidden(gamePlayer)) {
                        arena.removeHiddenPlayer(gamePlayer);
                    }

                    Message.of(LangKeys.TEAM_BLIND_TRAP_TRIGGERED_MESSAGE)
                            .placeholder("team", arena.getGame().getTeamOfPlayer(gamePlayer).getName())
                            .send(PlayerMapper.wrapPlayer(gamePlayer));

                    var title = Message.of(LangKeys.TEAM_BLIND_TRAP_TRIGGERED_TITLE).asComponent();
                    var subTitle = Message.of(LangKeys.TEAM_BLIND_TRAP_TRIGGERED_SUBTITLE).asComponent();

                    for (var teamPlayer : runningTeam.getConnectedPlayers()) {
                        // alert team
                        Sounds.playSound(teamPlayer,  teamPlayer.getLocation(), Main.getInstance().getConfig().getString("sounds.on_trap_triggered"),
                                Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(teamPlayer), title, subTitle, 20, 60, 0);
                    }
                }
            }
        }
    }
}
