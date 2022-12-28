package io.github.pronze.sba.fix;

import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class PerWorldPluginFix extends BaseFix {

    private boolean isProblematic;
    @Override
    public void detect() {
        isProblematic= Bukkit.getPluginManager().isPluginEnabled("PerWorldPlugins");
    }

    @Override
    public void fix(SBAConfig cfg) {
        if(isProblematic)
        {
            Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    @Override
    public void warn() {
        Logger.error("SBA FATAL ERROR::PerWorldPlugin breaks custom plugin events required by Bedwars and SBA");
        Logger.error("SBA Will shutdown due to incompatible plugin(s)");
    }

    @Override
    public boolean IsProblematic() {
        return isProblematic;
    }

    @Override
    public boolean IsCritical() {
        return IsProblematic();
    }
    
}
