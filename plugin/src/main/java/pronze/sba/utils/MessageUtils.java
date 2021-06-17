package pronze.sba.utils;

import org.bukkit.Bukkit;
import pronze.sba.SBA;

import java.util.Arrays;

public class MessageUtils {
    public static void showErrorMessage(String... messages) {
        Bukkit.getLogger().severe("======PLUGIN ERROR===========");
        Bukkit.getLogger().severe("Plugin: SBA is being disabled for the following error:");
        Arrays.stream(messages)
                .forEach(Bukkit.getLogger()::severe);
        Bukkit.getLogger().severe("=============================");
        Bukkit.getServer().getPluginManager().disablePlugin(SBA.getPluginInstance());
    }
}
