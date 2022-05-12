package io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board;
//https://github.com/RienBijl/Scoreboard-revision/blob/master/src/main/java/rien/bijl/Scoreboard/r/Board/ConfigBoard.java
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.animations.Row;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board.implementations.WrapperBoard;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin.ConfigControl;
import io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.plugin.utility.ScoreboardStrings;
import io.github.pronze.sba.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ConfigBoard extends BukkitRunnable {

    public String board;
    private Row title;
    private ArrayList<Row> rows = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private HashMap<Player, WrapperBoard> playerToBoard = new HashMap<>();
    private boolean enabled;

    public ConfigBoard(String board)
    {
        this.board = board;
        enabled = true;
        this.initTitle();
        this.initRows();
    }

    public void setTitle(List<String> animation,long interval)
    {
        this.title = new Row(ScoreboardStrings.makeColoredStringList(animation), (int)interval);
    }
    private void initTitle()
    {
        this.title = new Row(ScoreboardStrings.makeColoredStringList(List.of()), 0);
    }

    private void initRows()
    {
        /*for (int i = 1; i < 200; i++) {
            ConfigurationSection section = ConfigControl.get().gc("settings").getConfigurationSection(this.board + ".rows." + i);
            if (section != null) {
                Row row = new Row(ScoreboardStrings.makeColoredStringList(section.getStringList("lines")), section.getInt("interval"));
                rows.add(row);
            }
        }*/
    }

    public void setLines(List<String> lines)
    {
        Logger.trace("ConfigBoard setLines {}",lines);

        for (int i = 0; i < lines.size() && i < rows.size();i++)
        {
            Logger.trace("ConfigBoard editRow {}",i);

            rows.set(i, new Row(ScoreboardStrings.makeColoredStringList(List.of(lines.get(i))), 0));
        }
        while(rows.size() < lines.size())
        {
            Logger.trace("ConfigBoard addRow {}",rows.size());

            rows.add(new Row(ScoreboardStrings.makeColoredStringList(List.of(lines.get(rows.size()))),0));
        }
        while(rows.size() > lines.size())
        {
            Logger.trace("ConfigBoard removeRow {}",rows.size()-1);

            rows.remove(rows.size() - 1);
        }
    }

    public void hookPlayer(Player player) {
        players.add(player);

        try {
            WrapperBoard wrapperBoard = new WrapperBoard("SCOREBOARD_DRIVER_V1");
            wrapperBoard.setLineCount(rows.size());
            wrapperBoard.setPlayer(player);
            playerToBoard.put(player, wrapperBoard);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void unhookPlayer(Player player) {
        playerToBoard.remove(player);
        players.remove(player);
        player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
    }

    @Override
    public void run() {
        Logger.trace("Running ConfigBoard update");
        if (!this.enabled) return;

        this.title.update();

        for (Row row : rows) {
            row.update();
        }
        

        for (Player player: playerToBoard.keySet()) {
            WrapperBoard wrapperBoard = playerToBoard.get(player);
            wrapperBoard.setTitle(this.title.getLine());
            wrapperBoard.setLineCount(rows.size());
            
            int count = 0;
            for (Row row: new ArrayList<>(rows)) {
                wrapperBoard.setLine(count, row.getLine());
                count++;
            }
        }
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}