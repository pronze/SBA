package org.pronze.hypixelify.listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.pronze.hypixelify.Hypixelify;
import java.util.ArrayList;
import java.util.List;

public class ListenerManager {

    private final List<AbstractListener> listeners = new ArrayList<>();

    public ListenerManager(){
        listeners.add(new PlayerListener());
        if (Hypixelify.getConfigurator().config.getBoolean("party.enabled")) {
            listeners.add(new ChatListener());
            if(Hypixelify.getConfigurator().config.getBoolean("party.leader-autojoin-autoleave", true))
                listeners.add(new PartyListener());
        }
        if(Hypixelify.getConfigurator().config.getBoolean("main-lobby.enabled", false)){
            listeners.add(new LobbyBoard());
        }
        if(Hypixelify.getConfigurator().config.getBoolean("lobby-scoreboard.enabled", true))
            listeners.add(new LobbyScoreboard());

    }

    public void registerAll(JavaPlugin plugin){
        if(listeners.isEmpty()) return;

        for(AbstractListener listener : listeners){
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public void unregisterAll(){
        if(listeners == null || listeners.isEmpty()) return;

        for(AbstractListener l : listeners){
            l.onDisable();
        }
        listeners.clear();
    }
}
