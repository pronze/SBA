package io.github.pronze.sba;

import io.github.pronze.sba.utils.Logger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SBALanguageServiceTest {
    private ConfigurationNode node;

    @SneakyThrows
    @Test
    public void test() {
        Logger.mockMode();

        File tempFile = File.createTempFile("test", "language");
        try(var input = SBALanguageServiceTest.class.getResourceAsStream("/languages/language_en.yml")) {
            assert input != null;
            try (var output = new FileOutputStream(tempFile, false)) {
                input.transferTo(output);
            }
        }

        node = YamlConfigurationLoader
                .builder()
                .file(tempFile)
                .nodeStyle(NodeStyle.BLOCK)
                .build()
                .load();

        Arrays.stream(LangKeys.class.getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        Logger.trace(get((String[])field.get(null)).toStringList().toString());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    @SneakyThrows
    public Message get(String... arguments) {
        ConfigurationNode argumentNode = node.node((Object[]) arguments);
        if (argumentNode == null || argumentNode.empty()) {
            throw new UnsupportedOperationException("Could not find key for: " + Arrays.toString(arguments));
        }
        if (argumentNode.isList()) {
            return Message.of(argumentNode.getList(String.class));
        } else {
            return Message.of(List.of(Objects.requireNonNull(argumentNode.getString())));
        }
    }
}
