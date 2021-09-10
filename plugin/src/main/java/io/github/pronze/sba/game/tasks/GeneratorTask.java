package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBASpawnerTierUpgradeEvent;
import io.github.pronze.sba.game.*;
import io.github.pronze.sba.lib.lang.LanguageService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;

import java.text.SimpleDateFormat;

public class GeneratorTask implements Runnable {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private final Arena arena;
    private final String diamond;
    private final String emerald;
    private final double multiplier;
    private final Game game;
    private final boolean timerUpgrades;
    private final boolean showUpgradeMessage;
    private GameTierEvent nextEvent;
    private int elapsedTime;

    public GeneratorTask(IArena arena) {
        this.arena = (Arena) arena;
        nextEvent = GameTierEvent.DIAMOND_GEN_UPGRADE_TIER_II;
        diamond = LanguageService
                .getInstance()
                .get(MessageKeys.DIAMOND)
                .toString();
        emerald = LanguageService
                .getInstance()
                .get(MessageKeys.EMERALD)
                .toString();

        this.game = arena.getGame();

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
                    final var tierName = nextEvent.getKey();
                    GeneratorUpgradeType upgradeType = GeneratorUpgradeType.fromString(tierName.substring(0, tierName.indexOf("-")));
                    String matName = null;
                    Material type = null;

                    switch (upgradeType) {
                        case DIAMOND:
                            matName = "§b" + diamond;
                            type = Material.DIAMOND_BLOCK;
                            break;
                        case EMERALD:
                            matName = "§a" + emerald;
                            type = Material.EMERALD_BLOCK;
                            break;
                    }

                    // check to see if the spawners exist
                    var emptyQuery = game.getItemSpawners()
                            .stream()
                            .filter(itemSpawner -> itemSpawner.getItemSpawnerType().getMaterial() == upgradeType.getMaterial())
                            .findAny()
                            .isEmpty();

                    if (emptyQuery) {
                        type = null;
                    }

                    game.getItemSpawners().forEach(itemSpawner -> {
                        if (itemSpawner.getItemSpawnerType().getMaterial() == upgradeType.getMaterial()) {
                            itemSpawner.addToCurrentLevel(multiplier);
                        }
                    });


                    Material finalType = type;
                    arena.getRotatingGenerators().stream()
                            .map(generator -> (RotatingGenerator) generator)
                            .filter(generator -> generator.getStack().getType() == finalType)
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
                nextEvent = nextEvent.getNextEvent();
            }
        }
        elapsedTime++;
    }

    public String getTimeLeftForNextEvent() {
        return dateFormat.format((nextEvent.getTime() - elapsedTime) * 1000);
    }

    public String getNextTierName() {
        if (nextEvent == GameTierEvent.GAME_END) {
            return LanguageService
                    .getInstance()
                    .get(MessageKeys.GAME_END_MESSAGE)
                    .toString();
        }
        return nextEvent.getKey();
    }
}
