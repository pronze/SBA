// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards.holder;

import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;
import lombok.NonNull;
import java.util.Iterator;
import me.clip.placeholderapi.PlaceholderAPI;
import java.util.Map;
import io.github.pronze.lib.pronzelib.scoreboards.data.PlaceholderData;
import org.bukkit.scoreboard.Score;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.Objects;
import org.bukkit.scoreboard.DisplaySlot;
import java.util.Optional;
import org.bukkit.plugin.Plugin;
import io.github.pronze.lib.pronzelib.scoreboards.ScoreboardManager;
import org.bukkit.Bukkit;
import io.github.pronze.lib.pronzelib.scoreboards.api.PlaceholderFunction;
import org.bukkit.scoreboard.Objective;
import java.util.TreeMap;
import java.util.HashMap;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.entity.Player;
import io.github.pronze.sba.utils.Logger;

public class ScoreboardHolder
{
    private final Player player;
    private final Scoreboard bukkitScoreboard;
    private final HashMap<String, Object> persistentPlaceholders;
    private final TreeMap<Integer, String> lines;
    private boolean isVisible;
    private Objective objective;
    private PlaceholderFunction papiFunction;
    private String displayName;
    private String objectiveName;
    private boolean destroyed;
    
    public ScoreboardHolder(final Player player) {
        //Logger.trace("ScoreboardHolder.<init>", player);
        this.bukkitScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.persistentPlaceholders = new HashMap<String, Object>();
        this.lines = new TreeMap<Integer, String>();
        this.isVisible = true;
        this.displayName = "§c>>>> §fScoreboard §c<<<<";
        this.objectiveName = "pronze_lib";
        this.player = player;
        this.registerObjective(this.objectiveName);
        this.commit();
    }
    
    public void commit() {
        //Logger.trace("ScoreboardHolder.commit({})", player);

        if (this.isVisible) {
            Bukkit.getScheduler().runTask((Plugin)ScoreboardManager.getPluginInstance(), () -> this.player.setScoreboard(this.bukkitScoreboard));
        }
    }
    
    public void destroy() {
        //Logger.trace("ScoreboardHolder.destroy");

        try {
            this.objective.unregister();
        }
        catch (Exception ex) {}
        this.objective = null;
        this.lines.clear();
        this.player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        this.destroyed = true;
    }
    
    public Optional<Objective> getObjective() {
        //Logger.trace("ScoreboardHolder.getObjective");

        return Optional.ofNullable(this.objective);
    }
    
    public void registerObjective(final String objectiveName) {
        //Logger.trace("ScoreboardHolder.registerObjective({})", objectiveName);

        if (this.bukkitScoreboard.getObjective(objectiveName) != null) {
            return;
        }
        if (this.objective != null) {
            try {
                this.objective.unregister();
            }
            catch (Throwable t) {}
        }
        (this.objective = this.bukkitScoreboard.registerNewObjective(objectiveName, "dummy")).setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objectiveName = objectiveName;
    }
    
    public void setTitle(String displayName) {
        //Logger.trace("ScoreboardHolder.setTitle({})", displayName);

        if (displayName.length() > 32) {
            displayName = displayName.substring(0, 32);
        }
        this.displayName = displayName;
        final Optional<Objective> objective = this.getObjective();
        final String finalDisplayName = displayName;
        objective.ifPresent(value -> value.setDisplayName(finalDisplayName));
    }
    
    public void setLine(final int pos, final String content) {
        //Logger.trace("ScoreboardHolder.setLine({},{})", pos,content);

        if (pos > 15) {
            throw new IllegalArgumentException("Position cannot be greater than 15");
        }
        if (pos < 0) {
            throw new IllegalArgumentException("Position cannot be less than 0");
        }
        Objects.requireNonNull(content, "Content cannot be null!");
        final String line = this.lines.get(pos);
        final String finalContent = this.setPlaceholders(content);
        if (line != null && line.equalsIgnoreCase(finalContent)) {
            return;
        }
        final String anotherString="";
        final Scoreboard bukkitScoreboard = this.bukkitScoreboard;
        Objects.requireNonNull(bukkitScoreboard);
        this.bukkitScoreboard.getEntries().stream().filter(Objects::nonNull).filter(entry -> this.objective.getScore(entry).getScore() == pos && !entry.equalsIgnoreCase(anotherString)).forEach(bukkitScoreboard::resetScores);

        final Score score = this.objective.getScore(finalContent);
        score.setScore(pos);
        this.lines.put(pos, content);
    }
    
    public String setPlaceholders(String content) {
        //Logger.trace("ScoreboardHolder.setPlaceholders({})", content);

        Objects.requireNonNull(content, "Content cannot be null");
        if (this.papiFunction != null) {
            content = this.papiFunction.handleReplace(new PlaceholderData(this.player, this, content));
        }
        for (final Map.Entry<String, Object> entry : this.persistentPlaceholders.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue().toString());
        }
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            content = PlaceholderAPI.setPlaceholders(this.player, content);
        }
        return content;
    }
    
    public void addTeam(@NonNull final String teamName, final ChatColor color) {
        //Logger.trace("ScoreboardHolder.addTeam({},{})", teamName,color);

        if (teamName == null) {
            throw new NullPointerException("teamName is marked non-null but is null");
        }
        final Team team = this.getTeamOrRegister(teamName);
        if (!ScoreboardManager.isLegacy()) {
            team.setColor(color);
        }
        else {
            team.setPrefix(color.toString());
        }
        team.setDisplayName(teamName);
    }
    
    @NonNull
    public Team getTeamOrRegister(@NonNull final String teamName) {
        //Logger.trace("ScoreboardHolder.getTeamOrRegister({})", teamName);

        if (teamName == null) {
            throw new NullPointerException("teamName is marked non-null but is null");
        }
        return (this.bukkitScoreboard.getTeam(teamName) == null) ? this.bukkitScoreboard.registerNewTeam(teamName) : this.bukkitScoreboard.getTeam(teamName);
    }
    
    public void removeTeam(@NonNull final String teamName) {
        //Logger.trace("ScoreboardHolder.removeTeam({})", teamName);
        if (teamName == null) {
            throw new NullPointerException("teamName is marked non-null but is null");
        }
        if (!this.hasTeamEntry(teamName)) {
            return;
        }
        final Team team = this.bukkitScoreboard.getTeam(teamName);
        if (team == null) {
            return;
        }
        team.unregister();
    }
    
    public boolean hasTeamEntry(@NonNull final String teamName) {
        //Logger.trace("ScoreboardHolder.hasTeamEntry({})", teamName);

        if (teamName == null) {
            throw new NullPointerException("teamName is marked non-null but is null");
        }
        return this.bukkitScoreboard.getTeams().stream().anyMatch(team -> teamName.equalsIgnoreCase(team.getName()));
    }
    
    public void addPlayerEntryToTeam(final Player player, @NonNull final String teamName) {
        //Logger.trace("ScoreboardHolder.addPlayerEntryToTeam({},{})", player,teamName);

        if (teamName == null) {
            throw new NullPointerException("teamName is marked non-null but is null");
        }
        final Optional<Team> optionalTeam = this.getTeamEntry(teamName);
        if (optionalTeam.isEmpty()) {
            throw new IllegalArgumentException( teamName);
        }
        final Team team = optionalTeam.get();
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }
    
    public Optional<Team> getTeamEntry(final String teamName) {
        //Logger.trace("ScoreboardHolder.getTeamEntry({})", teamName);

        return Optional.ofNullable(this.bukkitScoreboard.getTeam(teamName));
    }
    
    public void removePlayerEntry(final Player player) {
        //Logger.trace("ScoreboardHolder.removePlayerEntry({})", player);

        final String playerName = player.getName();
        final String s="";
        this.bukkitScoreboard.getTeams().forEach(team -> {
            if (team.hasEntry(s)) {
                team.removeEntry(s);
            }
        });
    }
    
    public void setVisible(final boolean visibility) {
        //Logger.trace("ScoreboardHolder.setVisible({})", visibility);

        this.isVisible = visibility;
        final Scoreboard toSet = visibility ? this.bukkitScoreboard : Bukkit.getScoreboardManager().getMainScoreboard();
        this.player.setScoreboard(toSet);
    }
    
    public void hide() {
        //Logger.trace("ScoreboardHolder.hide()");

        this.setVisible(false);
    }
    
    public void show() {
        //Logger.trace("ScoreboardHolder.hide()");
        this.setVisible(true);
    }
    
    public Player getPlayer() {
        //Logger.trace("ScoreboardHolder.getPlayer()");
        return this.player;
    }
    
    public Scoreboard getBukkitScoreboard() {
        //Logger.trace("ScoreboardHolder.getBukkitScoreboard()");
        return this.bukkitScoreboard;
    }
    
    public HashMap<String, Object> getPersistentPlaceholders() {
        //Logger.trace("ScoreboardHolder.getPersistentPlaceholders()");

        return this.persistentPlaceholders;
    }
    
    public TreeMap<Integer, String> getLines() {

        return this.lines;
    }
    
    public boolean isVisible() {
        //Logger.trace("ScoreboardHolder.isVisible()");

        return this.isVisible;
    }
    
    public PlaceholderFunction getPapiFunction() {
        //Logger.trace("ScoreboardHolder.getPapiFunction()");

        return this.papiFunction;
    }
    
    public String getDisplayName() {
        //Logger.trace("ScoreboardHolder.getDisplayName()");

        return this.displayName;
    }
    
    public String getObjectiveName() {
        //Logger.trace("ScoreboardHolder.getObjectiveName()");

        return this.objectiveName;
    }
    
    public boolean isDestroyed() {

        return this.destroyed;
    }
    
    public void setObjective(final Objective objective) {
        this.objective = objective;
    }
    
    public void setPapiFunction(final PlaceholderFunction papiFunction) {
        this.papiFunction = papiFunction;
    }
    
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
    
    public void setObjectiveName(final String objectiveName) {
        this.objectiveName = objectiveName;
    }
    
    public void setDestroyed(final boolean destroyed) {
        this.destroyed = destroyed;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ScoreboardHolder)) {
            return false;
        }
        final ScoreboardHolder other = (ScoreboardHolder)o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$player = this.getPlayer();
        final Object other$player = other.getPlayer();
        Label_0065: {
            if (this$player == null) {
                if (other$player == null) {
                    break Label_0065;
                }
            }
            else if (this$player.equals(other$player)) {
                break Label_0065;
            }
            return false;
        }
        final Object this$bukkitScoreboard = this.getBukkitScoreboard();
        final Object other$bukkitScoreboard = other.getBukkitScoreboard();
        Label_0102: {
            if (this$bukkitScoreboard == null) {
                if (other$bukkitScoreboard == null) {
                    break Label_0102;
                }
            }
            else if (this$bukkitScoreboard.equals(other$bukkitScoreboard)) {
                break Label_0102;
            }
            return false;
        }
        final Object this$persistentPlaceholders = this.getPersistentPlaceholders();
        final Object other$persistentPlaceholders = other.getPersistentPlaceholders();
        Label_0139: {
            if (this$persistentPlaceholders == null) {
                if (other$persistentPlaceholders == null) {
                    break Label_0139;
                }
            }
            else if (this$persistentPlaceholders.equals(other$persistentPlaceholders)) {
                break Label_0139;
            }
            return false;
        }
        final Object this$lines = this.getLines();
        final Object other$lines = other.getLines();
        Label_0176: {
            if (this$lines == null) {
                if (other$lines == null) {
                    break Label_0176;
                }
            }
            else if (this$lines.equals(other$lines)) {
                break Label_0176;
            }
            return false;
        }
        if (this.isVisible() != other.isVisible()) {
            return false;
        }
        final Object this$objective = this.getObjective();
        final Object other$objective = other.getObjective();
        Label_0226: {
            if (this$objective == null) {
                if (other$objective == null) {
                    break Label_0226;
                }
            }
            else if (this$objective.equals(other$objective)) {
                break Label_0226;
            }
            return false;
        }
        final Object this$papiFunction = this.getPapiFunction();
        final Object other$papiFunction = other.getPapiFunction();
        Label_0263: {
            if (this$papiFunction == null) {
                if (other$papiFunction == null) {
                    break Label_0263;
                }
            }
            else if (this$papiFunction.equals(other$papiFunction)) {
                break Label_0263;
            }
            return false;
        }
        final Object this$displayName = this.getDisplayName();
        final Object other$displayName = other.getDisplayName();
        Label_0300: {
            if (this$displayName == null) {
                if (other$displayName == null) {
                    break Label_0300;
                }
            }
            else if (this$displayName.equals(other$displayName)) {
                break Label_0300;
            }
            return false;
        }
        final Object this$objectiveName = this.getObjectiveName();
        final Object other$objectiveName = other.getObjectiveName();
        if (this$objectiveName == null) {
            if (other$objectiveName == null) {
                return this.isDestroyed() == other.isDestroyed();
            }
        }
        else if (this$objectiveName.equals(other$objectiveName)) {
            return this.isDestroyed() == other.isDestroyed();
        }
        return false;
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof ScoreboardHolder;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $player = this.getPlayer();
        result = result * 59 + (($player == null) ? 43 : $player.hashCode());
        final Object $bukkitScoreboard = this.getBukkitScoreboard();
        result = result * 59 + (($bukkitScoreboard == null) ? 43 : $bukkitScoreboard.hashCode());
        final Object $persistentPlaceholders = this.getPersistentPlaceholders();
        result = result * 59 + (($persistentPlaceholders == null) ? 43 : $persistentPlaceholders.hashCode());
        final Object $lines = this.getLines();
        result = result * 59 + (($lines == null) ? 43 : $lines.hashCode());
        result = result * 59 + (this.isVisible() ? 79 : 97);
        final Object $objective = this.getObjective();
        result = result * 59 + (($objective == null) ? 43 : $objective.hashCode());
        final Object $papiFunction = this.getPapiFunction();
        result = result * 59 + (($papiFunction == null) ? 43 : $papiFunction.hashCode());
        final Object $displayName = this.getDisplayName();
        result = result * 59 + (($displayName == null) ? 43 : $displayName.hashCode());
        final Object $objectiveName = this.getObjectiveName();
        result = result * 59 + (($objectiveName == null) ? 43 : $objectiveName.hashCode());
        result = result * 59 + (this.isDestroyed() ? 79 : 97);
        return result;
    }
    
    @Override
    public String toString() {
        return  ""+this.getPlayer() + this.getBukkitScoreboard()+ this.getPersistentPlaceholders()+ this.getLines()+ this.isVisible()+ this.getObjective()+ this.getPapiFunction()+ this.getDisplayName()+ this.getObjectiveName()+ this.isDestroyed();
    } 
}
