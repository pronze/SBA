package io.github.pronze.sba;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.event.player.SPlayerJoinEvent;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;
import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service(dependsOn = {
        SBAConfig.class
})
public class UpdateChecker {

    private String version;
    @Getter
    private boolean isPendingUpdate = false;
    private JavaPlugin plugin;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static UpdateChecker getInstance() {
        return ServiceManager.get(UpdateChecker.class);
    }

    @OnPostEnable
    public void checkForUpdates() {
                if(SBA.isBroken())return;
                if (SBA.getInstance().isSnapshot()) {
             return;
        }
        if (SBAConfig.getInstance().shouldCheckUpdate())
            new Thread(() -> {
                // https://api.spiget.org/v2/resources/99149/download
                try (final var inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=99149")
                        .openStream(); Scanner scanner = new Scanner(inputStream)) {
                    if (scanner.hasNext()) {
                        promptUpdate(scanner.next());
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }).start();
    }

    private void promptUpdate(@NotNull String version) {
        this.version = version;
        if (!version.equalsIgnoreCase(SBA.getInstance().getVersion())) {
            if (SBAConfig.getInstance().shouldWarnConsoleAboutUpdate()) {
                Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
                Bukkit.getLogger().info(
                        "Download it from here: https://www.spigotmc.org/resources/sba-screaming-bedwars-addon-1-9-4-1-18-1.99149/ or run §e/sba updateplugin");
            }
            isPendingUpdate = true;

        } else {
            Bukkit.getLogger().info("No updates found");
        }
    }

    public void sendToUser(@NotNull Player player) {
        player.sendMessage("[SBA] §eTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
        player.sendMessage(
                "Download it from here: https://www.spigotmc.org/resources/sba-screaming-bedwars-addon-1-9-4-1-18-1.99149/ or run §e/sba updateplugin");
    }

    public void update(@NotNull CommandSender sender) {
        String newVersion = version;

        if (!newVersion.equalsIgnoreCase(SBA.getInstance().getVersion())) {
            try {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {

                            URL downloadUrl = new URL("https://api.spiget.org/v2/resources/99149/download");
                            HttpURLConnection httpConnection = (HttpURLConnection) downloadUrl.openConnection();
                            httpConnection.setRequestProperty("User-Agent", "SpigotResourceUpdater");

                            int grabSize = 2048;
                            BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());

                            File pluginFile = (File) Reflect.fastInvoke(plugin, "getFile");

                            FileOutputStream fos = new FileOutputStream(pluginFile);
                            BufferedOutputStream bout = new BufferedOutputStream(fos, grabSize);

                            byte[] data = new byte[grabSize];
                            int grab;
                            while ((grab = in.read(data, 0, grabSize)) >= 0) {
                                bout.write(data, 0, grab);
                            }

                            bout.close();
                            in.close();
                            fos.close();
                            try {
                                sender.sendMessage("Plugin JAR updated, please restart your server to receive the update");
                            } catch (Exception ex) {

                            }
                        } catch (Exception ex) {
                            Logger.error("Error occurred while updating . {}", ex);
                        }
                    }
                }.runTaskAsynchronously(plugin);

            } catch (Exception ex) {
                Logger.error("Error occurred while updating . {}", ex);
            }
        }
    }

}