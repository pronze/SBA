package io.github.pronze.sba.utils.citizens;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.hologram.HologramManager;

import net.citizensnpcs.api.trait.Trait;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.spectator.Component;

public class HologramTrait extends Trait {

    Hologram holo;
    Location loc;

    public HologramTrait() {
        super("SBAHologramTrait");
    }

    @Override
    public void run() {
        var npc = getNPC();
        var entity = npc.getEntity();
        if (holo != null && entity!=null) {
            var newLoc = getNPC().getEntity().getLocation().add(0.0D, 1.5D, 0.0D);
            if (!loc.equals(newLoc) && loc.distance(newLoc)>1) {
                loc = newLoc;
                holo.hide();
                holo.destroy();

                holo = HologramManager
                        .hologram(Objects.requireNonNull(org.screamingsandals.lib.world.Location.fromPlatform(newLoc)));
                holo.setLines(lines);

                getNPC().getEntity().getLocation().getWorld().getPlayers()
                        .forEach(player -> holo.addViewer(Players.wrapPlayer(player)));
                holo.spawn();
            }
        }
    }

    // Run code when the NPC is despawned. This is called before the entity actually
    // despawns so npc.getEntity() is still valid.
    @Override
    public void onDespawn() {
        if (holo != null) {
            holo.hide();
            holo.destroy();
        }
        holo = null;
    }

    // Run code when the NPC is despawned. This is called before the entity actually
    // despawns so npc.getEntity() is still valid.
    @Override
    public void onRemove() {
        if (holo != null) {
            holo.hide();
            holo.destroy();
        }
        holo = null;
    }

    List<Component> lines = new ArrayList<>();

    // Run code when the NPC is spawned. Note that npc.getEntity() will be null
    // until this method is called.
    // This is called AFTER onAttach and AFTER Load when the server is started.
    @Override
    public void onSpawn() {
        onDespawn();
        
        holo = HologramManager
                .hologram(Objects.requireNonNull(org.screamingsandals.lib.world.Location.fromPlatform(loc = getNPC().getEntity().getLocation().add(0.0D, 1.5D, 0.0D))));
        holo.setLines(lines);

        getNPC().getEntity().getLocation().getWorld().getPlayers()
                .forEach(player -> holo.addViewer(Players.wrapPlayer(player)));

        holo.spawn();
    }

    public void setLines(List<Component> lines) {
        this.lines.clear();
        this.lines.addAll(lines);

        if (holo != null)
            holo.setLines(this.lines);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (holo != null) {
            holo.removeViewer(Players.wrapPlayer(event.getPlayer()));
            var player = event.getPlayer();
            if (holo.location().getWorld().getName().equals(player.getWorld().getName())) {
                holo.addViewer(Players.wrapPlayer(player));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (holo != null) {
            var player = event.getPlayer();
            if (holo.location().getWorld().getName().equals(player.getWorld().getName())) {
                holo.addViewer(Players.wrapPlayer(player));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (holo != null) {
            holo.removeViewer(Players.wrapPlayer(event.getPlayer()));
            var player = event.getPlayer();
            if (holo.location().getWorld().getName().equals(player.getWorld().getName())) {
                holo.addViewer(Players.wrapPlayer(player));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (holo != null)
            holo.removeViewer(Players.wrapPlayer(event.getPlayer()));
    }

}
