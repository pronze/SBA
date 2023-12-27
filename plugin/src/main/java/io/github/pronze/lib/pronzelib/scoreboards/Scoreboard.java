// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.github.pronze.lib.pronzelib.scoreboards.api.PlaceholderFunction;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.HashMap;

import io.github.pronze.lib.pronzelib.scoreboards.builder.ScoreboardBuilder;
import io.github.pronze.lib.pronzelib.scoreboards.data.PlaceholderData;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.BoardPlayer;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.ConfigBoard;

import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;

import io.github.pronze.lib.pronzelib.scoreboards.api.UpdateCallback;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.PlaceholderAPI;
import org.screamingsandals.lib.player.Players;

public class Scoreboard {
    private ConfigBoard holder;
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
    private org.screamingsandals.lib.player.Player player;
    private List<String> lines;
    private PlaceholderFunction papiFunction;
    private final HashMap<String, Object> persistentPlaceholders;

    public Scoreboard(final Player player) {
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
        this.player = Players.wrapPlayer(player);
        this.holder = new ConfigBoard(player.getName());
        BoardPlayer.getBoardPlayer(player).attachConfigBoard(this.holder);
        this.holder.runTaskTimerAsynchronously(ScoreboardManager.getPluginInstance(), 1, 1);
        this.startUpdateTask();
        ScoreboardManager.getInstance().addToCache(this);
    }

    public static ScoreboardBuilder builder() {
        return new ScoreboardBuilder();
    }

    public void setLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        lines = this.resizeContent(lines);
        int i = 0;

        this.lines = lines;

        refresh();
    }

    public void setVisibility(final boolean visible) {
        if (visible)
        {
            BoardPlayer.getBoardPlayer(player.as(Player.class)).attachConfigBoard(this.holder);
        }
        else
        {
            BoardPlayer.getBoardPlayer(player.as(Player.class)).kill();
        }
        this.refresh();
    }

    public void refresh() {
        if (lines != null) {
            holder.setLines(lines.stream().map(l -> this.setPlaceholders(l)).collect(Collectors.toList()));
        }
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
        Objects.requireNonNull(title, "Title cannot be null");

        this.holder.setTitle(List.of(title), ANIMATION_TASK_INTERVAL);
    }

    public void setAnimatedTitle(final List<String> animatedTitle) {
        if (animatedTitle == null || animatedTitle.isEmpty()) {
            throw new IllegalArgumentException("Animated title cannot be null or empty");
        }
        if (animatedTitle.size() == 1) {
            this.setTitle(animatedTitle.get(0), false);
            return;
        }

        this.holder.setTitle(animatedTitle, ANIMATION_TASK_INTERVAL);
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
        //if (stringBuilder.length() > 40) {
        //    return stringBuilder.substring(0, 40);
        //}
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
        return newList;
    }

    public void destroy() {
        holder.cancel();

        this.cancelTasks();
        ScoreboardManager.getInstance().removeFromCache(this.player.getUniqueId());
    }
    public void setObjective(String objectiveName) {
        holder.setObjective(objectiveName);
    }

    public void setPlaceholderHook(final PlaceholderFunction papiFunction) {
        this.papiFunction = (papiFunction);
    }

    public void addInternalPlaceholder(final String placeholder, final Object value) {
        this.persistentPlaceholders.put(placeholder, value.toString());
    }

    public void setUpdateTaskInterval(final long interval) {
        this.UPDATE_TASK_INTERVAL = interval;
        this.startUpdateTask();
    }

    public void setAnimationTaskInterval(final long interval) {
        this.ANIMATION_TASK_INTERVAL = interval;
    }

    public void setCallback(final UpdateCallback callback) {
        this.callback = callback;
    }

    protected void startUpdateTask() {
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
        this.cancelUpdateTask();
    }

    public ConfigBoard getHolder() {
        return this.holder;
    }

    public void setOccupyMaxHeight(final boolean occupyMaxHeight) {
        this.occupyMaxHeight = occupyMaxHeight;
    }

    public void setOccupyMaxWidth(final boolean occupyMaxWidth) {
        this.occupyMaxWidth = occupyMaxWidth;
    }

    public org.screamingsandals.lib.player.Player getPlayer() {
        return player;
    }

  
}
