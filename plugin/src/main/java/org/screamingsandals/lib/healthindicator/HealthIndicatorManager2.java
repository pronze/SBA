/*
 * Copyright 2022 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.screamingsandals.lib.healthindicator;

import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.lib.Core;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.PlayerLeaveEvent;
import org.screamingsandals.lib.packet.PacketMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.visuals.Visual;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@ServiceDependencies(dependsOn = {
        Core.class,
        PacketMapper.class
})
public class HealthIndicatorManager2 {
    private static HealthIndicatorManager2 manager;
    protected final Map<UUID, HealthIndicator2> activeIndicators = new HashMap<>();

    @ApiStatus.Internal
    public HealthIndicatorManager2() {
        if (manager != null) {
            throw new UnsupportedOperationException("HealthIndicatorManager is already initialized!");
        }
        manager = this;
    }

    public static Map<UUID, HealthIndicator2> getActiveIndicators() {
        if (manager == null) {
            throw new UnsupportedOperationException("HealthIndicatorManager is not initialized yet!");
        }
        return Map.copyOf(manager.activeIndicators);
    }

    public static Optional<HealthIndicator2> getHealthIndicator(UUID uuid) {
        if (manager == null) {
            throw new UnsupportedOperationException("HealthIndicatorManager is not initialized yet!");
        }
        return Optional.ofNullable(manager.activeIndicators.get(uuid));
    }

    public static void addHealthIndicator(HealthIndicator2 healthIndicator) {
        if (manager == null) {
            throw new UnsupportedOperationException("HealthIndicatorManager is not initialized yet!");
        }
        manager.activeIndicators.put(healthIndicator.uuid(), healthIndicator);
    }

    public static void removeHealthIndicator(UUID uuid) {
        getHealthIndicator(uuid).ifPresent(HealthIndicatorManager2::removeHealthIndicator);
    }

    public static void removeHealthIndicator(HealthIndicator2 healthIndicator) {
        if (manager == null) {
            throw new UnsupportedOperationException("HealthIndicatorManager is not initialized yet!");
        }
        manager.activeIndicators.remove(healthIndicator.uuid());
    }

    public static HealthIndicator2 healthIndicator() {
        return healthIndicator(UUID.randomUUID());
    }

    public static HealthIndicator2 healthIndicator(UUID uuid) {
        if (manager == null) {
            throw new UnsupportedOperationException("HealthIndicatorManager is not initialized yet!");
        }

        final var healthIndicator = manager.healthIndicator0(uuid);
        addHealthIndicator(healthIndicator);
        return healthIndicator;
    }

    protected HealthIndicator2 healthIndicator0(UUID uuid) {
        return new HealthIndicatorImpl2(uuid);
    }

    @OnPreDisable
    public void destroy() {
        getActiveIndicators()
                .values()
                .forEach(Visual::destroy);
        manager.activeIndicators.clear();
    }

    @OnEvent
    public void onLeave(PlayerLeaveEvent event) {
        if (activeIndicators.isEmpty()) {
            return;
        }

        getActiveIndicators().forEach((key, indicator) -> {
            if (indicator.viewers().contains(event.player())) {
                indicator.removeViewer(event.player());
                indicator.removeTrackedPlayer(event.player());
            }
            if (!indicator.hasViewers()) {
                removeHealthIndicator(indicator);
            }
        });
    }
}