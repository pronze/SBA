package io.github.pronze.sba.service;

import io.github.pronze.sba.config.SBAConfig;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.annotations.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class DateProviderService {
    @Getter
    private final SimpleDateFormat format;

    public DateProviderService(SBAConfig config) {
        format = new SimpleDateFormat(config.node("date", "format").getString("MM/dd/yy"));
    }

    @NotNull
    public String getFormattedDate() {
        return format.format(new Date());
    }
}
