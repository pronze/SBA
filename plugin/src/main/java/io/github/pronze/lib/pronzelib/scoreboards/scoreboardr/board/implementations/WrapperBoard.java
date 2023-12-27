package io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.implementations;
//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Board/Implementations/WrapperBoard.java

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.implementations.drivers.v1.ScoreboardDriverV1;

public class WrapperBoard implements IBoard {

    private IBoard child;

    /**
     * Load the WrapperBoard with driver specification
     * @param driver What driver should be loaded
     * @throws ClassNotFoundException This really can't be thrown, but Intelliji wants me to
     * @throws IllegalAccessException This can be thrown tho
     * @throws InstantiationException This really can't be thrown too
     */
    public WrapperBoard(String driver) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
       
        if (this.child == null) {
            this.child = (IBoard) new ScoreboardDriverV1();
        }
    }

    @Override
    public void setPlayer(Player player) {
        child.setPlayer(player);
    }

    @Override
    public void setTitle(String title) {
        child.setTitle(title);
    }

    @Override
    public void setLine(int line, String content) {
        child.setLine(line, content);
    }

    @Override
    public void setLineCount(int lines) {
        child.setLineCount(lines);
    }

    @Override
    public Player getPlayer() {
        return child.getPlayer();
    }

    public void setObjective(String objectiveName) {
        child.setObjective(objectiveName);
    }

    public boolean hasTeamEntry(String invisTeamName) {
        return child.hasTeamEntry(invisTeamName);
    }

    public Team addTeam(String invisTeamName, ChatColor chatColor) {
        return child.addTeam(invisTeamName,chatColor);
    }

    public Optional<Team> getTeamEntry(String invisTeamName) {
        return child.getTeamEntry(invisTeamName);
    }

    public Team getTeamOrRegister(String invisTeamName) {
        return child.getTeamOrRegister(invisTeamName);
    }
}