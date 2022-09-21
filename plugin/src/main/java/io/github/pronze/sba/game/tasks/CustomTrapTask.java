package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.IArena;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.player.PlayerMapper;

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

                    });
                });
    }

    private static Map<String,CustomTrap> knownTraps = new HashMap<>();
    public static void registerTrap(CustomTrap trap)
    {
        knownTraps.put(trap.getIdentifier(),trap);
    }
}
