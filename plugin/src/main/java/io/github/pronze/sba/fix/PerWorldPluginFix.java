package io.github.pronze.sba.fix;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class PerWorldPluginFix extends BaseFix {

    private boolean isProblematic = false;
    @Override
    public void detect() {
        Plugin PerWorldPlugin = Bukkit.getPluginManager().getPlugin("PerWorldPlugins");
        if (PerWorldPlugin!=null)
        {
            String version = PerWorldPlugin.getDescription().getVersion();
            isProblematic = 
                version.startsWith("1.0") ||
                version.startsWith("1.1.0") ||
                version.startsWith("1.1.1") ||
                version.startsWith("1.1.2") ||
                version.startsWith("1.1.3") ;
        }
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
        Logger.error("SBA FATAL ERROR::PerWorldPlugins version 1.1.3 or lower breaks custom plugin events required by Bedwars and SBA");
        Logger.error("SBA Will shutdown due to incompatible plugin(s), You can update PerWorldPlugins to 1.1.4 or higher to fix it");
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
