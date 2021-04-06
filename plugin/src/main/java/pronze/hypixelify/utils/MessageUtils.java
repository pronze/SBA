package pronze.hypixelify.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pronze.hypixelify.SBAHypixelify;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class MessageUtils {

    public static void showErrorMessage(String... messages) {
        Bukkit.getLogger().severe("======PLUGIN ERROR===========");
        Bukkit.getLogger().severe("Plugin: SBAHypixelify is being disabled for the following error:");
        Arrays.stream(messages)
                .filter(Objects::nonNull)
                .forEach(Bukkit.getLogger()::severe);
        Bukkit.getLogger().severe("=============================");
        Bukkit.getServer().getPluginManager().disablePlugin(SBAHypixelify.getInstance());
    }

}
