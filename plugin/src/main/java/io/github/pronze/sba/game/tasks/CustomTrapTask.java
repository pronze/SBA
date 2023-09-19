package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;

import java.util.HashMap;
import java.util.Map;

import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.player.Players;

public class CustomTrapTask extends BaseGameTask {
    private final double radius;

    public CustomTrapTask() {
        radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);
    }

    @Override
    public void run() {
        arena.getGame().getRunningTeams()
                .stream()
                .forEach(team -> {
                    arena.getStorage().enabledTraps(team).forEach(trap -> {
                        arena.getGame().getConnectedPlayers()
                                .stream()
                                .filter(player -> !Main.getPlayerGameProfile(player).isSpectator)
                                .filter(player -> !team.getConnectedPlayers().contains(player))
                                .forEach(player -> {
                                    if (arena.getStorage().getTargetBlockLocation(team).orElseThrow()
                                            .distanceSquared(player.getLocation()) <= radius) {
                                        final var triggeredEvent = new SBATeamTrapTriggeredEvent(player, team, arena);
                                        SBA.getPluginInstance().getServer().getPluginManager()
                                                .callEvent(triggeredEvent);

                                        if (triggeredEvent.isCancelled()) {
                                            return;
                                        }

                                        arena.getStorage().setPurchasedTrap(team, false, trap);

                                        CustomTrap customTrap = knownTraps.get(trap);
                                        if (customTrap.target.equals("enemy") || customTrap.target.equals("all")) {
                                            customTrap.effects.forEach(effect -> {
                                                player.addPotionEffect(effect);
                                            });
                                        }
                                        if (customTrap.target.equals("team") || customTrap.target.equals("all")) {
                                            customTrap.effects.forEach(effect -> {
                                                team.getConnectedPlayers()
                                                        .forEach(teamPlayer -> teamPlayer.addPotionEffect(effect));
                                            });
                                        }

                                        if (arena.isPlayerHidden(player)) {
                                            arena.removeHiddenPlayer(player);
                                        }

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.TEAM_CUSTOM_TRAP_TRIGGERED_MESSAGE)
                                                .replace("%trap%",trap)
                                                .replace("%team%", arena.getGame().getTeamOfPlayer(player).getName())
                                                .send(Players.wrapPlayer(player).as(SBAPlayerWrapper.class));

                                        var title = LanguageService
                                                .getInstance()
                                                .get(MessageKeys.TEAM_CUSTOM_TRAP_TRIGGERED_TITLE)
                                                .replace("%trap%",trap)
                                                .toComponent();

                                        var subTitle = LanguageService
                                                .getInstance()
                                                .get(MessageKeys.TEAM_CUSTOM_TRAP_TRIGGERED_SUBTITLE)
                                                .replace("%trap%",trap)
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
                                });
                    });
                });
    }

    private static Map<String, CustomTrap> knownTraps = new HashMap<>();

    public static void registerTrap(CustomTrap trap) {
        knownTraps.put(trap.getIdentifier(), trap);
    }
}
