package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBASpawnerTierUpgradeEvent;
import io.github.pronze.sba.game.*;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;

import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

@Service
public class GeneratorTask extends AbstractGameTaskImpl {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private GameEvent nextEvent = GameEvent.DIAMOND_GEN_UPGRADE_TIER_II;
    private int elapsedTime;

    @Override
    public void run() {
        if (nextEvent == GameEvent.GAME_END) {
            // task does not need to run after this GameEvent.
            stop();
            return;
        }

        if (elapsedTime == nextEvent.getTime()) {
            if (SBAConfig
                    .getInstance()
                    .getBoolean("upgrades.timer-upgrades-enabled", true)) {
                GeneratorUpgradeType upgradeType = GeneratorUpgradeType.fromString(nextEvent.getKey().substring(0, nextEvent.name().indexOf("-")));

                String matName;
                Material type;

                switch (upgradeType) {
                    case DIAMOND:
                        matName = "§b" + AdventureHelper.toLegacy(Message.of(LangKeys.DIAMOND).asComponent());
                        type = Material.DIAMOND_BLOCK;
                        break;
                    case EMERALD:
                        matName = "§a" + AdventureHelper.toLegacy(Message.of(LangKeys.EMERALD).asComponent());
                        type = Material.EMERALD_BLOCK;
                        break;
                    default:
                        Logger.trace("Invalid upgrade type!?");
                        return;
                }

                // check to see if the spawners exist
                var emptyQuery = gameWrapper.getItemSpawners()
                        .stream()
                        .filter(itemSpawner -> itemSpawner.getItemSpawnerType().getMaterial() == upgradeType.getMaterial())
                        .findAny()
                        .isEmpty();

                if (emptyQuery) {
                    return;
                }

                for (var itemSpawner : gameWrapper.getItemSpawners()) {
                    if (itemSpawner.getItemSpawnerType().getMaterial() == upgradeType.getMaterial()) {
                        itemSpawner.addToCurrentLevel(SBAConfig.getInstance().getDouble("upgrades.multiplier", 0.25));
                    }
                }

                for (var rotatingGenerator : gameWrapper.getRotatingGenerators()) {
                    if (rotatingGenerator.isType(type)) {
                        final var event = new SBASpawnerTierUpgradeEvent(gameWrapper, rotatingGenerator);
                        EventManager.fire(event);
                        if (event.isCancelled()) {
                            continue;
                        }
                        rotatingGenerator.incrementTier();
                    }
                }

                if (SBAConfig
                        .getInstance()
                        .getBoolean("upgrades.show-upgrade-message", true)) {
                    Message.of(LangKeys.GENERATOR_UPGRADE_MESSAGE)
                            .placeholder("MatName", matName)
                            .placeholder("tier", nextEvent.getTranslatedTitle())
                            .send(gameWrapper
                                    .getConnectedPlayers()
                                    .stream()
                                    .map(PlayerMapper::wrapPlayer)
                                    .collect(Collectors.toList()));
                }
            }
            nextEvent = nextEvent.getNextEvent();
        }
        elapsedTime++;
    }

    public String getTimeLeftForNextEvent() {
        return dateFormat.format((nextEvent.getTime() - elapsedTime) * 1000);
    }

    public String getNextTierName() {
        return nextEvent.getTranslatedTitle();
    }
}
