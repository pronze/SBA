// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.Objects;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import java.util.Map;
import org.bukkit.event.Listener;

import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin.Session;
import io.github.pronze.sba.utils.Logger;

public class ScoreboardManager implements Listener
{
    private static ScoreboardManager instance;
    private final Map<UUID, Scoreboard> cachedBoards;
    private boolean toReset;
    private boolean legacy;
    private JavaPlugin plugin;
    
    public ScoreboardManager() {
        //Logger.trace("ScoreboardManager.<init>");
        this.cachedBoards = new HashMap<UUID, Scoreboard>();
        this.toReset = true;
    }
    
    public static ScoreboardManager init(final JavaPlugin plugin) {
        //Logger.trace("ScoreboardManager.init");
        Objects.requireNonNull(plugin, "Plugin instance cannot be null");
        Session.makeSession(plugin);
        if (ScoreboardManager.instance != null) {
            ScoreboardManager.instance.onDisable();
        }
        ScoreboardManager.instance = new ScoreboardManager();
        ScoreboardManager.instance.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents((Listener)ScoreboardManager.instance, (Plugin)plugin);
        final String[] bukkitVersion = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        int versionNumber = 0;
        for (int i = 0; i < 2; ++i) {
            versionNumber += Integer.parseInt(bukkitVersion[i]) * ((i == 0) ? 100 : 1);
        }
        ScoreboardManager.instance.legacy = (versionNumber < 113);
        return ScoreboardManager.instance;
    }
    
    public static boolean isLegacy() {
        //Logger.trace("ScoreboardManager.isLegacy");
        return ScoreboardManager.instance.legacy;
    }
    
    public static void setResetBoardsOnDisabled(final boolean boardsOnDisabled) {
        //Logger.trace("ScoreboardManager.setResetBoardsOnDisabled");
        ScoreboardManager.instance.toReset = boardsOnDisabled;
    }
    
    public static JavaPlugin getPluginInstance() {
        //Logger.trace("ScoreboardManager.getPluginInstance");
        return ScoreboardManager.instance.plugin;
    }
    
    public static ScoreboardManager getInstance() {
        //Logger.trace("ScoreboardManager.getInstance");
        return ScoreboardManager.instance;
    }
    
    public void onDisable() {
        //Logger.trace("ScoreboardManager.onDisable");
        HandlerList.unregisterAll((Listener)ScoreboardManager.instance);
        if (!this.toReset) {
            return;
        }
        List.copyOf((Collection<Scoreboard>)this.cachedBoards.values()).forEach(Scoreboard::destroy);
        this.cachedBoards.clear();
    }
    
    public void addToCache(final Scoreboard board) {
        //Logger.trace("ScoreboardManager.addToCache");
        Objects.requireNonNull(board, "Board cannot be null!");
        this.cachedBoards.put(board.getPlayer().getUniqueId(), board);
    }
    
    public void removeFromCache(final UUID uuid) {
        //Logger.trace("ScoreboardManager.removeFromCache");
        this.cachedBoards.remove(uuid);
    }
    
    public Optional<Scoreboard> fromCache(final UUID uuid) {
        //Logger.trace("ScoreboardManager.fromCache");
        if (this.cachedBoards.containsKey(uuid)) {
            return Optional.of(this.cachedBoards.get(uuid));
        }
        return Optional.empty();
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        //Logger.trace("ScoreboardManager.onQuit");
        final UUID uuid = e.getPlayer().getUniqueId();
        this.fromCache(uuid).ifPresent(Scoreboard::destroy);
        this.cachedBoards.remove(uuid);
    }
}
