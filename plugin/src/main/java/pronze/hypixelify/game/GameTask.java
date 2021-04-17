package pronze.hypixelify.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.utils.visual.TextEntry;
import org.screamingsandals.bedwars.player.PlayerManager;
import org.screamingsandals.bedwars.utils.Sounds;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.data.GeneratorData;
import pronze.hypixelify.api.events.SBATeamTrapTriggeredEvent;
import pronze.hypixelify.api.game.GameEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@EqualsAndHashCode(callSuper = true)
@Data
public class GameTask extends BukkitRunnable {
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
    private final List<GeneratorData> generatorData = new ArrayList<>();

    public GameTask(Arena arena) {
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
        linkSpawnerData();
        runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
    }

    private void linkSpawnerData() {
        if (SBAConfig.getInstance().getBoolean("floating-generator.enabled", true)) {
            game.getItemSpawners()
                    .stream()
                    .filter(org.screamingsandals.bedwars.api.game.ItemSpawner::getFloatingEnabled)
                    .forEach((spawner) -> {
                        final var mat = spawner.getItemSpawnerType().getMaterial();
                        final var convertedMat = mat == Material.DIAMOND ? Material.DIAMOND_BLOCK :
                                mat == Material.EMERALD ? Material.EMERALD_BLOCK : null;
                        if (convertedMat != null) {
                            ((ItemSpawner) spawner)
                                    .getHologram()
                                    .replaceLine(2, TextEntry.of(LanguageService
                                            .getInstance()
                                            .get(MessageKeys.SPAWNER_HOLO_TIER_FORMAT)
                                            .toString()));
                            generatorData.add(new GeneratorData((ItemSpawner) spawner, new ItemStack(convertedMat)));
                        }
                    });
        }
    }

    @Override
    public void run() {
        if (game.getStatus() == GameStatus.RUNNING) {
            if (storage.areTrapsEnabled()) {
                game.getConnectedPlayers().forEach(player -> {
                    final var bwPlayer = PlayerManager
                            .getInstance()
                            .getPlayer(player.getUniqueId())
                            .orElseThrow();
                    if (bwPlayer.isSpectator) return;

                    game.getRunningTeams().forEach(team -> {
                        if (!storage.isTrapEnabled(team) || team.getConnectedPlayers().contains(player)) return;

                        if (storage.getTargetBlockLocation(team)
                                .distanceSquared(player.getLocation()) <= arena.getRadius()) {
                            final var triggeredEvent = new SBATeamTrapTriggeredEvent(player, team, arena);
                            SBAHypixelify.getInstance().getServer().getPluginManager().callEvent(triggeredEvent);

                            if (!triggeredEvent.isCancelled()) {
                                storage.setTrap(team, false);
                                player.addPotionEffect(new PotionEffect
                                        (PotionEffectType.BLINDNESS, 20 * 3, 2));

                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.TEAM_TRAP_TRIGGERED_MESSAGE)
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
                                    Sounds.playSound(pl, pl.getLocation(), MainConfig.getInstance()
                                                    .node("sounds", "on_trap_triggered").getString(),
                                            Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), title, subTitle, 20, 60, 0);
                                });
                            }
                        }
                    });
                });
            }

            if (storage.arePoolEnabled()) {
                game.getRunningTeams().forEach(team -> {
                    if (!storage.isPoolEnabled(team)) return;

                    team.getConnectedPlayers().forEach(player -> {
                        final var bwPlayer = PlayerManager
                                .getInstance()
                                .getPlayer(player.getUniqueId())
                                .orElseThrow();

                        if (bwPlayer.isSpectator) return;
                        if (storage.getTargetBlockLocation(team)
                                .distanceSquared(player.getLocation()) <= arena.getRadius()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                                    30, 1));
                        }
                    });
                });
            }

            // Only proceed if the next event is not GameEnd.
            if (!nextEvent.getKey().equals("GameEnd")) {
                if (elapsedTime == nextEvent.getTime()) {
                    if (timerUpgrades) {
                        final AtomicReference<String> matName = new AtomicReference<>();
                        final AtomicReference<Material> type = new AtomicReference<>();

                        game.getItemSpawners().forEach(itemSpawner -> {
                            if (nextEvent.getKey().contains("Diamond")) {
                                if (itemSpawner.getItemSpawnerType().getMaterial() == Material.DIAMOND) {
                                    itemSpawner.addToCurrentLevel(multiplier);
                                    matName.set("§b" + diamond);
                                    type.set(Material.DIAMOND_BLOCK);
                                }
                            } else if (nextEvent.getKey().contains("Emerald")) {
                                if (itemSpawner.getItemSpawnerType().getMaterial() == Material.EMERALD) {
                                    itemSpawner.addToCurrentLevel(multiplier);
                                    matName.set("§a" + emerald);
                                    type.set(Material.EMERALD_BLOCK);
                                }
                            }
                        });

                        final var tierName = nextEvent.getKey();
                        final var tierLevel = tierName.substring(tierName.lastIndexOf("-") + 1);

                        generatorData.forEach(generator -> {
                            final var generatorMatType = generator.getItemStack().getType();
                            if (generatorMatType == type.get()) {
                                generator
                                        .getItemSpawner()
                                        .getHologram()
                                        .bottomLine(TextEntry.of(LanguageService
                                                .getInstance()
                                                .get(MessageKeys.SPAWNER_HOLO_TIER_FORMAT)
                                                .toString()
                                                .replace("%tier%", tierLevel)));
                                generator.setTierLevel(generator.getTierLevel() + 1);
                            }
                        });

                        if (showUpgradeMessage && matName.get() != null) {
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.GENERATOR_UPGRADE_MESSAGE)
                                    .replace("%MatName%", matName.get())
                                    .replace("%tier%", tierName)
                                    .send(game
                                            .getConnectedPlayers()
                                            .stream()
                                            .map(PlayerMapper::wrapPlayer)
                                            .toArray(org.screamingsandals.bedwars.lib.player.PlayerWrapper[]::new));
                        }
                    }
                    tier++;
                    nextEvent = GameEvent.ofOrdinal(nextEvent.ordinal() + 1);
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
