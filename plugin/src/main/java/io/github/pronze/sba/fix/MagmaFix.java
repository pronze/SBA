package io.github.pronze.sba.fix;

import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class MagmaFix extends BaseFix {

    private boolean isProblematic;
    @Override
    public void detect() {
        isProblematic= (Reflect.has("org.magmafoundation.magma.MagmaStart"));
    }

    @Override
    public void fix(SBAConfig cfg) {
        cfg.set("lobby-scoreboard.enabled", false);
        cfg.set("game-scoreboard.enabled", false);
    }

    @Override
    public void warn() {
        Logger.warn("Magma isn't supported by the scoreboard, it has been disabled");
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
