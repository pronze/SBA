package io.github.pronze.sba.utils.citizens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import net.citizensnpcs.api.trait.Trait;
import org.screamingsandals.lib.spectator.Component;

public class ReturnToStoreTrait extends Trait {

    Location loc;

    public ReturnToStoreTrait() {
        super("ReturnToStoreTrait");
    }

    @Override
    public void run() {
        var npc = getNPC();
        var entity = npc.getEntity();
        if (entity!=null) {
            var newLoc = getNPC().getEntity().getLocation().add(0.0D, 1.5D, 0.0D);
            if (!loc.equals(newLoc) && loc.distance(newLoc)>1) {
                npc.getNavigator().setTarget(loc);
            }
        }
    }

    // Run code when the NPC is despawned. This is called before the entity actually
    // despawns so npc.getEntity() is still valid.
    @Override
    public void onDespawn() {
     
    }

    // Run code when the NPC is despawned. This is called before the entity actually
    // despawns so npc.getEntity() is still valid.
    @Override
    public void onRemove() {
      
    }

    List<Component> lines = new ArrayList<>();

    // Run code when the NPC is spawned. Note that npc.getEntity() will be null
    // until this method is called.
    // This is called AFTER onAttach and AFTER Load when the server is started.
    @Override
    public void onSpawn() {
        loc = getNPC().getEntity().getLocation();
    }
}
