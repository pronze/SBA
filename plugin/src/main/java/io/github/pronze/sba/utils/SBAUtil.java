package io.github.pronze.sba.utils;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.tasker.task.TaskState;
import org.screamingsandals.lib.tasker.task.TaskerTask;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.bukkit.entity.Player;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
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

    public static void cancelTask(@Nullable BukkitTask task) {
        if (task != null) {
            if (Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId()) || Bukkit.getScheduler().isQueued(task.getTaskId())) {
                task.cancel();
            }
        }
    }

    public static void cancelTask(@Nullable TaskerTask task) {
        if (task != null) {
            if (task.getState() != TaskState.CANCELLED && task.getState() != TaskState.FINISHED) {
                try {
                    task.cancel();
                } catch (Throwable t) {
                    Logger.error("Exception cancelling task of id: {}, {}" + task.getId(), t);
                }
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

    public static void disablePlugin(@NotNull JavaPlugin plugin) {
        // thank you Misat for this :)
        try {
            String message = String.format("Disabling %s", plugin.getDescription().getFullName());
            plugin.getLogger().info(message);
            Bukkit.getPluginManager().callEvent(new PluginDisableEvent(plugin));
            Reflect.setField(plugin, "isEnabled", false);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getScheduler().cancelTasks(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getServicesManager().unregisterAll(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering services for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            HandlerList.unregisterAll(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering events for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin);
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering plugin channels for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            for (World world : Bukkit.getWorlds()) {
                world.removePluginChunkTickets(plugin);
            }
        } catch (Throwable ex) {
            // older versions don't even have chunk tickets
            //Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while removing chunk tickets for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    public static void reloadPlugin(@NonNull JavaPlugin plugin) {
        disablePlugin(plugin);
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
        if (plugin == SBA.getPluginInstance()) {
            SBAConfig.getInstance().forceReload();
        }
        Bukkit.getLogger().info("Plugin reloaded! Keep in mind that restarting the server is safer!");
    }

    public static String capitalizeFirstLetter(@NotNull String toCap) {
        return toCap.substring(0, 1).toUpperCase() + toCap.substring(1).toLowerCase();
    }

    private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections)
            return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();

        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }
}
