package io.pronze.hypixelify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {


    public static void run(JavaPlugin plugin, int resourceId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    String version = scanner.next();
                    if (version != null) {
                        if (!version.equalsIgnoreCase(SBAHypixelify.getVersion())) {
                            Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE.");
                        }
                    }
                }
            } catch (IOException exception) {
                Bukkit.getLogger().info("Cannot look for updates: " + exception.getMessage());
            }
        });
    }
}