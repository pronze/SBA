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
    }

    private void promptUpdate(@NotNull String version) {
        if (!version.equalsIgnoreCase(SBA.getInstance().getVersion())) {
            Bukkit.getLogger().info("§e§lTHERE IS A NEW UPDATE AVAILABLE Version: " + version);
            Bukkit.getLogger().info("Download it from here: https://www.spigotmc.org/resources/addon-sbahypixelify-for-screaming-bedwars-1-9-4-1-16-4.79505/");
        } else {
            Bukkit.getLogger().info("No updates found");
        }
    }
}