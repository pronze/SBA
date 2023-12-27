package io.github.pronze.lib.pronzelib.scoreboards.scoreboardr.board;
//https://raw.githubusercontent.com/RienBijl/Scoreboard-revision/master/src/main/java/rien/bijl/Scoreboard/r/Board/BoardPlayer.java
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;

public class BoardPlayer {

    private final Player player;
    private ConfigBoard configBoard;
    private boolean enabled = true;
    public boolean worldLock = false;

    private BoardPlayer(Player player)
    {
        this.player = player;
        BoardPlayer.map.put(player, this);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!this.enabled) {
            configBoard.unhookPlayer(player);
        } else {
            configBoard.hookPlayer(player);
        }
    }

    public void lock()
    {
        configBoard.unhookPlayer(player);
        this.worldLock = true;
    }

    public void unlock() {
        worldLock = false;

        if (isEnabled()) {
            configBoard.hookPlayer(player);
        }
    }

    public void attachConfigBoard(ConfigBoard board) {
        if (configBoard != null) {
            configBoard.unhookPlayer(player);
        }
        configBoard = board;
        configBoard.hookPlayer(player);
    }

    public void kill() {
        configBoard.unhookPlayer(player);
        map.remove(player);
    }

    public static BoardPlayer getBoardPlayer(Player player)
    {
        if (map.containsKey(player)) {
            return map.get(player);
        }

        return new BoardPlayer(player);
    }

    public static Collection<BoardPlayer> allBoardPlayers() {
        return map.values();
    }


    private static HashMap<Player, BoardPlayer> map = new HashMap<>();

}