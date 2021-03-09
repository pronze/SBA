package pronze.hypixelify.utils;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.packets.WrapperPlayServerScoreboardObjective;

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

    public static void removeScoreboardObjective(Player player) {
        if (SBAHypixelify.isProtocolLib() && !Main.isLegacy()) {
            try {
                final var obj = new WrapperPlayServerScoreboardObjective();
                obj.setName(ScoreboardUtil.TAB_OBJECTIVE_NAME);
                obj.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);
                obj.sendPacket(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                final var obj = new WrapperPlayServerScoreboardObjective();
                obj.setName(ScoreboardUtil.TAG_OBJECTIVE_NAME);
                obj.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);
                obj.sendPacket(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static List<Material> parseMaterialFromConfig(String key) {
        final var materialList = new ArrayList<Material>();
        final var materialNames = SBAHypixelify.getConfigurator().getStringList(key);
        materialNames.stream()
                .filter(mat -> mat != null && !mat.isEmpty())
                .forEach(material -> {
                    try {
                        final var mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
                        materialList.add(mat);
                    } catch (Exception ignored) {}
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
        } catch (Throwable t) {
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
}
