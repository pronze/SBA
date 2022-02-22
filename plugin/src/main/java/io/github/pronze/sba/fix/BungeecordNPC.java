package io.github.pronze.sba.fix;

import org.bukkit.Bukkit;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

public class BungeecordNPC extends BaseFix {
    private static BungeecordNPC instance;
    private boolean isProblematic = false;
    public void detect()
    {
        var isBungee = Bukkit.getServer().spigot().getSpigotConfig().getBoolean("settings.bungeecord");
        var isOffline = !Bukkit.getServer().spigot().getConfig().getBoolean("online-mode");

        isProblematic = isBungee && isOffline;
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

    @Override
    public void warn() {
        Logger.warn("NPC currently don't work under bungeecord, consider disabling NPC");
    }
}
