package pronze.hypixelify;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {

    public static void run(SBAHypixelify plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.isSnapshot()) {
                //TODO: snapshot update checker
                return;
            }
            try (final var inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=79505").openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    final var version = scanner.next();
                    if (version != null) {
                        if (!version.equalsIgnoreCase(SBAHypixelify.getInstance().getVersion())) {
                            Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
                            Bukkit.getLogger().info("Download it from here: https://www.spigotmc.org/resources/addon-sbahypixelify-for-screaming-bedwars-1-9-4-1-16-4.79505/");
                        } else {
                            Bukkit.getLogger().info("No updates found");
                        }
                    }
                }
            } catch (IOException exception) {
                Bukkit.getLogger().info("Cannot look for updates: " + exception.getMessage());
            }
        });
    }
}