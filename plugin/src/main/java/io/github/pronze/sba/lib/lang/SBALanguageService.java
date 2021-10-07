package io.github.pronze.sba.lib.lang;

import com.google.gson.Gson;
import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.utils.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang.LocaleUtils;
import org.screamingsandals.lib.lang.Lang;
import org.screamingsandals.lib.lang.LangService;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;
import org.screamingsandals.lib.utils.annotations.parameters.DataFolder;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import io.github.pronze.sba.config.SBAConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service(dependsOn = {
        SBAConfig.class
})
@RequiredArgsConstructor
@Getter
public class SBALanguageService extends LangService {
    public static final Pattern LANGUAGE_PATTERN = Pattern.compile("[a-z]{2}-[A-Z]{2}");

    {
        Lang.initDefault(this);
    }

    private final SBAConfig mainConfig;
    @DataFolder("languages")
    private final Path languagesFolder;
    @Getter
    private LanguageDefinition internalLanguageDefinition;

    @SneakyThrows
    @OnEnable
    public void onEnable() {
        Locale locale;
        try {
            locale = LocaleUtils.toLocale(mainConfig.node("locale").getString("en_US").replace("-", "_"));
        } catch (IllegalArgumentException ex) {
            Logger.error("invalid locale specified in config, fallback to en_US!");
            locale = Locale.US;
        }
        final var finalLocale = locale;

        var prefix = AddonAPI
                .getInstance()
                .getConfigurator()
                .getString("prefix", "[SBA]");

        Lang.setDefaultPrefix(AdventureHelper.toComponent(prefix));

        internalLanguageDefinition = new Gson().fromJson(new InputStreamReader(SBALanguageService.class.getResourceAsStream("/language_definition.json")), LanguageDefinition.class);
        if (internalLanguageDefinition == null) {
            Logger.error("Can't load default Language Definition for Screaming BedWars!");
            return;
        }

        var languages = internalLanguageDefinition
                .getLanguages()
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        return Map.entry(Locale.forLanguageTag(entry.getKey()), entry.getValue());
                    } catch (IllegalArgumentException ex) {
                        Logger.error("Invalid language definition: {}", ex.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        var us = languages
                .stream()
                .filter(entry -> entry.getKey().equals(Locale.US))
                .findFirst()
                .map(entry -> {
                    try {
                        return LayeredTranslationContainer.of(
                                GsonConfigurationLoader
                                        .builder()
                                        .source(() -> new BufferedReader(new InputStreamReader(SBALanguageService.class.getResourceAsStream("/" + entry.getValue()))))
                                        .build()
                                        .load()
                        );
                    } catch (ConfigurateException e) {
                        Logger.error("Can't load base language file en_US!");
                        e.printStackTrace();
                        return null;
                    }
                })
                .orElseGet(() -> LayeredTranslationContainer.of(BasicConfigurationNode.root()));

        if (us.isEmpty()) {
            Logger.warn("Language definitions don't contain en_US file!");
        }

        if (!finalLocale.equals(Locale.US)) {
            fallbackContainer = languages
                    .stream()
                    .filter(entry -> entry.getKey().equals(finalLocale))
                    .findFirst()
                    .or(() -> languages
                            .stream()
                            .filter(entry -> entry.getKey().getLanguage().equals(finalLocale.getLanguage()))
                            .findFirst()
                    )
                    .map(entry -> {
                        try {
                            return LayeredTranslationContainer.of(
                                    us,
                                    GsonConfigurationLoader
                                            .builder()
                                            .source(() -> new BufferedReader(new InputStreamReader(SBALanguageService.class.getResourceAsStream("/" + entry.getValue()))))
                                            .build()
                                            .load(),
                                    BasicConfigurationNode.root(),
                                    BasicConfigurationNode.root()
                            );
                        } catch (ConfigurateException e) {
                            Logger.error("Can't load language file!");
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .orElse(us);
        } else {
            fallbackContainer = us;
        }

        if (Files.exists(languagesFolder)) {
            try (var stream = Files.walk(languagesFolder.toAbsolutePath())) {
                stream.filter(Files::isRegularFile)
                        .forEach(file -> {
                            var name = file.getFileName().toString();
                            if (Files.exists(file) && Files.isRegularFile(file) && name.toLowerCase().endsWith(".json")) {
                                var matcher = LANGUAGE_PATTERN.matcher(name);
                                if (matcher.find()) {
                                    try {
                                        var locale1 = Locale.forLanguageTag(matcher.group());

                                        if (finalLocale.equals(locale1)) {
                                            ((LayeredTranslationContainer) fallbackContainer)
                                                    .setCustomNode(GsonConfigurationLoader
                                                            .builder()
                                                            .path(file)
                                                            .build()
                                                            .load()
                                                    );
                                        } else if (Locale.US.equals(locale1)) {
                                            us.setCustomNode(GsonConfigurationLoader
                                                    .builder()
                                                    .path(file)
                                                    .build()
                                                    .load()
                                            );
                                        }
                                    } catch (IllegalArgumentException | ConfigurateException ex) {
                                        Logger.warn("Invalid language file in languages directory: " + name);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Files.createDirectory(languagesFolder);
        }
    }
}
