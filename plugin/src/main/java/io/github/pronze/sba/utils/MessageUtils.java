package io.github.pronze.sba.utils;

import io.github.pronze.sba.SBA;
import org.bukkit.Bukkit;

import java.util.Arrays;

public class MessageUtils {
    public static void showErrorMessage(String... messages) {
        Bukkit.getLogger().severe("======PLUGIN ERROR===========");
        Bukkit.getLogger().severe("Plugin: SBA is being disabled for the following error:");
        Arrays.stream(messages)
                .forEach(Bukkit.getLogger()::severe);
        Bukkit.getLogger().severe("=============================");
        SBAUtil.disablePlugin(SBA.getPluginInstance());
    }
}
