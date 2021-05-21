package pronze.hypixelify.utils;

import lombok.NonNull;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import pronze.hypixelify.config.SBAConfig;
import org.bukkit.entity.Player;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class SBAUtil {
    public static List<Material> parseMaterialFromConfig(String key) {
        final var materialList = new ArrayList<Material>();
        final var materialNames = SBAConfig.getInstance().getStringList(key);
        materialNames.stream()
                .filter(mat -> mat != null && !mat.isEmpty())
                .forEach(material -> {
                    try {
                        final var mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
                        materialList.add(mat);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        return materialList;
    }

    public static Optional<Location> readLocationFromConfig(String section) {
        try {
            return Optional.of(new Location(Bukkit.getWorld(Objects
                    .requireNonNull(SBAConfig.getInstance().getString(section + ".world"))),
                    SBAConfig.getInstance().getDouble(section + ".x", 0),
                    SBAConfig.getInstance().getDouble(section + ".y", 0),
                    SBAConfig.getInstance().getDouble(section + ".z", 0),
                    (float) SBAConfig.getInstance().getDouble(section + ".yaw", 0),
                    (float) SBAConfig.getInstance().getDouble(section + ".pitch", 0)
            ));
        } catch (Exception e) {
            e.printStackTrace();
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
        var titleComponent = net.kyori.adventure.title.Title.title(
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
