// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.github.pronze.lib.pronzelib.scoreboards.api.PlaceholderFunction;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import io.github.pronze.lib.pronzelib.scoreboards.animations.ScoreboardAnimator;
import java.util.function.BiConsumer;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collections;
import io.github.pronze.lib.pronzelib.scoreboards.builder.ScoreboardBuilder;
import org.bukkit.entity.Player;
import java.util.List;
import io.github.pronze.lib.pronzelib.scoreboards.api.UpdateCallback;
import org.bukkit.scheduler.BukkitTask;
import io.github.pronze.lib.pronzelib.scoreboards.holder.ScoreboardHolder;
import io.github.pronze.sba.utils.Logger;

public class Scoreboard
{
    private final ScoreboardHolder holder;
    protected BukkitTask animationTask;
    protected BukkitTask updateTask;
    private long ANIMATION_TASK_INTERVAL;
    private long UPDATE_TASK_INTERVAL;
    private boolean occupyMaxHeight;
    private boolean occupyMaxWidth;
    private UpdateCallback callback;
    private List<String> animatedTitle;
    private boolean updateTaskRunning;
    private boolean animationTaskRunning;
    
    public Scoreboard(final Player player) {
        //Logger.trace("Scoreboard init (player)");
        this.ANIMATION_TASK_INTERVAL = 2L;
        this.UPDATE_TASK_INTERVAL = 20L;
        this.occupyMaxHeight = false;
        this.occupyMaxWidth = false;
        this.updateTaskRunning = false;
        this.animationTaskRunning = false;
        if (ScoreboardManager.getPluginInstance() == null) {
            throw new NullPointerException("Plugin instance not set! call ScoreboardManager.install() first");
        }
        this.holder = new ScoreboardHolder(player);
        this.startUpdateTask();
        ScoreboardManager.getInstance().addToCache(this);
    }
    
    public static ScoreboardBuilder builder() {
        //Logger.trace("Scoreboard builder");
        return new ScoreboardBuilder();
    }
    
    public void setLines(List<String> lines) {
        //Logger.trace("Scoreboard setLines[1] {}",lines);
        if (lines == null || lines.isEmpty()) {
            return;
        }
        if (lines.equals(this.holder.getLines().values())) {
            return;
        }
        
        lines = this.resizeContent(lines);
        //Logger.trace("Scoreboard setLines[2] {}",lines);
        Collections.reverse(lines);
        int i = 0;
        for (final String line : lines) {
            //Logger.trace("Scoreboard setLines::{}",line);
            this.holder.setLine(i, line);
            ++i;
        }
    }
    
    public void setVisibility(final boolean visible) {

        //Logger.trace("Scoreboard setVisibility"+visible);
        this.holder.setVisible(visible);
        this.refresh();
    }
    
    public void refresh() {
        //Logger.trace("Scoreboard refresh");
        this.holder.registerObjective(this.holder.getObjectiveName());
        this.holder.setTitle(this.holder.getDisplayName());
        final TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>(this.holder.getLines());
        final ScoreboardHolder holder = this.holder;
        Objects.requireNonNull(holder);
        treeMap.forEach((k,v)->{
            holder.setLine(k, v);
        });
    }
    
    public void setTitle(final String title, final boolean animate) {
        //Logger.trace("Scoreboard setTitle"+title+animate);
        Objects.requireNonNull(title, "Title cannot be null");
        if (animate) {
            this.startAnimationTask(ScoreboardAnimator.getAnimatedTitle(title));
        }
        else {
            this.holder.setTitle(title);
        }
    }
    
    public void setAnimatedTitle(final List<String> animatedTitle) {
        if (animatedTitle == null || animatedTitle.isEmpty()) {
            throw new IllegalArgumentException("Animated title cannot be null or empty");
        }
        if (animatedTitle.size() == 1) {
            this.setTitle(animatedTitle.get(0), false);
            return;
        }
        this.startAnimationTask(animatedTitle);
        this.animatedTitle = animatedTitle;
    }
    
    public void cancelAnimationTask() {
        if (this.animationTask != null) {
            if (Bukkit.getScheduler().isQueued(this.animationTask.getTaskId()) || Bukkit.getScheduler().isCurrentlyRunning(this.animationTask.getTaskId()) || this.animationTaskRunning) {
                this.animationTask.cancel();
                this.animationTaskRunning = false;
            }
            this.animationTask = null;
        }
    }
    
    private void cancelUpdateTask() {
        if (this.updateTask != null) {
            if (Bukkit.getScheduler().isQueued(this.updateTask.getTaskId()) || Bukkit.getScheduler().isCurrentlyRunning(this.updateTask.getTaskId()) || this.updateTaskRunning) {
                this.updateTask.cancel();
            }
            this.updateTask = null;
        }
    }
    
    public String makeUnique(String toUnique, final List<String> from) {
        if (toUnique == null) {
            toUnique = " ";
        }
        final StringBuilder stringBuilder = new StringBuilder(toUnique);
        while (from.contains(stringBuilder.toString()) || (this.occupyMaxWidth && !from.contains( stringBuilder) && stringBuilder.length() < 40)) {
            stringBuilder.append(" ");
        }
        if (stringBuilder.length() > 40) {
            return stringBuilder.substring(0, 40);
        }
        return stringBuilder.toString();
    }
    
    public List<String> resizeContent(final List<String> lines) {
        final ArrayList<String> newList = new ArrayList<String>();
        lines.forEach(line -> newList.add(this.makeUnique(line, newList)));
        if (newList.size() > 15) {
            return newList.subList(0, 15);
        }
        if (this.occupyMaxHeight) {
            while (newList.size() < 16) {
                newList.add(this.makeUnique(" ", newList));
            }
        }
        //Logger.trace("Scoreboard.resizeContent {}", newList);
        return newList;
    }
    
    public void destroy() {
        //Logger.trace("Scoreboard.destroy");
        this.holder.setDestroyed(true);
        this.cancelTasks();
        this.holder.destroy();
        ScoreboardManager.getInstance().removeFromCache(this.holder.getPlayer().getUniqueId());
    }
    
    public void setPlaceholderHook(final PlaceholderFunction papiFunction) {
        //Logger.trace("Scoreboard.setPlaceholderHook {}",papiFunction);

        this.holder.setPapiFunction(papiFunction);
    }
    
    public void addInternalPlaceholder(final String placeholder, final Object value) {
        //Logger.trace("Scoreboard.addInternalPlaceholder {} {}",placeholder,value);

        this.holder.getPersistentPlaceholders().put(placeholder, value.toString());
    }
    
    public void setUpdateTaskInterval(final long interval) {
        //Logger.trace("Scoreboard.setUpdateTaskInterval {}",interval);

        this.UPDATE_TASK_INTERVAL = interval;
        this.startUpdateTask();
    }
    
    public void setAnimationTaskInterval(final long interval) {
        //Logger.trace("Scoreboard.setAnimationTaskInterval {}",interval);

        this.ANIMATION_TASK_INTERVAL = interval;
        this.startAnimationTask(this.animatedTitle);
    }
    
    public void setCallback(final UpdateCallback callback) {
        //Logger.trace("Scoreboard.setCallback {}",callback);

        this.callback = callback;
    }
    
    protected void startAnimationTask(final List<String> animatedTitle) {
        //Logger.trace("Scoreboard.startAnimationTask {}",animatedTitle);

        this.cancelAnimationTask();
        if (animatedTitle == null) {
            return;
        }
        this.animationTaskRunning = true;
        this.animationTask = new BukkitRunnable() {
            int pos = 0;
            
            public void run() {
                if (Scoreboard.this.holder.isDestroyed()) {
                    this.cancel();
                    return;
                }
                if (this.pos >= animatedTitle.size()) {
                    this.pos = 0;
                }
                Scoreboard.this.holder.setTitle(animatedTitle.get(this.pos));
                ++this.pos;
            }
        }.runTaskTimer((Plugin)ScoreboardManager.getPluginInstance(), 0L, this.ANIMATION_TASK_INTERVAL);
    }
    
    protected void startUpdateTask() {
        //Logger.trace("Scoreboard.startUpdateTask");

        this.cancelUpdateTask();
        this.updateTaskRunning = true;
        this.updateTask = new BukkitRunnable() {
            public void run() {
                if (Scoreboard.this.holder.isDestroyed()) {
                    this.cancel();
                    return;
                }
                if (Scoreboard.this.callback != null) {
                    final boolean cancelled = Scoreboard.this.callback.onCallback(Scoreboard.this);
                    if (cancelled) {
                        return;
                    }
                }
                final TreeMap<Integer, String> lines = new TreeMap<Integer, String>(Scoreboard.this.holder.getLines());
                if (!lines.isEmpty()) {
                    final TreeMap<Integer, String> treeMap = lines;
                    final ScoreboardHolder holder = Scoreboard.this.holder;
                    Objects.requireNonNull(holder);
                    treeMap.forEach(holder::setLine);
                }
            }
        }.runTaskTimer((Plugin)ScoreboardManager.getPluginInstance(), 0L, this.UPDATE_TASK_INTERVAL);
    }
    
    protected void cancelTasks() {
        //Logger.trace("Scoreboard.cancelTasks");

        this.cancelAnimationTask();
        this.cancelUpdateTask();
    }
    
    public ScoreboardHolder getHolder() {
        //Logger.trace("Scoreboard.getHolder");

        return this.holder;
    }
    
    public void setOccupyMaxHeight(final boolean occupyMaxHeight) {
        //Logger.trace("Scoreboard.setOccupyMaxHeight {}",occupyMaxHeight);

        this.occupyMaxHeight = occupyMaxHeight;
    }
    
    public void setOccupyMaxWidth(final boolean occupyMaxWidth) {
        //Logger.trace("Scoreboard.setOccupyMaxWidth {}",occupyMaxWidth);

        this.occupyMaxWidth = occupyMaxWidth;
    }
}
