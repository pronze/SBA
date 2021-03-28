package pronze.hypixelify.utils;

import org.screamingsandals.bedwars.lib.ext.bstats.bukkit.Metrics;
import pronze.hypixelify.SBAHypixelify;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;

@AutoInitialize
public class MetricsRegistrationUtil {
    private Metrics metrics;

    public MetricsRegistrationUtil() {
        register();
    }

    public static MetricsRegistrationUtil getInstance() {
        return Core.getObjectFromClass(MetricsRegistrationUtil.class);
    }

    public void register() {
        metrics = new Metrics(SBAHypixelify.getInstance(), 79505);
        metrics.addCustomChart(new Metrics.SimplePie("build", () -> SBAHypixelify.getInstance().isSnapshot() ? "snapshot" : "stable"));
        metrics.addCustomChart(new Metrics.SimplePie("version", () -> SBAHypixelify.getInstance().getVersion()));
    }
}
