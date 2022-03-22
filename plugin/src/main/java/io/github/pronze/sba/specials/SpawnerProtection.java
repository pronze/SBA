package io.github.pronze.sba.specials;

import org.bukkit.Location;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;

@Service(dependsOn = SBAConfig.class)
public class SpawnerProtection {

    private static SpawnerProtection instance;
    private double spawnerProtectionSize = 0;
    private double teamProtectionSize = 0;
    private double storeProtectionSize = 0;
    @OnPostEnable
    public void registerProtection() {
        instance = this;

        spawnerProtectionSize=SBAConfig.getInstance().getDouble("automatic-protection.spawner-diameter", 0);
        teamProtectionSize=SBAConfig.getInstance().getDouble("automatic-protection.team-spawn-diameter", 0);
        storeProtectionSize=SBAConfig.getInstance().getDouble("automatic-protection.store-diameter", 0);
    }

    
    public boolean isProtected(Game g, Location l)
    {
        return isProtectedSpawner(g,l) || isProtectedTeam(g,l) || isProtectedStore(g,l);
    }
    public boolean isProtectedSpawner(Game g, Location l)
    {
        if (spawnerProtectionSize == 0)
            return false;
        else {
            double criteria = spawnerProtectionSize / 2;
            for (var spawner : g.getItemSpawners()) {
                var spawnerBlock = spawner.getLocation().getBlock().getLocation();
                double dx = Math.abs(spawnerBlock.getX() - l.getX());
                double dy = Math.abs(spawnerBlock.getY() - l.getY());
                double dz = Math.abs(spawnerBlock.getZ() - l.getZ());

                if (dx < criteria && dy < criteria && dz < criteria) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean isProtectedTeam(Game g, Location l)
    {
        if (teamProtectionSize == 0)
            return false;
        else {
            double criteria = teamProtectionSize / 2;
            for (var team : g.getAvailableTeams()) {
                var spawnerBlock = team.getTeamSpawn().getBlock().getLocation();
                double dx = Math.abs(spawnerBlock.getX() - l.getX());
                double dy = Math.abs(spawnerBlock.getY() - l.getY());
                double dz = Math.abs(spawnerBlock.getZ() - l.getZ());

                if (dx < criteria && dy < criteria && dz < criteria) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean isProtectedStore(Game g, Location l)
    {
        if(storeProtectionSize==0)
            return false;
        else
        {
            double criteria = storeProtectionSize / 2;
            for( var store : g.getGameStores())
            {
                var spawnerBlock = store.getStoreLocation().getBlock().getLocation();
                double dx = Math.abs(spawnerBlock.getX() - l.getX());
                double dy = Math.abs(spawnerBlock.getY() - l.getY());
                double dz = Math.abs(spawnerBlock.getZ() - l.getZ());

                if(dx < criteria && dy < criteria && dz < criteria)
                {
                    return true;
                }
            }
        }
        return false;
    }


    public static SpawnerProtection getInstance() {
        return instance;
    }
}
