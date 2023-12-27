package io.github.pronze.sba.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.utils.Logger;
import lombok.NoArgsConstructor;
import me.frep.vulcan.api.event.VulcanFlagEvent;
import me.frep.vulcan.api.event.VulcanGhostBlockEvent;

@Service
@NoArgsConstructor
public class AntiCheatIntegration implements Listener {
    ArrayList<Listener> integrations = new ArrayList<>();

    public static AntiCheatIntegration getInstance() {
        return ServiceManager.get(AntiCheatIntegration.class);
    }

    Set<UUID> currentlyJumping = new HashSet<>();
    Map<UUID, BukkitTask> jumpingCooldown = new HashMap<>();

    public void beginTntJump(Player p) {
        if (!currentlyJumping.contains(p.getUniqueId())) {
            if (jumpingCooldown.containsKey(p.getUniqueId())) {
                try {
                    jumpingCooldown.get(p.getUniqueId()).cancel();
                } catch (IllegalStateException ex) {

                }
                jumpingCooldown.remove(p.getUniqueId());
            }
            currentlyJumping.add(p.getUniqueId());
            Logger.trace("Starting tnt jump for {}", p);
        }
    }

    public void tntJumpLanding(Player p) {
        var id = p.getUniqueId();
        currentlyJumping.remove(id);
        Logger.trace("Landed tnt jump for {}", p);
        jumpingCooldown.put(id,
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        jumpingCooldown.remove(id);
                        Logger.trace("Ending tnt jump for {}", p);
                    }

                    @Override
                    public void cancel() {
                        super.cancel();
                        Logger.trace("Cancelling delay tnt jump for {}", p);
                    }
                }.runTaskLater(SBA.getPluginInstance(), 120));
    }

    @OnPostEnable
    public void onPostEnabled() {
        if(SBA.isBroken())return;
        if (Bukkit.getPluginManager().isPluginEnabled("Vulcan")) {
            integrations.add(new VulcanIntegration());
        }

        integrations.forEach(SBA.getInstance()::registerListener);
    }

    @NoArgsConstructor
    private class VulcanIntegration implements Listener {
        @EventHandler
        public void onGhostBlock(VulcanGhostBlockEvent event) {
            var id = event.getPlayer().getUniqueId();
            if (currentlyJumping.contains(id) || jumpingCooldown.containsKey(id)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onVulcanDetection(VulcanFlagEvent event) {
            Logger.trace("Vulcan detection, [{},{}]", event.getCheck().getDisplayName(),
                    event.getCheck().getDisplayType());
            var id = event.getPlayer().getUniqueId();
     
            if (currentlyJumping.contains(id) || jumpingCooldown.containsKey(id) || event.getCheck().getDisplayName().equalsIgnoreCase("Jump")) {
                Logger.trace("Vulcan detection cancelled, {},{},{}", event.getInfo(), event.getCheck().getComplexType(),
                        event.getCheck().getName());
                event.setCancelled(true);
            }
        }
    }
}
