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
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.bedwars.lib.nms.holograms.Hologram;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.utils.Sounds;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBATeamTrapTriggeredEvent;
import pronze.hypixelify.utils.SBAUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pronze.hypixelify.lib.lang.I.i18n;

@EqualsAndHashCode(callSuper = true)
@Data
public class GameTask extends BukkitRunnable {
    private final Map<Integer, String> Tiers = new HashMap<>();
    private final Map<Integer, Integer> tier_timer = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private final double multiplier;
    private final Game game;
    private final ArenaImpl arena;
    private final GameStorage storage;
    private final boolean timerUpgrades;
    private final boolean showUpgradeMessage;
    private int time;
    private int tier = 1;
    private final List<RotatingGenerator> rotatingGenerators = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public GameTask(ArenaImpl arena) {
        this.arena = arena;
        this.game = arena.getGame();
        try {
            final var gameHolosField = game
                    .getClass()
                    .getDeclaredField("countdownHolograms");
            gameHolosField.setAccessible(true);
            final var gameHolos = (Map<ItemSpawner, Hologram>) gameHolosField.get(game);
            final var copy = Map.copyOf(gameHolos);
            copy.forEach((spawner, holo) -> {
                if (spawner.getFloatingEnabled()) {
                    final var mat = spawner.getItemSpawnerType().getMaterial();
                    final var convertedMat = mat == Material.DIAMOND ? Material.DIAMOND_BLOCK :
                            mat == Material.EMERALD ? Material.EMERALD_BLOCK : null;
                    if (convertedMat != null) {
                        holo.destroy();
                        gameHolos.remove(spawner);
                        rotatingGenerators.add(new RotatingGenerator(arena, spawner,new ItemStack(convertedMat)));
                    }
                }
            });

            gameHolosField.set(game, copy);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.storage = arena.getStorage();
        timerUpgrades = SBAHypixelify.getConfigurator().config
                .getBoolean("upgrades.timer-upgrades-enabled", true);
        showUpgradeMessage = SBAHypixelify.getConfigurator().config
                .getBoolean("upgrades.show-upgrade-message", true);

        byte inc = 1;
        for (int i = 1; i < 9; i++) {
            final var romanNumeral = SBAUtil.romanNumerals.get(inc);
            final var material = i % 2 == 0 ?
                    i18n("emerald") :
                    i18n("diamond");

            final var str = material + "-" + romanNumeral;
            Tiers.put(i, str);

            final var configMat = i % 2 == 0 ? "Emerald" : "Diamond";
            final var m_Time = SBAHypixelify.getConfigurator().config
                    .getInt("upgrades.time." + configMat + "-" + romanNumeral);
            tier_timer.put(i, m_Time);

            if (i % 2 == 0) inc += 1;
        }

        Tiers.put(9, i18n("game-end"));
        tier_timer.put(9, game.getGameTime());
        multiplier = SBAHypixelify.getConfigurator().config.getDouble("upgrades.multiplier", 0.25);
        runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (game.getStatus() == GameStatus.RUNNING) {

            if (storage.areTrapsEnabled()) {
                game.getConnectedPlayers().forEach(player -> {
                    if (Main.getPlayerGameProfile(player).isSpectator) return;

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

                                player.sendMessage(i18n("trap-triggered.message")
                                        .replace("%team%", team.getName()));

                                team.getConnectedPlayers().forEach(pl -> {
                                    Sounds.playSound(pl, pl.getLocation(), MainConfig.getInstance()
                                                    .node("sounds", "on_trap_triggered").getString(),
                                            Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), i18n("trap-triggered.title"),
                                            i18n("trap-triggered.sub-title"), 20, 60, 0);
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
                        if (Main.getPlayerGameProfile(player).isSpectator) return;
                        if (storage.getTargetBlockLocation(team)
                                .distanceSquared(player.getLocation()) <= arena.getRadius()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                                    30, 1));
                        }
                    });
                });
            }

            if (!Tiers.get(tier).equals(Tiers.get(9))) {

                if (time == tier_timer.get(tier)) {
                    if (timerUpgrades) {
                        String matName = null;
                        Material type = null;
                        for (final var itemSpawner : game.getItemSpawners()) {
                            if (tier % 2 != 0) {
                                if (itemSpawner.getItemSpawnerType().getMaterial() == Material.DIAMOND){
                                    itemSpawner.addToCurrentLevel(multiplier);
                                    matName = "§b" + i18n("diamond");
                                    type = Material.DIAMOND_BLOCK;
                                }
                            } else {
                                if (itemSpawner.getItemSpawnerType().getMaterial() == Material.EMERALD) {
                                    itemSpawner.addToCurrentLevel(multiplier);
                                    matName = "§a" + i18n("emerald");
                                    type = Material.EMERALD_BLOCK;
                                }
                            }
                        }

                        final var tierName = Tiers.get(tier);
                        final var tierLevel = tierName.substring(tierName.lastIndexOf("-") + 1);

                        for (final var generator : rotatingGenerators) {
                            final var generatorMatType = generator.getItemStack().getType();
                            if (generatorMatType == type) {
                                final var lines = SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("floating-generator.holo-text");
                                final var newLines = new ArrayList<String>();
                                if (lines != null) {
                                    lines.forEach(line-> {
                                        newLines.add(line
                                                .replace("{time}", String.valueOf(generator.getTime()))
                                                .replace("{tier}", tierLevel)
                                                .replace("{material}", generatorMatType.name()));
                                    });
                                }
                                generator.update(newLines);
                                generator.setTierLevel(generator.getTierLevel() + 1);
                            }
                        }

                        if (showUpgradeMessage && matName != null) {
                            String finalMatName = matName;
                            game.getConnectedPlayers().forEach(player ->
                                    player.sendMessage(i18n("generator-upgrade")
                                            .replace("{MatName}", finalMatName)
                                            .replace("{tier}", Tiers.get(tier))));
                        }
                    }
                    tier++;
                }
            }
            time++;
        } else {
            this.cancel();
        }
    }

    public int getTime() {
        return time;
    }

    public String getFormattedTimeLeft() {
        return dateFormat.format((tier_timer.get(tier) - time) * 1000);
    }

    public String getTier() {
        return Tiers.get(tier);
    }
}
