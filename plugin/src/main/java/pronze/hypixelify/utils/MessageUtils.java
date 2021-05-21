package pronze.hypixelify.utils;

import org.bukkit.Bukkit;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageUtils {
    public static void showErrorMessage(String... messages) {
        Bukkit.getLogger().severe("======PLUGIN ERROR===========");
        Bukkit.getLogger().severe("Plugin: SBAHypixelify is being disabled for the following error:");
        Arrays.stream(messages)
                .forEach(Bukkit.getLogger()::severe);
        Bukkit.getLogger().severe("=============================");
        Bukkit.getServer().getPluginManager().disablePlugin(SBAHypixelify.getPluginInstance());
    }
}
