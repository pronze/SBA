package io.github.pronze.sba.specials.runners;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.SBAUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.utils.reflect.Reflect;

public class BridgeEggRunnable extends BukkitRunnable {
    @Getter
    private final BukkitTask task;
    private final Egg egg;
    private final Player thrower;
    private final Game game;
    private final RunningTeam team;
    private final Material wool;
    private final byte legacyData;

    public BridgeEggRunnable(Egg egg, RunningTeam team, Player thrower, Game game) {
        this.egg = egg;
        this.team = team;
        this.thrower = thrower;
        this.game = game;
        var wool = TeamColor.fromApiColor(team.getColor()).getWool();
        this.wool = wool.getType();
        this.legacyData = Main.isLegacy() ? wool.getData().getData() : 0;
        task = runTaskTimer(SBA.getPluginInstance(), 0L, 1L);
    }

    @Override
    public void run() {
        final var eggLocation = egg.getLocation();

        if (!Main.getInstance().isPlayerPlayingAnyGame(thrower) || egg.isDead() || team.isDead() || game.getStatus() != GameStatus.RUNNING) {
            cancel();
            return;
        }

        if (eggLocation.distance(thrower.getLocation()) > 30.0D) {
            cancel();
            return;
        }

        final var b1 = eggLocation.clone().subtract(0.0D, 3.0D, 0.0D).getBlock();
        setBlock(b1);
        final var b2 = eggLocation.clone().subtract(1.0D, 3.0D, 0.0D).getBlock();
        setBlock(b2);
        final var b3 = eggLocation.clone().subtract(0.0D, 3.0D, 1.0D).getBlock();
        setBlock(b3);
    }

    public void setBlock(Block block) {
        if (block.getType() == Material.AIR && game.isLocationInArena(block.getLocation())) {
            block.setType(wool);

            if (Main.isLegacy() && legacyData != 0) {
                Reflect.getMethod(block, "setData", byte.class).invoke(legacyData);
            }

            game.getRegion().addBuiltDuringGame(block.getLocation());
            Sounds.playSound(block.getLocation(), "ENTITY_CHICKEN_EGG", Sounds.ENTITY_CHICKEN_EGG, 1, 1);
        }
    }

    public void cancel() {
        SBAUtil.cancelTask(task);
    }
}
