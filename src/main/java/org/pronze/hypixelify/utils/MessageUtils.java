package org.pronze.hypixelify.utils;

import org.bukkit.entity.Player;
import org.pronze.hypixelify.SBAHypixelify;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageUtils {

    public static void sendMessage(String key, Player player){
        final List<String> message = SBAHypixelify.getConfigurator().getStringList(key);
        if(player == null || !player.isOnline()){
            return;
        }

        if(message != null){
            message.forEach(msg->{
                player.sendMessage(ShopUtil.translateColors(msg));
            });
        }
    }

    public static void sendMessage(String key, Player player, Map<String, String> replacementMap){
        final List<String> message = SBAHypixelify.getConfigurator().getStringList(key);

        if(replacementMap == null){
            throw new IllegalArgumentException("Map is null, could not send message to player!");
        }

        message.forEach(msg->{

            String replacement = msg;
            if(replacement == null){
                return;
            }

            for(Map.Entry<String, String> set : replacementMap.entrySet()){
                replacement = replacement.replace(set.getKey(), set.getValue());
            }

            player.sendMessage(ShopUtil.translateColors(replacement));
        });
    }
}
