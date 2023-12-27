package io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin;
//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Plugin/ConfigControl.java
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;

/**
 * Created by Rien on 21-10-2018.
 */
public class ConfigControl {

    private static ConfigControl instance = null;

    public static ConfigControl get()
    {
        if(instance != null)
            return instance;

        return new ConfigControl();
    }

    HashMap<String, FileConfiguration> designations = new HashMap<>();

    private ConfigControl()
    {
        ConfigControl.instance = this;
        this.createDataFiles();
    }

    public void createDataFiles()
    {

        if(!Session.getSession().plugin.getDataFolder().exists())
            Session.getSession().plugin.getDataFolder().mkdirs();


        createConfigFile("settings");
        createConfigFile("language");
    }

    public void purge()
    {
        designations.clear();
    }

    public void createConfigFile(String name)
    {
        File f = new File(Session.getSession().plugin.getDataFolder(), name + ".yml");

        boolean needCopyDefaults = false;

        try {
            if(!f.exists())
            {
                f.createNewFile();
                needCopyDefaults = true;
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        if(needCopyDefaults)
        {
            try {
                Reader defConfigStream = new InputStreamReader(ConfigControl.class.getResourceAsStream("/" + name + ".yml"), "UTF-8");
                PrintWriter writer = new PrintWriter(f, "UTF-8");
                writer.print(read(defConfigStream));
                writer.close();
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        designations.put(name, fc);
    }

    public void reloadConfigs()
    {
        this.purge();
        this.createDataFiles();
    }

    public FileConfiguration gc(String fc)
    {
        return designations.get(fc);
    }

    public String read(Reader r)
            throws IOException {
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = r.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        r.close();
        return buffer.toString();
    }

}