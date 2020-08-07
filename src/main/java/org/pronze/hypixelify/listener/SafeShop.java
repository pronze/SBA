package org.pronze.hypixelify.listener;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;

public class SafeShop {

    public static boolean canInstantiate(){
        if(Bukkit.getServer().getPluginManager().getPlugin("Citizens") == null)
            return false;

        try {
            final Class<?> NPCClass = Class.forName("net.citizensnpcs.api.npc.NPC");
            if(!CitizensAPI.getPlugin().isEnabled())
                return false;
        } catch (Exception e){
            return false;
        }
         return true;
    }

}
