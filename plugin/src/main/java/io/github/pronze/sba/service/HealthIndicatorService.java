package io.github.pronze.sba.service;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.healthindicator.HealthIndicator;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.visuals.Visual;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class HealthIndicatorService implements Listener {
    private final Map<Game, HealthIndicator> healthIndicatorMap = new HashMap<>();

    private final SBA plugin;
    private final SBAConfig config;
    private boolean tabEnabled;

    @OnPostEnable
    public void postEnable() {
        if (!config.node("health-indicator", "enabled").getBoolean(true)) {
            return;
        }

        this.tabEnabled = config
                .node("show-health-in-tablist")
                .getBoolean(true);

        plugin.registerListener(this);
    }

    @OnPreDisable
    public void onDestroy() {
        healthIndicatorMap
                .values()
                .forEach(Visual::destroy);
        healthIndicatorMap.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStart(BedwarsGameStartedEvent event) {
        final var game = event.getGame();
        final var healthIndicator = HealthIndicator.of()
                .symbol(Component.text("\u2665", NamedTextColor.RED))
                .showHealthInTabList(tabEnabled)
                .show()
                .startUpdateTask(2, TaskerTime.TICKS);

        game.getConnectedPlayers()
                .stream()
                .map(PlayerMapper::wrapPlayer)
                .forEach(healthIndicator::addViewer);

        game.getConnectedPlayers()
                .stream()
                .map(PlayerMapper::wrapPlayer)
                .forEach(healthIndicator::addTrackedPlayer);

        healthIndicatorMap.put(game, healthIndicator);
    }

    @EventHandler
    public void onPlayerLeave(BedwarsPlayerLeaveEvent event) {
        final var playerWrapper = PlayerMapper.wrapPlayer(event.getPlayer());
        final var healthIndicator = healthIndicatorMap.get(event.getGame());
        if (healthIndicator != null) {
            healthIndicator.removeViewer(playerWrapper);
            healthIndicator.removeTrackedPlayer(playerWrapper);
        }
    }

    @EventHandler
    public void onBedWarsGameEndingEvent(BedwarsGameEndingEvent event) {
        final var healthIndicator = healthIndicatorMap.get(event.getGame());
        if (healthIndicator != null) {
            healthIndicator.destroy();
            healthIndicatorMap.remove(event.getGame());
        }
    }
}
