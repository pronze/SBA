package io.github.pronze.sba.fix;

import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.bukkit.utils.nms.Version;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class CitizensFix extends BaseFix {

    private boolean isProblematic=false;
    private boolean isCritical=false;
    @Override
    public void detect() {
        boolean isViaEnabled = Bukkit.getPluginManager().isPluginEnabled("Citizens");
        if(isViaEnabled){
            var viaVersion = Bukkit.getPluginManager().getPlugin("Citizens");
            isCritical=viaVersion.getDescription().getVersion().contains("2.0.2");
            isProblematic=!viaVersion.getDescription().getVersion().contains("2.0.30");
        }
    }
    public boolean canEnable()
    {
        return Bukkit.getServer().getPluginManager().getPlugin("Citizens") != null
        && Bukkit.getServer().getPluginManager().getPlugin("Citizens").isEnabled() &&
        !isCritical;
    }
    @Override
    public void fix(SBAConfig cfg) {
        if(isCritical)
        {
            SBAConfig.getInstance().ai().disable();
        }
    }

    @Override
    public void warn() {
        Logger.error("Only Citizens 2.0.30 is currently supported as Citizens API change frequently. 2.0.31+ might or might not work.");
        if(isCritical)
        {
            Logger.error("SBA Will totally disable AI");
        }
    }

    @Override
    public boolean IsProblematic() {
        return isProblematic;
    }

    @Override
    public boolean IsCritical() {
        return false;
    }
    
}
