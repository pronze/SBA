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

import java.util.ArrayList;
import java.util.List;

public class SBAUtil {

    public static void removeScoreboardObjective(Player player) {
        if (SBAHypixelify.isProtocolLib() && player != null && player.isOnline()) {
            try {
                WrapperPlayServerScoreboardObjective obj = new WrapperPlayServerScoreboardObjective();
                obj.setName(ScoreboardUtil.TAB_OBJECTIVE_NAME);
                obj.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);
                obj.sendPacket(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                WrapperPlayServerScoreboardObjective obj = new WrapperPlayServerScoreboardObjective();
                obj.setName(ScoreboardUtil.TAG_OBJECTIVE_NAME);
                obj.setMode(WrapperPlayServerScoreboardObjective.Mode.REMOVE_OBJECTIVE);
                obj.sendPacket(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /*
        Destroys the armorstand entities if somehow the server crashes and the entities remain.
     */
    public static void destroySpawnerArmorStandEntitiesFrom(Game game) {
        final World gameWorld = game.getGameWorld();
        if (gameWorld == null) {
            return;
        }

        final List<RotatingGenerators> toDestroy = new ArrayList<>();


        for (Entity entity : gameWorld.getEntitiesByClass(ArmorStand.class)) {

            if (GameCreator.isInArea(entity.getLocation(), game.getPos1(), game.getPos2())) {
                final String customName = entity.getCustomName();

                if (customName == null) {
                    continue;
                }
                if (customName.equalsIgnoreCase(RotatingGenerators.entityName)) {
                    Chunk chunk = entity.getLocation().getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }

                    for (RotatingGenerators generator : RotatingGenerators.cache) {
                        if (generator == null) {
                            continue;
                        }
                        final ArmorStand armorStand = generator.getArmorStandEntity();
                        if (armorStand == null) continue;


                        if (armorStand.equals(entity)) {
                            toDestroy.add(generator);
                        }
                    }
                }
            }
        }

        toDestroy.forEach(generator -> {
            if (generator == null) {
                return;
            }
            generator.destroy();
        });

        RotatingGenerators.cache.removeAll(toDestroy);
    }

    public static void destroySpawnerArmorStandEntities() {
        if (BedwarsAPI.getInstance() == null) {
            return;
        }

        final List<Game> games = BedwarsAPI.getInstance().getGames();
        if (games != null) {
            for (Game game : games) {
                if (game != null) {
                    SBAUtil.destroySpawnerArmorStandEntitiesFrom(game);
                }
            }
        }
    }

    public static List<Material> parseMaterialFromConfig(String key) {
        final List<Material> materialList = new ArrayList<>();

        final List<String> materialNames = SBAHypixelify.getConfigurator().getStringList(key);
        try {
            materialNames.forEach(material -> {
                if (material == null || material.isEmpty()) {
                    return;
                }

                try {
                    final Material mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
                    materialList.add(mat);
                } catch (Exception ignored) {

                }

            });
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return materialList;
    }
}
