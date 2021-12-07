package io.github.pronze.sba;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

@RequiredArgsConstructor
@Service
public class UpdateChecker {
    private final SBA pluginContainer;
    private final LoggerWrapper logger;

    @OnEnable
    public void checkForUpdates() {
        if (pluginContainer.isSnapshot()) {
            // TODO: implement snapshot update checker
            logger.trace("Skipping update checker for snapshots....");
            return;
        }

        new Thread(() -> {
            try (final var inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=79505").openStream(); var scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    promptUpdate(scanner.next());
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }).start();
    }

    protected void promptUpdate(@NotNull String version) {
        if (!version.equalsIgnoreCase(pluginContainer.getPluginDescription().getVersion())) {
            Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
            Bukkit.getLogger().info("Download it from here: https://www.spigotmc.org/resources/addon-sba-for-screaming-bedwars-1-9-4-1-17.79505/");
        } else {
            logger.info("No updates found");
        }
    }
}