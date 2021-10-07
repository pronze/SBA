package io.github.pronze.sba.game.tasks;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBASpawnerTierUpgradeEvent;
import io.github.pronze.sba.game.*;
import io.github.pronze.sba.lang.LangKeys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;

import java.text.SimpleDateFormat;

public class GeneratorTask extends BaseGameTask {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private final String diamond;
    private final String emerald;
    private final double multiplier;
    private final boolean timerUpgrades;
    private final boolean showUpgradeMessage;
    private GameTierEvent nextEvent;
    private int elapsedTime;

    public GeneratorTask() {
        nextEvent = GameTierEvent.DIAMOND_GEN_UPGRADE_TIER_II;
        diamond = Message.of(LangKeys.DIAMOND).asComponent();
        emerald = Message.of(LangKeys.EMERALD).asComponent();

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
                        Message.of(LangKeys.GENERATOR_UPGRADE_MESSAGE)
                                .placeholder("MatName", matName)
                                .placeholder("tier", tierName)
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
            return AdventureHelper.toLegacy(Message.of(LangKeys.GAME_END_MESSAGE).asComponent());
        }
        return nextEvent.getKey();
    }
}
