package io.github.pronze.sba.fix;

import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.bukkit.utils.nms.Version;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class ViaVersionFix extends BaseFix {

    private boolean isProblematic=false;
    @Override
    public void detect() {
        boolean isViaEnabled = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        if(isViaEnabled){
            var viaVersion = Bukkit.getPluginManager().getPlugin("ViaVersion");
            isProblematic=!viaVersion.getDescription().getVersion().contains("4.4");
        }
    }

    @Override
    public void fix(SBAConfig cfg) {
        
    }

    @Override
    public void warn() {
        Logger.error("Only ViaVersion 4.4 is currently supported as 4.5 changed the API which broke this plugin");
    }

    @Override
    public boolean IsProblematic() {
        return isProblematic;
    }

    @Override
    public boolean IsCritical() {
        return isProblematic;
    }
    
}
