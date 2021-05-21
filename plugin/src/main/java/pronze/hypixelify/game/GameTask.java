package pronze.hypixelify.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.data.GeneratorData;
import pronze.hypixelify.api.events.SBATeamTrapTriggeredEvent;
import pronze.hypixelify.api.game.GameEvent;
import pronze.hypixelify.api.game.GeneratorUpgradeType;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
    private final List<GeneratorData> generatorData = new ArrayList<>();
    private GameEvent nextEvent;
    private int elapsedTime;
    private int tier = 2;

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
        runTaskTimer(SBAHypixelify.getPluginInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (game.getStatus() == GameStatus.RUNNING) {

            // LOGIC FOR TRAPS
            if (storage.areTrapsEnabled()) {
                game.getConnectedPlayers().forEach(player -> {
                    final var bwPlayer = Main.getPlayerGameProfile(player);
                    if (bwPlayer.isSpectator) return;

                    game.getRunningTeams().forEach(team -> {
                        if (storage.isTrapEnabled(team)) {
                            if (team.getConnectedPlayers().contains(player)) {
                                return;
                            }

                            if (storage.getTargetBlockLocation(team)
                                    .distanceSquared(player.getLocation()) <= arena.getRadius()) {
                                final var triggeredEvent = new SBATeamTrapTriggeredEvent(player, team, arena);
                                SBAHypixelify.getPluginInstance().getServer().getPluginManager().callEvent(triggeredEvent);

                                if (!triggeredEvent.isCancelled()) {
                                    storage.setTrap(team, false);
                                    player.addPotionEffect(new PotionEffect
                                            (PotionEffectType.BLINDNESS, 20 * 3, 2));

                                    if (arena.isPlayerHidden(player)) {
                                        arena.removeHiddenPlayer(player);
                                    }

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
                                        Sounds.playSound(pl, pl.getLocation(), Main.getInstance().getConfig().getString("sounds.on_trap_triggered"),
                                                Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), title, subTitle, 20, 60, 0);
                                    });
                                }
                            }

                        }

                    });
                });
            }

            // LOGIC FOR HEAL POOL
            if (storage.arePoolEnabled()) {
                game.getRunningTeams()
                        .stream()
                        .filter(storage::isPoolEnabled)
                        .forEach(team -> team.getConnectedPlayers().forEach(player -> {
                            final var bwPlayer = Main.getPlayerGameProfile(player);

                            if (bwPlayer.isSpectator) return;
                            if (storage.getTargetBlockLocation(team)
                                    .distanceSquared(player.getLocation()) <= arena.getRadius()) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                                        30, 1));
                            }
                        }));
            }

            if (nextEvent != GameEvent.GAME_END) {
                if (elapsedTime == nextEvent.getTime()) {
                    if (timerUpgrades) {
                        final var tierName = nextEvent.getKey();
                        GeneratorUpgradeType upgradeType = GeneratorUpgradeType.fromString(nextEvent.getKey());
                        String matName = null;
                        Material type = null;

                        switch (upgradeType) {
                            case DIAMOND:
                                matName = "§b" + diamond;
                                type = Material.valueOf(SBAConfig.getInstance().getString("floating-generator", "diamond-block"));
                                break;
                            case EMERALD:
                                matName = "§a" + emerald;
                                type = Material.valueOf(SBAConfig.getInstance().getString("floating-generator", "emerald-block"));
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
                        generatorData.forEach(generator -> {
                            final var generatorMatType = generator.getItemStack().getType();
                            if (generatorMatType == finalType) {
                                generator.setTierLevel(generator.getTierLevel() + 1);
                            //  generator
                            //          .getItemSpawner()
                            //          .getHologram()
                            //          .bottomLine(TextEntry.of(LanguageService
                            //                  .getInstance()
                            //                  .get(MessageKeys.SPAWNER_HOLO_TIER_FORMAT)
                            //                  .toString()
                            //                  .replace("%tier%", String.valueOf(generator.getTierLevel()))));
                            }
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
