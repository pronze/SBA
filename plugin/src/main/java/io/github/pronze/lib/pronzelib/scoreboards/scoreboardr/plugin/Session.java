package io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin;
//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Plugin/Session.java

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.ConfigBoard;

import java.util.ArrayList;

public class Session {

    public JavaPlugin plugin;

    private static Session session;

    public String[] dependencies = {"PlaceholderAPI"};
    public ArrayList<String> enabled_dependencies = new ArrayList<>();

    /**
     * Make a new session
     * @param plugin The JavaPlugin
     */
    public static void makeSession(JavaPlugin plugin) {
        session = new Session(plugin);
    }

    /**
     * Get the active session
     * @return Session
     */
    public static Session getSession() {
        return session;
    }

    /**
     * Session constructor
     * @param plugin the JavaPlugin
     */
    private Session(JavaPlugin plugin) {
        this.plugin = plugin;

        for(String dependency : this.dependencies)
            if(Bukkit.getPluginManager().isPluginEnabled(dependency))
                this.enabled_dependencies.add(dependency);
    }

}