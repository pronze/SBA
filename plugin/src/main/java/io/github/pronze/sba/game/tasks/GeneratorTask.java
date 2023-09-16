package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBASpawnerTierUpgradeEvent;
import io.github.pronze.sba.game.*;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;

import java.util.regex.Pattern;

import javax.naming.NameNotFoundException;

public class GeneratorTask extends BaseGameTask {
    private final double multiplier;
    private final boolean timerUpgrades;
    private final boolean showUpgradeMessage;
    private GameTierEvent nextEvent;
    private int elapsedTime;

    public GeneratorTask() {
        nextEvent = GameTierEvent.first();

        timerUpgrades = SBAConfig
                .getInstance()
                .getBoolean("upgrades.timer-upgrades-enabled", true);
        showUpgradeMessage = SBAConfig
                .getInstance()
                .getBoolean("upgrades.show-upgrade-message", true);

        multiplier = SBAConfig.getInstance().getDouble("upgrades.multiplier", 0.25);
    }

    @Override
    public void run() {
        if (nextEvent != GameTierEvent.GAME_END) {
            if (elapsedTime == nextEvent.getTime()) {
                if (timerUpgrades) {
                    try {
                        var tierName = nextEvent.getKey();
                        var upgradeType = GeneratorUpgradeType.fromString(tierName.substring(0, tierName.indexOf("-")));
                        tierName = getNextTierName();

                        String matName = null;
                        Material type = null;

                        type = upgradeType.getMaterial();
                        matName = upgradeType.getColor().toString() + upgradeType.getItemName();

                        // check to see if the spawners exist
                        var emptyQuery = game.getItemSpawners()
                                .stream()
                                .filter(itemSpawner -> itemSpawner.getItemSpawnerType().getMaterial() == upgradeType
                                        .getMaterial())
                                .findAny()
                                .isEmpty();

                        if (emptyQuery) {
                            type = null;
                        } else {
                            game.getItemSpawners().forEach(itemSpawner -> {
                                if (itemSpawner.getItemSpawnerType().getMaterial() == upgradeType.getMaterial()) {
                                    itemSpawner.addToCurrentLevel(multiplier);
                                }
                            });
                            Material finalType = type;
                            arena.getRotatingGenerators().stream()
                                    .map(generator -> (RotatingGenerator) generator)
                                    .filter(generator -> generator.getItemSpawner().getItemSpawnerType()
                                            .getMaterial() == finalType)
                                    .forEach(generator -> {
                                        final var event = new SBASpawnerTierUpgradeEvent(game, generator);
                                        Bukkit.getServer().getPluginManager().callEvent(event);
                                        if (event.isCancelled()) {
                                            return;
                                        }
                                        generator.setTierLevel(generator.getTierLevel() + 1);
                                    });

                            if (showUpgradeMessage && finalType != null) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GENERATOR_UPGRADE_MESSAGE)
                                        .replace("%MatName%", matName)
                                        .replace("%tier%", tierName)
                                        .send(game
                                                .getConnectedPlayers()
                                                .stream()
                                                .map(PlayerMapper::wrapPlayer)
                                                .toArray(org.screamingsandals.lib.player.PlayerWrapper[]::new));
                            }
                        }
                    } catch (NameNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                nextEvent = nextEvent.getNextEvent();
            }

        }
        elapsedTime++;
    }

    public String getTimeLeftForNextEvent() {
        if (nextEvent == GameTierEvent.GAME_END) {
            return ((org.screamingsandals.bedwars.game.Game) game).getFormattedTimeLeft();
        } else {
            return ((org.screamingsandals.bedwars.game.Game) game).getFormattedTimeLeft(nextEvent.getTime() - elapsedTime);
        }
    }

    public String getNextTierName() {
        if (nextEvent == GameTierEvent.GAME_END) {
            return LanguageService
                    .getInstance()
                    .get(MessageKeys.GAME_END_MESSAGE)
                    .toString();
        } else {
            try {
                var tierName = nextEvent.getKey();
                var upgradeType = GeneratorUpgradeType.fromString(tierName.substring(0, tierName.indexOf("-")));
                Pattern STRIP_COLOR_PATTERN = Pattern
                        .compile("(?i)" + String.valueOf(ChatColor.COLOR_CHAR) + "[0-9A-FK-ORX]");
                tierName = STRIP_COLOR_PATTERN.matcher(upgradeType.getItemName()).replaceAll("") +"-"+ tierName.split("-")[1];
                return tierName;
            } catch (NameNotFoundException e) {
                Logger.warn("{}", e);
            }
        }
        return nextEvent.getKey();
    }
}
