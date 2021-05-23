package pronze.hypixelify;


import org.bukkit.Bukkit;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

@Service
public class UpdateChecker {

    public Mono<String> checkForUpdates() {
        return Mono.create(sink -> {
            if (SBAHypixelify.getInstance().isSnapshot()) {
                sink.success();
            }
            try (final var inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=79505").openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    sink.success(scanner.next());
                }
            } catch (IOException exception) {
                sink.error(exception);
            }
        });
    }

    @OnPostEnable
    protected void run() {
        checkForUpdates()
                .doOnError(throwable -> Bukkit.getLogger().info("Cannot look for updates: " + throwable.getMessage()))
                .doOnNext(this::promptUpdate)
                .subscribe();
    }

    private void promptUpdate(String version) {
        if (version == null) {
            throw new UnsupportedOperationException("Update Version cannot be null!");
        }
        if (!version.equalsIgnoreCase(SBAHypixelify.getInstance().getVersion())) {
            Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
            Bukkit.getLogger().info("Download it from here: https://www.spigotmc.org/resources/addon-sbahypixelify-for-screaming-bedwars-1-9-4-1-16-4.79505/");
        } else {
            Bukkit.getLogger().info("No updates found");
        }
    }
}