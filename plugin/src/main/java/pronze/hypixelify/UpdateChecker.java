package pronze.hypixelify;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {

    public static void run(SBAHypixelify plugin) {
        if (plugin.isSnapshot()) return;
        new Thread(() -> {
            try (final var inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=79505").openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    promptUpdate(scanner.next());
                }
            } catch (IOException exception) {
                Bukkit.getLogger().info("Cannot look for updates: " + exception.getMessage());
            }
        }).start();
    }

    private static void promptUpdate(String version) {
        if (version != null) {
            if (!version.equalsIgnoreCase(SBAHypixelify.getInstance().getVersion())) {
                Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
                Bukkit.getLogger().info("Download it from here: https://www.spigotmc.org/resources/addon-sbahypixelify-for-screaming-bedwars-1-9-4-1-16-4.79505/");
            } else {
                Bukkit.getLogger().info("No updates found");
            }
        }
    }
}