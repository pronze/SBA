package org.pronze.hypixelify.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.SBAHypixelify;

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
