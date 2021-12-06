// 
// Decompiled by Procyon v0.5.36
// 

package io.github.pronze.lib.pronzelib.scoreboards.data;

import io.github.pronze.lib.pronzelib.scoreboards.holder.ScoreboardHolder;
import org.bukkit.entity.Player;

public class PlaceholderData
{
    private final Player player;
    private final ScoreboardHolder holder;
    private final String line;
    
    public PlaceholderData(final Player player, final ScoreboardHolder holder, final String line) {
        this.player = player;
        this.holder = holder;
        this.line = line;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public ScoreboardHolder getHolder() {
        return this.holder;
    }
    
    public String getLine() {
        return this.line;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlaceholderData)) {
            return false;
        }
        final PlaceholderData other = (PlaceholderData)o;
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
        final Object this$holder = this.getHolder();
        final Object other$holder = other.getHolder();
        Label_0102: {
            if (this$holder == null) {
                if (other$holder == null) {
                    break Label_0102;
                }
            }
            else if (this$holder.equals(other$holder)) {
                break Label_0102;
            }
            return false;
        }
        final Object this$line = this.getLine();
        final Object other$line = other.getLine();
        if (this$line == null) {
            if (other$line == null) {
                return true;
            }
        }
        else if (this$line.equals(other$line)) {
            return true;
        }
        return false;
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof PlaceholderData;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $player = this.getPlayer();
        result = result * 59 + (($player == null) ? 43 : $player.hashCode());
        final Object $holder = this.getHolder();
        result = result * 59 + (($holder == null) ? 43 : $holder.hashCode());
        final Object $line = this.getLine();
        result = result * 59 + (($line == null) ? 43 : $line.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return ""+getPlayer() + this.getHolder() + this.getLine();
    }
}
