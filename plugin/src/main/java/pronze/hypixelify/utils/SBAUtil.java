package pronze.hypixelify.utils;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.title.Title;
import org.screamingsandals.bedwars.lib.player.PlayerWrapper;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.exception.ExceptionManager;
import pronze.hypixelify.packets.WrapperPlayServerScoreboardObjective;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class SBAUtil {

    //lazy solution but fine for now xdd
    public static List<String> romanNumerals = new ArrayList<>() {
        {
            add("null");
            add("I");
            add("II");
            add("III");
            add("IV");
            add("V");
            add("VI");
            add("VII");
            add("VIII");
            add("IX");
            add("X");
        }
    };

    public static List<Material> parseMaterialFromConfig(String key) {
        final var materialList = new ArrayList<Material>();
        final var materialNames = SBAHypixelify.getConfigurator().getStringList(key);
        materialNames.stream()
                .filter(mat -> mat != null && !mat.isEmpty())
                .forEach(material -> {
                    try {
                        final var mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
                        materialList.add(mat);
                    } catch (Exception ex) {
                        SBAHypixelify.getExceptionManager().handleException(ex);
                    }
                });
        return materialList;
    }

    public static Optional<Location> readLocationFromConfig(String section) {
        try {
            return Optional.of(new Location(Bukkit.getWorld(Objects
                    .requireNonNull(SBAHypixelify.getConfigurator().getString(section + ".world"))),
                    SBAHypixelify.getConfigurator().config.getDouble(section + ".x"),
                    SBAHypixelify.getConfigurator().config.getDouble(section + ".y"),
                    SBAHypixelify.getConfigurator().config.getDouble(section + ".z"),
                    (float) SBAHypixelify.getConfigurator().config.getDouble(section + ".yaw"),
                    (float) SBAHypixelify.getConfigurator().config.getDouble(section + ".pitch")
            ));
        } catch (Exception e) {
            SBAHypixelify.getExceptionManager().handleException(e);
            return Optional.empty();
        }
    }

    public static void cancelTask(BukkitTask task) {
        if (task != null) {
            if (Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId()) || Bukkit.getScheduler().isQueued(task.getTaskId())) {
                task.cancel();
            }
        }
    }

    public static List<String> translateColors(List<String> toTranslate) {
        return toTranslate.stream().map(string -> ChatColor
                .translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static String translateColors(String toTranslate) {
        return ChatColor.translateAlternateColorCodes('&', toTranslate);
    }

    public static Optional<Player> getPlayer(UUID uuid) {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }

    public static void reloadPlugin(@NonNull JavaPlugin plugin) {
        if (!plugin.isEnabled()) {
            return;
        }
        final var pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.disablePlugin(plugin);
        pluginManager.enablePlugin(plugin);
    }

    public static void sendTitle(PlayerWrapper player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        var titleComponent = org.screamingsandals.bedwars.lib.ext.kyori.adventure.title.Title.title(
                AdventureHelper.toComponent(title),
                AdventureHelper.toComponent(subtitle),
                Title.Times.of(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        );

        player.showTitle(titleComponent);
    }
}
