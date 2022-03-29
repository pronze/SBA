package io.github.pronze.sba.fix;

import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class MohistFix extends BaseFix {

    private boolean isProblematic;
    @Override
    public void detect() {
        isProblematic= (Reflect.has("com.mohistmc.MohistMC"));
    }

    @Override
    public void fix(SBAConfig cfg) {
        if(isProblematic)
            Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
    }

    @Override
    public void warn() {
        Logger.error("MohistMC isn't a supported server by BedWars or SBA, it introduce bugs in the Bukkit API making it unfit for BedWars or SBA");
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
