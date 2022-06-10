package io.github.pronze.sba.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import io.github.pronze.sba.SBA;
import me.frep.vulcan.api.event.VulcanFlagEvent;

@Service
public class AntiCheatIntegration implements Listener {
    ArrayList<Listener> integrations = new ArrayList<>();

    public static AntiCheatIntegration getInstance() {
        return ServiceManager.get(AntiCheatIntegration.class);
    }

    Set<UUID> currentlyJumping = new HashSet<>();

    public void beginTntJump(Player p) {
        //if (!currentlyJumping.contains(p.getUniqueId()))
        //    currentlyJumping.add(p.getUniqueId());
    }
    public void endTntJump(Player p)
    {
        //currentlyJumping.remove(p.getUniqueId());
    }

    public AntiCheatIntegration() {

    }

    @OnPostEnable
    public void onPostEnabled() {
        //if (Bukkit.getPluginManager().isPluginEnabled("Vulcan")) {
        //    integrations.add(new VulcanIntegration());
        //}

        //integrations.forEach(SBA.getInstance()::registerListener);
    }

    private class VulcanIntegration implements Listener {
        public VulcanIntegration() {

        }

        @EventHandler
        public void onVulcanDetection(VulcanFlagEvent event) {
            //if (currentlyJumping.contains(event.getPlayer().getUniqueId()))
                event.setCancelled(true);
        }
    }
}
