package io.github.pronze.sba.game;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.data.GeneratorData;
import io.github.pronze.sba.events.SBASpawnerTierUpgradeEvent;
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.wrapper.PlayerWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.player.PlayerMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GameTask extends BukkitRunnable {
    private final double radius;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private final String diamond;
    private final String emerald;
    private final double multiplier;
    private final Game game;
    private final Arena arena;
    private final GameStorage storage;
    private final boolean timerUpgrades;
    private final boolean showUpgradeMessage;
    private GameEvent nextEvent;
    private int elapsedTime;
    private int tier = 2;

    public GameTask(Arena arena) {
        radius = Math.pow(SBAConfig.getInstance().node("upgrades", "trap-detection-range").getInt(7), 2);

        nextEvent = GameEvent.DIAMOND_GEN_UPGRADE_TIER_II;
        diamond = LanguageService
                .getInstance()
                .get(MessageKeys.DIAMOND)
                .toString();
        emerald = LanguageService
                .getInstance()
                .get(MessageKeys.EMERALD)
                .toString();

        this.arena = arena;
        this.game = arena.getGame();
        this.storage = arena.getStorage();

        timerUpgrades = SBAConfig
                .getInstance()
                .getBoolean("upgrades.timer-upgrades-enabled", true);
        showUpgradeMessage = SBAConfig
                .getInstance()
                .getBoolean("upgrades.show-upgrade-message", true);

        multiplier = SBAConfig.getInstance().getDouble("upgrades.multiplier", 0.25);
        runTaskTimer(SBA.getPluginInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (game.getStatus() == GameStatus.RUNNING) {

            // LOGIC FOR TRAPS
            if (storage.areTrapsEnabled()) {
                game.getRunningTeams()
                        .stream()
                        .filter(storage::isTrapEnabled)
                        .forEach(team -> game.getConnectedPlayers()
                                .stream()
                                .filter(player -> !Main.getPlayerGameProfile(player).isSpectator)
                                .filter(player -> !team.getConnectedPlayers().contains(player))
                                .forEach(player -> {

                                    if (storage.getTargetBlockLocation(team).distanceSquared(player.getLocation()) <= radius) {
                                        final var triggeredEvent = new SBATeamTrapTriggeredEvent(player, team, arena);
                                        SBA.getPluginInstance().getServer().getPluginManager().callEvent(triggeredEvent);

                                        if (triggeredEvent.isCancelled()) {
                                            return;
                                        }

                                        storage.setTrap(team, false);
                                        player.addPotionEffect(new PotionEffect
                                                (PotionEffectType.BLINDNESS, 20 * 3, 2));

                                        if (arena.isPlayerHidden(player)) {
                                            arena.removeHiddenPlayer(player);
                                        }

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.TEAM_TRAP_TRIGGERED_MESSAGE).replace("%team%", game.getTeamOfPlayer(player).getName())
                                                .send(PlayerMapper.wrapPlayer(player).as(PlayerWrapper.class));

                                        var title = LanguageService
                                                .getInstance()
                                                .get(MessageKeys.TEAM_TRAP_TRIGGERED_TITLE)
                                                .toString();

                                        var subTitle = LanguageService
                                                .getInstance()
                                                .get(MessageKeys.TEAM_TRAP_TRIGGERED_SUBTITLE)
                                                .toString();

                                        team.getConnectedPlayers().forEach(pl -> {
                                            Sounds.playSound(pl, pl.getLocation(), Main.getInstance().getConfig().getString("sounds.on_trap_triggered"),
                                                    Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), title, subTitle, 20, 60, 0);
                                        });
                                    }
                                }));
            }

            // LOGIC FOR HEAL POOL
            if (storage.arePoolEnabled()) {
                game.getRunningTeams()
                        .stream()
                        .filter(storage::isPoolEnabled)
                        .forEach(team -> team.getConnectedPlayers()
                                .stream()
                                .filter(player -> !Main.getPlayerGameProfile(player).isSpectator)
                                .forEach(player -> {
                                    if (storage.getTargetBlockLocation(team).distanceSquared(player.getLocation()) <= radius) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1));
                                    }
                                }));
            }

            if (nextEvent != GameEvent.GAME_END) {
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
                tier++;
                nextEvent = nextEvent.getNextEvent();
            }
        }
        elapsedTime++;
    } else {
        this.cancel();
    }
}

    public String getTimeLeftForNextEvent() {
        return dateFormat.format((nextEvent.getTime() - elapsedTime) * 1000);
    }

    public String getNextTierName() {
        if (nextEvent.getKey().equals("GameEnd")) {
            return LanguageService
                    .getInstance()
                    .get(MessageKeys.GAME_END_MESSAGE)
                    .toString();
        }
        return nextEvent.getKey();
    }
}
