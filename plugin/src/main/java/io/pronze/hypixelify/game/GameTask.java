package io.pronze.hypixelify.game;

import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.events.TeamTrapTriggeredEvent;
import io.pronze.hypixelify.game.Arena;
import io.pronze.hypixelify.game.GameStorage;
import io.pronze.hypixelify.message.Messages;
import io.pronze.hypixelify.specials.Dragon;
import io.pronze.hypixelify.utils.SBAUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.utils.Sounds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class GameTask extends BukkitRunnable {

    private final Map<Integer, String> Tiers = new HashMap<>();
    private final Map<Integer, Integer> tier_timer = new HashMap<>();
    private final SimpleDateFormat dateFormat;
    private final double multiplier;
    private final Game game;
    private final Arena arena;
    private final GameStorage storage;
    private final boolean timerUpgrades;
    private final boolean showUpgradeMessage;
    private int time;
    private int tier = 1;

    public GameTask(Arena arena) {
        this.arena = arena;
        this.game = arena.getGame();
        this.storage = arena.getStorage();
        dateFormat = new SimpleDateFormat("mm:ss");
        timerUpgrades = SBAHypixelify.getConfigurator().config
                .getBoolean("upgrades.timer-upgrades-enabled", true);
        showUpgradeMessage = SBAHypixelify.getConfigurator().config
                .getBoolean("upgrades.show-upgrade-message", true);

        byte inc = 1;
        for (int i = 1; i < 9; i++) {
            final var romanNumeral = SBAUtil.romanNumerals.get(inc);
            final var material = i % 2  == 0 ?
                    SBAHypixelify.getConfigurator().getString("message.emerald") :
                    SBAHypixelify.getConfigurator().getString("message.diamond");

            final var str = material + "-" + romanNumeral;
            Tiers.put(i, str);

            final var configMat = i % 2 == 0 ? "Emerald" : "Diamond";
            final var m_Time = SBAHypixelify.getConfigurator().config
                    .getInt("upgrades.time." + configMat + "-" + romanNumeral);
            tier_timer.put(i, m_Time);

            if (i % 2 == 0) inc+= 1;
        }

        Tiers.put(9, SBAHypixelify.getConfigurator().getString("message.game-end"));
        tier_timer.put(9, game.getGameTime());
        multiplier = SBAHypixelify.getConfigurator().config.getDouble("upgrades.multiplier", 0.25);
        runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (game.getStatus() == GameStatus.RUNNING) {

            if (storage.areTrapsEnabled()) {
                game.getConnectedPlayers().forEach(player-> {
                    if (Main.getPlayerGameProfile(player).isSpectator) return;

                    game.getRunningTeams().forEach(team-> {
                        if (!storage.isTrapEnabled(team) || team.isPlayerInTeam(player)) return;

                        if (storage.getTargetBlockLocation(team)
                                .distanceSquared(player.getLocation()) <= arena.radius) {
                            final var triggeredEvent = new TeamTrapTriggeredEvent(player, team, arena);
                            SBAHypixelify.getInstance().getServer().getPluginManager().callEvent(triggeredEvent);

                            if (!triggeredEvent.isCancelled()) {
                                storage.setTrap(team, false);
                                player.addPotionEffect(new PotionEffect
                                        (PotionEffectType.BLINDNESS, 20 * 3, 2));

                                player.sendMessage(SBAHypixelify
                                        .getConfigurator()
                                        .getString("message.trap-triggered.message"));

                                team.getConnectedPlayers().forEach(pl -> {
                                    Sounds.playSound(pl, pl.getLocation(), Main.getConfigurator()
                                                    .config.getString("sounds.on_trap_triggered"),
                                            Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    sendTitle(pl, Messages.trapTriggered_title,
                                            Messages.trapTriggered_subtitle, 20, 60, 0);
                                });
                            }
                        }
                    });
                });
            }

            if (storage.arePoolEnabled()) {
                game.getRunningTeams().forEach(team-> {
                    if (!storage.isPoolEnabled(team)) return;

                    team.getConnectedPlayers().forEach(player-> {
                        if (Main.getPlayerGameProfile(player).isSpectator) return;
                        if (storage.getTargetBlockLocation(team)
                                .distanceSquared(player.getLocation()) <= arena.radius) {
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
                                    matName = "§b" + SBAHypixelify.getConfigurator().getString("message.diamond");
                                    type = Material.DIAMOND_BLOCK;
                                }
                            } else {
                                if (itemSpawner.getItemSpawnerType().getMaterial() == Material.EMERALD) {
                                    itemSpawner.addToCurrentLevel(multiplier);
                                    matName = "§a" + SBAHypixelify.getConfigurator().getString("message.emerald");
                                    type = Material.EMERALD_BLOCK;
                                }
                            }
                        }

                        final var tierName = Tiers.get(tier);
                        final var tierLevel = tierName.substring(tierName.lastIndexOf("-") + 1);

                        for (final var generator : arena.getRotatingGenerators()) {
                            final var generatorMatType = generator.getItemStack().getType();
                            if (generatorMatType == type) {
                                final var lines = RotatingGenerators.format;
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
                                    player.sendMessage(Messages.generatorUpgrade
                                            .replace("{MatName}", finalMatName)
                                            .replace("{tier}", Tiers.get(tier))));
                        }
                    }
                    tier++;
                }
            }

         ////we have reached the ender dragon stage :)
         //else {
         //    if (storage.areDragonsEnabled()) {
         //        game.getRunningTeams().forEach(team-> {
         //            final var isEnabled = storage.isDragonEnabled(team);
         //            final var firstPlayer = team.getConnectedPlayers().get(0);

         //            //why? idk
         //            if (firstPlayer == null) {
         //                return;
         //            }

         //            if (isEnabled) {
         //                new Dragon(game, firstPlayer, team, game.getSpectatorSpawn()).spawn();
         //                storage.setDragon(team, false);
         //            }
         //        });

         //        game.getConnectedPlayers().forEach(player-> {
         //            player.sendMessage(SBAHypixelify.getConfigurator().getString("message.dragon-spawn"));
         //        });
         //    }
         //}

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
