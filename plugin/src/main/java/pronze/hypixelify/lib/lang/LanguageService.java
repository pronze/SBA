package pronze.hypixelify.lib.lang;

import lombok.Getter;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurationNode;
import org.screamingsandals.bedwars.lib.ext.configurate.serialize.SerializationException;
import org.screamingsandals.bedwars.lib.ext.configurate.yaml.NodeStyle;
import org.screamingsandals.bedwars.lib.ext.configurate.yaml.YamlConfigurationLoader;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.lang.ILanguageService;
import pronze.hypixelify.api.lang.Message;
import pronze.hypixelify.config.SBAConfig;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AutoInitialize
@Getter
public class LanguageService implements ILanguageService {
    private final String prefix;
    private final String locale;
    private static final List<String> validLocale = List.of(
            "af", "ar", "ca", "cs", "da", "de", "el", "en", "es", "fi", "fr", "he", "hu", "it", "ja", "ko", "nl", "no", "pl",
            "pt", "pt-BR", "ro", "ru", "sr", "sv", "tr", "uk", "vi", "zh", "zh-CN"
    );

    private ConfigurationNode configurationNode;

    public static LanguageService getInstance() {
        return Core.getObjectFromClass(LanguageService.class);
    }

    public LanguageService() {
        prefix = SBAConfig.getInstance().node("prefix").getString();
        locale = SBAConfig.getInstance().node("locale").getString("en");
        loadConfigurationNode();
    }

    @Override
    public Message get(String... arguments) {
        var argumentNode = configurationNode.node((Object[]) arguments);
        try {
            if (argumentNode == null || argumentNode.empty()) {
                throw new UnsupportedOperationException("Could not find key for: " + Arrays.toString(arguments));
            }
            if (argumentNode.isList()) {
                return Message.of(new ArrayList<>(argumentNode.getList(String.class)));
            } else {
                return Message.of(List.of(argumentNode.getString()));
            }
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return Message.of(List.of("TRANSLATION FOR: " + Arrays.toString(arguments) + " NOT FOUND!"));
    }

    private void loadConfigurationNode() {
        if (!validLocale.contains(locale.toLowerCase())) {
            throw new UnsupportedOperationException("Invalid locale provided!");
        }

        try {
            var pathStr = SBAHypixelify.getInstance().getDataFolder().getAbsolutePath();
            pathStr = pathStr + "/languages/" + "language_" + locale + ".yml";

            var loader = YamlConfigurationLoader
                    .builder()
                    .path(Paths.get(pathStr))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();
            configurationNode = loader.load();
        } catch (Exception ex) {
            SBAHypixelify.getExceptionManager().handleException(ex);
        }
    }
}
