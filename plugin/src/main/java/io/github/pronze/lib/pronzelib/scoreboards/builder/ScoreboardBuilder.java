// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards.builder;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.Objects;
import io.github.pronze.lib.pronzelib.scoreboards.api.UpdateCallback;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.ConfigBoard;
import io.github.pronze.sba.utils.Logger;
import net.kyori.adventure.text.Component;
import io.github.pronze.lib.pronzelib.scoreboards.api.PlaceholderFunction;
import java.util.List;
import org.bukkit.entity.Player;
import org.screamingsandals.lib.sidebar.Sidebar;

import io.github.pronze.lib.pronzelib.scoreboards.Scoreboard;
import java.util.Map;

public class ScoreboardBuilder
{
    private final Map<String, String> placeholders;
    private Scoreboard scoreboard;
    private Player holder;
    private boolean animation;
    private boolean occupyWidth;
    private boolean occupyHeight;
    private long interval;
    private long animationInterval;
    private String title;
    private String objectiveName;
    private List<String> lines;
    private List<String> animatedTitle;
    private PlaceholderFunction papiFunction;
    private UpdateCallback updateCallback;
    
    public ScoreboardBuilder updateCallback(final UpdateCallback callback) {
        this.updateCallback = callback;
        return this;
    }
    
    public ScoreboardBuilder placeholder(final String placeholder, final Object value) {
        this.placeholders.put(placeholder, value.toString());
        return this;
    }
    
    public ScoreboardBuilder placeholderHook(final PlaceholderFunction papiFunction) {
        this.papiFunction = papiFunction;
        return this;
    }
    
    public ScoreboardBuilder player(final Player player) {
        this.holder = player;
        return this;
    }
    
    public ScoreboardBuilder lines(final List<String> lines) {
        this.lines = lines;
        return this;
    }
    
    public ScoreboardBuilder animate(final boolean animate) {
        this.animation = animate;
        return this;
    }
    
    public ScoreboardBuilder title(final String title) {
        this.title = title;
        return this;
    }
    
    public ScoreboardBuilder animatedTitle(final List<String> title) {
        this.animatedTitle = title;
        return this;
    }
    
    public ScoreboardBuilder occupyMaxHeight(final boolean bool) {
        this.occupyHeight = bool;
        return this;
    }
    
    public ScoreboardBuilder occupyMaxWidth(final boolean bool) {
        this.occupyWidth = bool;
        return this;
    }
    
    public ScoreboardBuilder displayObjective(final String objectiveName) {
        this.objectiveName = objectiveName;
        return this;
    }
    
    public ScoreboardBuilder updateInterval(final long interval) {
        this.interval = interval;
        return this;
    }
    
    public ScoreboardBuilder animationInterval(final long interval) {
        this.animationInterval = interval;
        return this;
    }
    
    public Scoreboard build() {
        Logger.trace("ScoreboardBuilder::build");
        Objects.requireNonNull(this.holder, "Holder cannot be null");
        (this.scoreboard = new Scoreboard(this.holder)).setAnimationTaskInterval(this.animationInterval);
        this.scoreboard.setOccupyMaxHeight(this.occupyHeight);
        this.scoreboard.setOccupyMaxWidth(this.occupyWidth);
        this.scoreboard.setUpdateTaskInterval(this.interval);
        if (this.title != null) {
            this.scoreboard.setAnimatedTitle(List.of(this.title));
        }
        if (this.lines != null) {
            this.scoreboard.setLines(this.lines);
        }
        if (this.papiFunction != null) {
            this.scoreboard.setPlaceholderHook(this.papiFunction);
        }
        if (this.animatedTitle != null) {
            this.scoreboard.setAnimatedTitle(this.animatedTitle);
        }
        if (this.updateCallback != null) {
            this.scoreboard.setCallback(this.updateCallback);
        }
        final Map<String, String> placeholders = this.placeholders;
        final Scoreboard scoreboard = this.scoreboard;
        Objects.requireNonNull(scoreboard);
        placeholders.forEach(scoreboard::addInternalPlaceholder);
        return this.scoreboard;
    }
    
    public ScoreboardBuilder() {
        this.placeholders = new HashMap<String, String>();
        this.interval = 20L;
        this.animationInterval = 2L;
    }
}
