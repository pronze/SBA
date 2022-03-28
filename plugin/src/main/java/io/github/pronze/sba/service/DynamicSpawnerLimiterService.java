package io.github.pronze.sba.service;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBASpawnerTierUpgradeEvent;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.game.RotatingGenerator;
import io.github.pronze.sba.utils.Logger;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DynamicSpawnerLimiterService implements Listener {
    private final Map<String, Map<Integer, Integer>> limiters = new HashMap<>();

    public static DynamicSpawnerLimiterService getInstance() {
        return ServiceManager.get(DynamicSpawnerLimiterService.class);
    }

    public static final int romanToInteger2(String s) {

        Map<Character, Integer> values = new LinkedHashMap<>();
        values.put('0', 0);
        values.put('I', 1);
        values.put('V', 5);
        values.put('X', 10);
        values.put('L', 50);
        values.put('C', 100);
        values.put('D', 500);
        values.put('M', 1000);

        int number = 0;
        for (int i = 0; i < s.length(); i++) {
            if (i + 1 == s.length() || values.get(s.charAt(i)) >= values.get(s.charAt(i + 1))) {
                number += values.get(s.charAt(i));
            } else {
                number -= values.get(s.charAt(i));
            }
        }
        return number;
    }

    @OnPostEnable
    public void onPostEnable() {
        SBA.getInstance().registerListener(this);
        load();
    }

    public void reload() {
        limiters.clear();
        load();
    }

    private void load() {
        var subkeys = SBAConfig.getInstance().getSubKeys("upgrades.limit");
        for (var key : subkeys) {
            try {
                var parts = key.split("-");
                var item = parts[0].toLowerCase();
                var number = romanToInteger2(parts.length > 1 ? parts[1] : "I");
                if (!limiters.containsKey(item)) {
                    limiters.put(item, new HashMap<>());
                }
                limiters.get(item).putIfAbsent(number,
                        SBAConfig.getInstance().node("upgrades", "limit", key).getInt(1));
            } catch (Throwable t) {
                Logger.error("Key not in right format {};Expecting RESSOURCE-ROMAN_NUMERAL:TIME;{}", key, t);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameStart(BedwarsGameStartedEvent event) {
        final var game = event.getGame();
        setAccordingly(game, false);
    }

    @EventHandler
    public void onSpawnerUpgrade(SBASpawnerTierUpgradeEvent event) {
        setAccordingly(event.getGame(), true);
    }

    private int getTier(Game game, ItemSpawner spawner) {
        final var arena = ArenaManager
                .getInstance()
                .get(game.getName())
                .orElseThrow();

        var rotating = ((Arena) arena).getRotatingGenerators().stream()
                .map(iRotatingGenerator -> (RotatingGenerator) iRotatingGenerator)
                .filter(generator -> generator.getItemSpawner() == spawner).findFirst();

        if (rotating.isPresent())
            return rotating.get().getTierLevel();
        else
            return 1;
    }

    private void setAccordingly(Game game, boolean isUpgraded) {
        for (var spawner : game.getItemSpawners()) {
            var material = spawner.getItemSpawnerType().getName().toLowerCase();
            if (limiters.containsKey(material)) {
                var limiter = limiters.get(material);
                var tier = getTier(game, spawner);
                if (limiter.containsKey(tier))
                    Reflect.setField(spawner, "maxSpawnedResources", limiter.get(tier));
            }
        }
    }
}
