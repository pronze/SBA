package io.github.pronze.sba.service;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBASpawnerTierUpgradeEvent;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.game.RotatingGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.util.HashMap;
import java.util.Map;

@Service
public class DynamicSpawnerLimiterService implements Listener {
    private final Map<Integer, Integer> diamondLimiter = new HashMap<>();
    private final Map<Integer, Integer> emeraldLimiter = new HashMap<>();

    @OnPostEnable
    public void onPostEnable() {
        SBA.getInstance().registerListener(this);

        diamondLimiter.put(1, SBAConfig.getInstance().node("upgrades", "limit", "Diamond-I").getInt());
        diamondLimiter.put(2, SBAConfig.getInstance().node("upgrades", "limit", "Diamond-II").getInt());
        diamondLimiter.put(3, SBAConfig.getInstance().node("upgrades", "limit", "Diamond-III").getInt());
        diamondLimiter.put(4, SBAConfig.getInstance().node("upgrades", "limit", "Diamond-IV").getInt());

        emeraldLimiter.put(1, SBAConfig.getInstance().node("upgrades", "limit", "Emerald-I").getInt());
        emeraldLimiter.put(2, SBAConfig.getInstance().node("upgrades", "limit", "Emerald-II").getInt());
        emeraldLimiter.put(3, SBAConfig.getInstance().node("upgrades", "limit", "Emerald-III").getInt());
        emeraldLimiter.put(4, SBAConfig.getInstance().node("upgrades", "limit", "Emerald-IV").getInt());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStart(BedwarsGameStartedEvent event) {
        setAccordingly(event.getGame());
    }

    @EventHandler
    public void onSpawnerUpgrade(SBASpawnerTierUpgradeEvent event) {
        setAccordingly(event.getGame());
    }

    private void setAccordingly(Game game) {
        final var arena = ArenaManager
                .getInstance()
                .get(game.getName())
                .orElseThrow();

        ((Arena) arena).getRotatingGenerators().stream()
                .map(iRotatingGenerator -> (RotatingGenerator) iRotatingGenerator)
                .forEach(generator -> {
                    int limit = 4;

                    switch (generator.getStack().getType()) {
                        case EMERALD_BLOCK:
                            limit = emeraldLimiter.getOrDefault(generator.getTierLevel(), 4);
                            break;
                        case DIAMOND_BLOCK:
                            limit = diamondLimiter.getOrDefault(generator.getTierLevel(), 4);
                            break;
                    }

                    final var spawner = generator.getItemSpawner();
                    Reflect.setField(spawner, "maxSpawnedResources", limit);
                });
    }


}
