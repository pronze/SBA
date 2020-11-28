package io.pronze.hypixelify.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import io.pronze.hypixelify.SBAHypixelify;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.GameCreator;

import java.util.ArrayList;
import java.util.List;

public class SBAUtil {

    public static void removeScoreboardObjective(Player player){
        if (SBAHypixelify.isProtocolLib() && player != null && player.isOnline()) {
            final ProtocolManager m = ProtocolLibrary.getProtocolManager();
            try {
                PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
                packet.getIntegers().write(0, 1);
                packet.getStrings().write(0, "bwa-tag");
                m.sendServerPacket(player, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
                packet.getIntegers().write(0, 1);
                packet.getStrings().write(0, "bwa-tab");
                m.sendServerPacket(player, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /*
        Destroys the armorstand entities if somehow the server crashes and the entities remain.
     */
    public static void destroySpawnerArmorStandEntitiesFrom(Game game){
        final World gameWorld = game.getGameWorld();
        if(gameWorld == null){
            return;
        }

        final List<RotatingGenerators> toDestroy = new ArrayList<>();

        for(Entity entity : gameWorld.getEntities()){
            if(entity == null){
                continue;
            }

            if(entity.getType() == EntityType.ARMOR_STAND){
                if(GameCreator.isInArea(entity.getLocation(),game.getPos1(), game.getPos2())){
                    final String customName = entity.getCustomName();

                    if(customName == null){
                        continue;
                    }
                    if(customName.equalsIgnoreCase(RotatingGenerators.entityName)){

                        for(RotatingGenerators generator : RotatingGenerators.cache){
                            if(generator == null){
                                continue;
                            }
                            final ArmorStand armorStand = generator.getArmorStandEntity();
                            if(armorStand == null) continue;


                            if(armorStand.equals(entity)){
                                toDestroy.add(generator);
                            }
                        }

                        entity.remove();
                    }
                }
            }
        }

       toDestroy.forEach(generator->{
           if(generator == null){
               return;
           }

           generator.destroy();
       });

        RotatingGenerators.cache.removeAll(toDestroy);
    }

    public static void destroySpawnerArmorStandEntities(){
        if(BedwarsAPI.getInstance() == null){
            return;
        }

        final List<Game> games = BedwarsAPI.getInstance().getGames();
        if(games != null){
            for(Game game : games){
                if(game != null){
                    SBAUtil.destroySpawnerArmorStandEntitiesFrom(game);
                }
            }
        }
    }

    public static List<Material> parseMaterialFromConfig(String key){
        final List<Material> materialList = new ArrayList<>();

        final List<String> materialNames = SBAHypixelify.getConfigurator().config.getStringList(key);
        try{
            materialNames.forEach(material->{
                if(material == null || material.isEmpty()){
                    return;
                }

                try{
                    final Material mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
                    materialList.add(mat);
                } catch (Exception ignored){

                }

            });
        } catch (Throwable t){
            t.printStackTrace();
        }

        return materialList;
    }
}
