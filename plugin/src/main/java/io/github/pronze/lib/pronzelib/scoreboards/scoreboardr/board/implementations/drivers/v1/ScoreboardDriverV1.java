package io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.implementations.drivers.v1;
//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Board/Implementations/Drivers/V1/ScoreboardDriverV1.java

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.implementations.IBoard;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin.Session;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin.utility.LineLimits;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin.utility.ScoreboardStrings;
import io.github.pronze.sba.utils.Logger;

import java.util.HashMap;
import java.util.Objects;

public class ScoreboardDriverV1 implements IBoard {

    private Player player;
    private Scoreboard board;
    private Objective objective;
    private int lines;
    private HashMap<Integer, String> cache = new HashMap<>();

    @Override
    public void setPlayer(Player player) {
        this.player = player;

        this.board = Objects.requireNonNull(Session.getSession().plugin.getServer().getScoreboardManager())
                .getNewScoreboard();
        this.objective = this.board.registerNewObjective("sb1", "sb2");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName("");

        this.createTeams();
        this.setBoard();

        LineLimits.getLineLimit();
    }

    @Override
    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }

        if (title.length() > LineLimits.getLineLimit() * 2) {
            title = title.substring(0, LineLimits.getLineLimit() * 2);
        }

        this.objective.setDisplayName(title);
    }

    @Override
    public void setLine(int line, String content) {
        Logger.trace("ScoreboardDriver::setline({},{})", line, content);
        if (content == null) {
            content = "";
        }
        if (!shouldUpdate(line, content)) {
            return;
        }

        Team team = board.getTeam(line + "");
        String[] split = split(content);

        assert team != null;

        team.setPrefix(split[0]);
        team.setSuffix(split[1]);
    }

    private String[] split(String line) {
        if (line.length() < LineLimits.getLineLimit()) {
            return new String[] { line, "" };
        }

        String prefix = line.substring(0, LineLimits.getLineLimit());
        String suffix = line.substring(LineLimits.getLineLimit());

        if (prefix.endsWith("§")) { // Check if we accidentally cut off a color
            prefix = ScoreboardStrings.removeLastCharacter(prefix);
            suffix = "§" + suffix;
        } else if (prefix.contains("§")) { // Are there any colors we need to continue?
            suffix = ChatColor.getLastColors(prefix) + suffix;
        } else { // Just make sure the team color doesn't mess up anything
            suffix = "§f" + suffix;
        }

        if (suffix.length() > LineLimits.getLineLimit()) {
            suffix = suffix.substring(0, LineLimits.getLineLimit());
        }

        return new String[] { prefix, suffix };
    }

    private boolean shouldUpdate(int line, String content) {
        if (!cache.containsKey(line)) {
            cache.put(line, content);
            return true;
        }

        if (cache.get(line).equals(content)) {
            return false;
        }

        cache.put(line, content);
        return true;
    }

    @Override
    public void setLineCount(int lines) {
        this.lines = lines;

        createTeams();
    }

    @Override
    public Player getPlayer() {
        return this.getPlayer();
    }

    private void createTeams() {
        if(board!=null)
        {
            int score = this.lines;

            for (int i = 0; i < this.lines; i++) {
                Team team = board.getTeam(i + "");
                if (team == null) {
                    Team t = this.board.registerNewTeam(i + "");
                    t.addEntry(ChatColor.values()[i] + "");
                    this.objective.getScore(ChatColor.values()[i] + "").setScore(score);
                    score--;
                }
            }
            for (int i = board.getTeams().size() - 1; i >= this.lines; i++) {
                Team team = board.getTeam(i + "");
                if (team != null)
                    team.unregister();
            }
        }
    }

    private void setBoard() {
        this.player.setScoreboard(this.board);
    }
}