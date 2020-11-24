package org.pronze.hypixelify.arena;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.events.TeamTrapTriggeredEvent;
import org.pronze.hypixelify.data.GameStorage;
import org.pronze.hypixelify.message.Messages;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.utils.Sounds;

import java.text.SimpleDateFormat;
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
    private int time;
    private int tier = 1;

    public GameTask(Arena arena) {
        this.arena = arena;
        this.game = arena.getGame();
        this.storage = arena.getStorage();
        dateFormat = new SimpleDateFormat("mm:ss");
        timerUpgrades = SBAHypixelify.getConfigurator().config.getBoolean("upgrades.timer-upgrades-enabled", true);
        Tiers.put(1, "Diamond-I");
        Tiers.put(2, "Emerald-I");
        Tiers.put(3, "Diamond-II");
        Tiers.put(4, "Emerald-II");
        Tiers.put(5, "Diamond-III");
        Tiers.put(6, "Emerald-III");
        Tiers.put(7, "Diamond-IV");
        Tiers.put(8, "Emerald-IV");
        for (int i = 1; i < 9; i++) {
            tier_timer.put(i, SBAHypixelify.getConfigurator().config.getInt("upgrades.time." + Tiers.get(i)));
        }
        Tiers.put(9, "Game End");
        tier_timer.put(9, game.getGameTime());
        multiplier = SBAHypixelify.getConfigurator().config.getDouble("upgrades.multiplier", 0.25);
        runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (game.getStatus() == GameStatus.RUNNING) {
            if (storage.areTrapsEnabled()) {
                for (Player player : game.getConnectedPlayers()) {
                    if (Main.getPlayerGameProfile(player).isSpectator) continue;

                    for (RunningTeam rt : game.getRunningTeams()) {
                        if (!storage.isTrapEnabled(rt) || rt.isPlayerInTeam(player)) continue;

                        if (storage.getTargetBlockLocation(rt).distanceSquared(player.getLocation()) <= arena.radius) {
                            TeamTrapTriggeredEvent event = new TeamTrapTriggeredEvent(player, rt, arena);
                            Bukkit.getServer().getPluginManager().callEvent(event);

                            if(!event.isCancelled()) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 2));
                                storage.setTrap(rt, false);
                                player.sendMessage("§eYou have been blinded by " + rt.getName() + " team!");
                                rt.getConnectedPlayers().forEach(pl -> {
                                    Sounds.playSound(pl, pl.getLocation(),
                                            Main.getConfigurator().config.getString("sounds.on_trap_triggered"),
                                            Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    sendTitle(pl, Messages.trapTriggered_title, Messages.trapTriggered_subtitle,
                                            20, 60, 0);
                                });
                            }
                        }
                    }
                }
            }

            if (storage.arePoolEnabled()) {
                for (RunningTeam rt : game.getRunningTeams()) {
                    if (!storage.isPoolEnabled(rt)) continue;

                    for (Player pl : rt.getConnectedPlayers()) {
                        if (Main.getPlayerGameProfile(pl).isSpectator) continue;
                        if (storage.getTargetBlockLocation(rt).distanceSquared(pl.getLocation()) <= arena.radius) {
                            pl.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 1));
                        }
                    }
                }
            }


            if (!Tiers.get(tier).equals(Tiers.get(9))) {
                if (time == tier_timer.get(tier)) {
                    if (timerUpgrades) {
                        game.getItemSpawners().forEach(itemSpawner -> {
                            if (tier % 2 == 0) {
                                if (itemSpawner.getItemSpawnerType().getMaterial().equals(Material.DIAMOND))
                                    itemSpawner.addToCurrentLevel(multiplier);
                            } else {
                                if (itemSpawner.getItemSpawnerType().getMaterial().equals(Material.EMERALD))
                                    itemSpawner.addToCurrentLevel(multiplier);
                            }
                        });
                        String MatName = tier % 2 == 0 ? "§aEmerald§6" : "§bDiamond§6";

                        game.getConnectedPlayers().forEach(player -> player.sendMessage(Messages.generatorUpgrade
                                .replace("{MatName}", MatName)
                                .replace("{tier}", Tiers.get(tier))));
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
