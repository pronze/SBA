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
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;

import io.github.pronze.lib.pronzelib.scoreboards.builder.ScoreboardBuilder;
import io.github.pronze.lib.pronzelib.scoreboards.data.PlaceholderData;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;

import io.github.pronze.lib.pronzelib.scoreboards.api.UpdateCallback;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.sidebar.SidebarImpl;
import org.screamingsandals.lib.utils.visual.TextEntry;

import io.github.pronze.sba.utils.Logger;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;

public class Scoreboard {
    private Sidebar holder;
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
    private PlayerWrapper player;
    private List<String> lines;
    private PlaceholderFunction papiFunction;
    private final HashMap<String, Object> persistentPlaceholders;

    public Scoreboard(final Player player) {
        // Logger.trace("Scoreboard init (player)");
        this.ANIMATION_TASK_INTERVAL = 2L;
        this.UPDATE_TASK_INTERVAL = 20L;
        this.occupyMaxHeight = false;
        this.occupyMaxWidth = false;
        this.updateTaskRunning = false;
        this.animationTaskRunning = false;
        persistentPlaceholders = new HashMap<>();
        if (ScoreboardManager.getPluginInstance() == null) {
            throw new NullPointerException("Plugin instance not set! call ScoreboardManager.install() first");
        }
        this.player = PlayerMapper.wrapPlayer(player);
        this.holder = Sidebar.of().addViewer(this.player);
        this.holder.show();
        this.startUpdateTask();
        ScoreboardManager.getInstance().addToCache(this);
    }

    public static ScoreboardBuilder builder() {
        // Logger.trace("Scoreboard builder");
        return new ScoreboardBuilder();
    }

    public void setLines(List<String> lines) {
        // Logger.trace("Scoreboard setLines[1] {}",lines);
        if (lines == null || lines.isEmpty()) {
            return;
        }

        lines = this.resizeContent(lines);
        // Logger.trace("Scoreboard setLines[2] {}",lines);
        Collections.reverse(lines);
        int i = 0;

        this.lines = lines;

        refresh();
    }

    public void setVisibility(final boolean visible) {

        // Logger.trace("Scoreboard setVisibility"+visible);
        if (visible)
            holder.show();
        else
            holder.hide();
        this.refresh();
    }

    public void refresh() {
        // Logger.trace("Scoreboard refresh");
        if (lines != null) {
            var list2 = new ArrayList<Component>(
                lines.stream().map(l -> (Component) Component.text(this.setPlaceholders(l))).collect(Collectors.toList()));
            if (list2 != null)
                Collections.reverse(list2);
            holder.setLines(list2);
        }
        holder.update();
    }

    private @NotNull String setPlaceholders(String content) {
        Objects.requireNonNull(content, "Content cannot be null");
        if (this.papiFunction != null) {
            content = this.papiFunction.handleReplace(new PlaceholderData(this.player, this, content));
        }
        for (final Map.Entry<String, Object> entry : this.persistentPlaceholders.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue().toString());
        }
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            content = PlaceholderAPI.setPlaceholders(this.player.as(Player.class), content);
        }
        return content;
    }

    public void setTitle(final String title, final boolean animate) {
        // Logger.trace("Scoreboard setTitle"+title+animate);
        Objects.requireNonNull(title, "Title cannot be null");
        if (animate) {
            this.startAnimationTask(ScoreboardAnimator.getAnimatedTitle(title));
        } else {
            this.holder.title(Component.text(title));
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
            if (Bukkit.getScheduler().isQueued(this.animationTask.getTaskId())
                    || Bukkit.getScheduler().isCurrentlyRunning(this.animationTask.getTaskId())
                    || this.animationTaskRunning) {
                this.animationTask.cancel();
                this.animationTaskRunning = false;
            }
            this.animationTask = null;
        }
    }

    private void cancelUpdateTask() {
        if (this.updateTask != null) {
            if (Bukkit.getScheduler().isQueued(this.updateTask.getTaskId())
                    || Bukkit.getScheduler().isCurrentlyRunning(this.updateTask.getTaskId())
                    || this.updateTaskRunning) {
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
        while (from.contains(stringBuilder.toString())
                || (this.occupyMaxWidth && !from.contains(stringBuilder.toString()) && stringBuilder.length() < 40)) {
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
        // Logger.trace("Scoreboard.resizeContent {}", newList);
        return newList;
    }

    public void destroy() {
        // Logger.trace("Scoreboard.destroy");
        Logger.trace("Destroy sidebar for {}", holder.viewers());
        holder.viewers().forEach(viewer->((SidebarImpl)holder).onViewerRemoved(viewer,false));
        
        holder.hide();
        holder.destroy();
        this.cancelTasks();
        ScoreboardManager.getInstance().removeFromCache(this.player.getUniqueId());
    }

    public void setPlaceholderHook(final PlaceholderFunction papiFunction) {
        // Logger.trace("Scoreboard.setPlaceholderHook {}",papiFunction);

        this.papiFunction = (papiFunction);
    }

    public void addInternalPlaceholder(final String placeholder, final Object value) {
        // Logger.trace("Scoreboard.addInternalPlaceholder {} {}",placeholder,value);

        this.persistentPlaceholders.put(placeholder, value.toString());
    }

    public void setUpdateTaskInterval(final long interval) {
        // Logger.trace("Scoreboard.setUpdateTaskInterval {}",interval);

        this.UPDATE_TASK_INTERVAL = interval;
        this.startUpdateTask();
    }

    public void setAnimationTaskInterval(final long interval) {
        // Logger.trace("Scoreboard.setAnimationTaskInterval {}",interval);

        this.ANIMATION_TASK_INTERVAL = interval;
        this.startAnimationTask(this.animatedTitle);
    }

    public void setCallback(final UpdateCallback callback) {
        // Logger.trace("Scoreboard.setCallback {}",callback);

        this.callback = callback;
    }

    protected void startAnimationTask(final List<String> animatedTitle) {
        // Logger.trace("Scoreboard.startAnimationTask {}",animatedTitle);

        this.cancelAnimationTask();
        if (animatedTitle == null) {
            return;
        }
        this.animationTaskRunning = true;
        this.animationTask = new BukkitRunnable() {
            int pos = 0;

            public void run() {
                if (Scoreboard.this.holder == null) {
                    this.cancel();
                    return;
                }
                if (this.pos >= animatedTitle.size()) {
                    this.pos = 0;
                }
                Scoreboard.this.holder.title(Component.text(animatedTitle.get(this.pos)));
                ++this.pos;
            }
        }.runTaskTimer((Plugin) ScoreboardManager.getPluginInstance(), 0L, this.ANIMATION_TASK_INTERVAL);
    }

    protected void startUpdateTask() {
        // Logger.trace("Scoreboard.startUpdateTask");

        this.cancelUpdateTask();
        this.updateTaskRunning = true;
        this.updateTask = new BukkitRunnable() {
            public void run() {
                if (Scoreboard.this.holder == null) {
                    this.cancel();
                    return;
                }
                if (Scoreboard.this.callback != null) {
                    final boolean cancelled = Scoreboard.this.callback.onCallback(Scoreboard.this);
                    if (cancelled) {
                        return;
                    }
                }
                refresh();
            }
        }.runTaskTimer((Plugin) ScoreboardManager.getPluginInstance(), 0L, this.UPDATE_TASK_INTERVAL);
    }

    protected void cancelTasks() {
        // Logger.trace("Scoreboard.cancelTasks");

        this.cancelAnimationTask();
        this.cancelUpdateTask();
    }

    public Sidebar getHolder() {
        // Logger.trace("Scoreboard.getHolder");

        return this.holder;
    }

    public void setOccupyMaxHeight(final boolean occupyMaxHeight) {
        // Logger.trace("Scoreboard.setOccupyMaxHeight {}",occupyMaxHeight);

        this.occupyMaxHeight = occupyMaxHeight;
    }

    public void setOccupyMaxWidth(final boolean occupyMaxWidth) {
        // Logger.trace("Scoreboard.setOccupyMaxWidth {}",occupyMaxWidth);

        this.occupyMaxWidth = occupyMaxWidth;
    }

    public PlayerWrapper getPlayer() {
        return player;
    }
}
