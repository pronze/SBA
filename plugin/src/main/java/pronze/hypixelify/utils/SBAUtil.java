package pronze.hypixelify.utils;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.GameCreator;
import org.screamingsandals.bedwars.lib.paperlib.PaperLib;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.game.RotatingGenerators;
import pronze.hypixelify.packets.WrapperPlayServerScoreboardObjective;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public static void destroySpawnerArmorStandEntitiesFrom(Game game) {
        final var gameWorld = game.getGameWorld();
        final var toDestroy = new ArrayList<RotatingGenerators>();

        gameWorld.getEntitiesByClass(ArmorStand.class)
                .stream()
                .filter(entity -> GameCreator.isInArea(entity.getLocation(), game.getPos1(), game.getPos2()))
                .filter(entity -> RotatingGenerators.entityName.equalsIgnoreCase(entity.getCustomName()))
                .forEach(entity -> {
                    RotatingGenerators.cache.stream()
                            .filter(gen -> gen != null && gen.getArmorStandEntity() != null)
                            .forEach(generator -> {
                                if (generator.getArmorStandEntity().equals(entity)) {
                                    toDestroy.add(generator);
                                    generator.setArmorStand(null);
                                }
                            });

                    PaperLib.getChunkAtAsync(entity.getLocation())
                            .thenAccept(chunk -> entity.remove());
                });

        RotatingGenerators.destroy(toDestroy);
    }

    public static void destroySpawnerArmorStandEntities() {
        if (BedwarsAPI.getInstance() == null) {
            return;
        }

        final var games = BedwarsAPI.getInstance().getGames();
        games.stream()
                .filter(Objects::nonNull)
                .forEach(SBAUtil::destroySpawnerArmorStandEntitiesFrom);
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
                    } catch (Exception ignored) {
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

    public static void reloadPlugin(@NonNull JavaPlugin plugin) {
        if (!plugin.isEnabled()) {
            return;
        }
        final var pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.disablePlugin(plugin);
        pluginManager.enablePlugin(plugin);
    }
}
