package io.github.pronze.sba.fix;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class BungeecordNPC extends BaseFix {
    private static BungeecordNPC instance;
    private boolean isProblematic = false;
    public void detect()
    {
        /*boolean isBungee = false;
        boolean isOffline = false;
        File file = new File("spigot.yml");
        try {
            isBungee = YamlConfiguration.loadConfiguration(file).getBoolean("settings.bungeecord");
        } catch (Throwable e) {
            //TODO: handle exception
        }
        
        isOffline = !Bukkit.getServer().spigot().getConfig().getBoolean("online-mode");
        //System.err.println("isBungee:" + isBungee);
        //System.err.println("isOffline:" + isOffline);
        isProblematic = isBungee && isOffline;*/
        isProblematic = false;
    }

    public static BungeecordNPC getInstance() {
        if (instance == null)
            setInstance(new BungeecordNPC());
        return instance;
    }

    private static void setInstance(BungeecordNPC instance) {
        BungeecordNPC.instance = instance;
    }

    public void fix(SBAConfig cfg)
    {
        if (cfg == null)
            cfg = SBAConfig.getInstance();
        if(isProblematic)
        {
            //cfg.set("editing-hologram-enabled", false);
            //cfg.set("npc.enabled", false);
            //cfg.set("replace-stores-with-npc", false);
        }
    }
    public boolean CanRunNPC()
    {
        return !isProblematic;
    }
    public boolean IsProblematic()
    {
        return isProblematic;
    }
    public boolean IsCritical()
    {
        return false;
    }
    @Override
    public void warn() {
        Logger.warn("NPC currently don't work under bungeecord, consider disabling NPC");
    }
}
