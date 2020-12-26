package io.pronze.hypixelify.utils;

import io.pronze.hypixelify.SBAHypixelify;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageUtils {

    public static void sendMessage(String key, Player player){
        final var message = SBAHypixelify.getConfigurator().getStringList(key);
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
        final var message = SBAHypixelify.getConfigurator().getStringList(key);

        if(replacementMap == null){
            throw new IllegalArgumentException("Map is null, could not send message to player!");
        }

        message.stream()
                .filter(Objects::nonNull)
                .forEach(msg->{
            for(var set : replacementMap.entrySet()){
                msg = msg.replace(set.getKey(), set.getValue());
            }

            player.sendMessage(ShopUtil.translateColors(msg));
        });
    }
}
