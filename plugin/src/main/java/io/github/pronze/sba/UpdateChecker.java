package io.github.pronze.sba;


import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

@Service
public class UpdateChecker {

    @OnPostEnable
    public void checkForUpdates() {
        if (SBA.getInstance().isSnapshot()) {
            return;
        }
        new Thread(() -> {
            try (final var inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=99149").openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    promptUpdate(scanner.next());
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }).start();
    }

    private void promptUpdate(@NotNull String version) {
        if (!version.equalsIgnoreCase(SBA.getInstance().getVersion())) {
            Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
            Bukkit.getLogger().info("Download it from here: https://www.spigotmc.org/resources/sba-screaming-bedwars-addon-1-9-4-1-18-1.99149/");
        } else {
            Bukkit.getLogger().info("No updates found");
        }
    }
}