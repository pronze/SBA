package io.github.pronze.sba.fix;

import io.github.pronze.sba.config.SBAConfig;

public abstract class BaseFix {
    public abstract void detect();
    public abstract void fix(SBAConfig cfg);
    public abstract void warn();
    public abstract boolean IsProblematic();
}
