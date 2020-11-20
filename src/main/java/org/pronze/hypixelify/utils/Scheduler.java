package org.pronze.hypixelify.utils;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.pronze.hypixelify.SBAHypixelify;

public class Scheduler {


    public static BukkitTask runTaskLater(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(
                SBAHypixelify.getInstance(), runnable, delay
        );
    }

    public static BukkitTask runTask(Runnable runnable) {
        return Bukkit.getScheduler().runTask(SBAHypixelify.getInstance(), runnable);
    }

    public static BukkitTask runTimerTask(Runnable runnable, long delay, long duration) {
        return Bukkit.getScheduler().runTaskTimer(SBAHypixelify.getInstance(), runnable, delay, duration);
    }
}
