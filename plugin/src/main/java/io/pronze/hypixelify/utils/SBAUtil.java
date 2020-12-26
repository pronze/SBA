package io.pronze.hypixelify.utils;

import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.game.RotatingGenerators;
import io.pronze.hypixelify.packets.WrapperPlayServerScoreboardObjective;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.GameCreator;
import org.screamingsandals.lib.paperlib.PaperLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        if (SBAHypixelify.isProtocolLib()) {
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
        if (gameWorld == null) {
            return;
        }

        final var toDestroy = new ArrayList<RotatingGenerators>();

        gameWorld.getEntitiesByClass(ArmorStand.class)
                .forEach(entity-> {
                    if (GameCreator.isInArea(entity.getLocation(), game.getPos1(), game.getPos2())) {
                        final var customName = entity.getCustomName();
                        if (customName == null) {
                            return;
                        }
                        if (customName.equalsIgnoreCase(RotatingGenerators.entityName)) {
                            RotatingGenerators
                                    .cache
                                    .stream()
                                    .filter(gen-> gen != null && gen.getArmorStandEntity() != null)
                                    .forEach(generator-> {
                                if (generator.getArmorStandEntity().equals(entity)) {
                                    toDestroy.add(generator);
                                    generator.setArmorStand(null);
                                }
                            });

                            PaperLib.getChunkAtAsync(entity.getLocation())
                                    .thenAccept(chunk-> entity.remove());
                        }
                    }
                });

        toDestroy.forEach(RotatingGenerators::destroy);
        RotatingGenerators.cache.removeAll(toDestroy);
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
        try {
            materialNames.stream()
                    .filter(mat-> mat != null && !mat.isEmpty())
                    .forEach(material -> {
                try {
                    final var mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
                    materialList.add(mat);
                } catch (Exception ignored) {}

            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return materialList;
    }
}
