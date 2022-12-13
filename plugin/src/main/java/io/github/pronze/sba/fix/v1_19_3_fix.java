package io.github.pronze.sba.fix;

import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.bukkit.utils.nms.Version;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class v1_19_3_fix extends BaseFix {

    private boolean isProblematic;
    @Override
    public void detect() {
        //v1_19_R2
        String a = Bukkit.getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        
        
        isProblematic= Version.isVersion(1, 19, 3);
    }

    @Override
    public void fix(SBAConfig cfg) {
        
    }

    @Override
    public void warn() {
        Logger.error("1.19.3 isn't a supported server by SBA");
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
