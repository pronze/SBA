package io.github.pronze.sba.config;

import org.screamingsandals.lib.plugin.ServiceManager;

public interface MainConfig extends ConfiguratorAPI {

    static MainConfig getInstance() {
        return ServiceManager.get(MainConfig.class);
    }
}
