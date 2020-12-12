package io.pronze.hypixelify.listener;
import java.text.SimpleDateFormat;
import java.util.*;

import io.pronze.hypixelify.Configurator;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.utils.ScoreboardUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

public class LobbyScoreboard implements Listener {



    private String title = "";
    private final String countdown_message;
    private final boolean isEnabled;
    private final List<String> lobby_scoreboard_lines;
    private final String date;
    private final List<Player> players = new ArrayList<>();
    private BukkitTask updateTask;

    public static String format(String format){
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    public static List<String> listColor(List<String> list) {
        List<String> cList = new ArrayList<>();
        for (String l : list)
            cList.add(ChatColor.translateAlternateColorCodes('&', l));
        return cList;
    }

    public LobbyScoreboard() {
        date = new SimpleDateFormat(Configurator.date).format(new Date());
        List<String> lobby_scoreboard = SBAHypixelify.getConfigurator()
                .getStringList("lobby-scoreboard.title");

        lobby_scoreboard_lines = SBAHypixelify.getConfigurator()
                .getStringList("lobby_scoreboard.lines");

        countdown_message = format(SBAHypixelify.getConfigurator().config
                .getString("lobby-scoreboard.state.countdown", "&fStarting in &a{countdown}s"));
        isEnabled = SBAHypixelify.getConfigurator().config
                .getBoolean("lobby-scoreboard.enabled", true);

        if(!isEnabled){
            disable();
            return;
        }

        cancelTask();

        updateTask = new BukkitRunnable() {
            int tc = 0;
            final BedwarsAPI bedwarsAPI = BedwarsAPI.getInstance();

            public void run() {
                    title = lobby_scoreboard.get(tc);
                    tc++;
                    if (tc >= lobby_scoreboard.size())
                        tc = 0;

                    for (Player player : new ArrayList<>(players)) {
                        if (player == null || !player.isOnline()) continue;

                        final Game game = bedwarsAPI.getGameOfPlayer(player);

                        if (game != null && game.getStatus() == GameStatus.WAITING) {
                            updateScoreboard(player, game);
                        }
                    }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 2L);
    }

    @EventHandler
    public void onPlayerJoin(BedwarsPlayerJoinedEvent e) {
        if (!isEnabled)
            return;

        final Player player = e.getPlayer();
        players.add(player);
    }

    @EventHandler
    public void onPlayerLeave(BedwarsPlayerLeaveEvent e){
        players.remove(e.getPlayer());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        final String pluginName = e.getPlugin().getName();
        if (pluginName.equalsIgnoreCase(SBAHypixelify.getInstance().getName())) {
            disable();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        players.remove(e.getPlayer());
    }

    private void updateScoreboard(Player player, Game game) {
        List<String> ncelements = new ArrayList<>();
        ncelements.add(title.replace("{game}", game.getName()));
        ncelements.addAll(getLine(player, game));
        ncelements = makeElementsUnique(ncelements);
        if (ncelements.size() < 16) {
            int es = ncelements.size();
            for (int i = 0; i < 16 - es; i++)
                ncelements.add(1, null);
        }
        String[] scoreboardElements = ncelements.toArray(new String[0]);
        ScoreboardUtil.setLobbyScoreboard(player, scoreboardElements, game);
    }

    private List<String> getLine(Player player, Game game) {
        final List<String> line = new ArrayList<>();
        String state = SBAHypixelify.getConfigurator().getString("message.waiting"
                , "§fWaiting...");

        String countdown = "null";
        int needplayers = game.getMinPlayers() - game.getConnectedPlayers().size();
        needplayers = Math.max(needplayers, 0);
        int s = Configurator.game_size.getOrDefault(game.getName(), 4);
        String mode;
        switch(s){
            case 1:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.solo-prefix", "Solo");
                break;
            case 2:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.doubles-prefix", "Doubles");
                break;
            case 3:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.triples-prefix", "Triples");
                break;
            case 4:
                mode = SBAHypixelify.getConfigurator().getString("lobby-scoreboard.squads-prefix", "Squads");
                break;
            default:
                mode = s +"v" +s +"v" + s + "v" +s;
        }

        if (game.countConnectedPlayers() >= game.getMinPlayers() && game.getStatus() == GameStatus.WAITING) {
            String time = Main.getGame(game.getName()).getFormattedTimeLeft();
            if(!time.contains("0-1")) {
                String[] units = time.split(":");
                int seconds = Integer.parseInt(units[1]) + 1;
                state = countdown_message.replace("{countdown}", String.valueOf(seconds));
            }
        }

        for (String li : lobby_scoreboard_lines) {
            String l = li
                    .replace("{date}", date).replace("{state}", state).replace("{game}", game.getName())
                    .replace("{players}", String.valueOf(game.getConnectedPlayers().size()))
                    .replace("{maxplayers}", String.valueOf(game.getMaxPlayers()))
                    .replace("{minplayers}", String.valueOf(game.getMinPlayers())).replace("{needplayers}", String.valueOf(needplayers))
                    .replace("{countdown}", countdown)
                    .replace("{mode}", mode);
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
                l = PlaceholderAPI.setPlaceholders(player, l);
            line.add(l);
        }
        return line;
    }



    private List<String> makeElementsUnique(List<String> lines) {
        final ArrayList<String> sbLines = new ArrayList<>();
        lines.forEach(ls -> {
            if (ls == null) return;

            String l = ls;
            while (sbLines.contains(l)) {
                l = l + "§r";
            }

            sbLines.add(l);
        });
        return sbLines;
    }

    public void cancelTask(){
        try{
            if(updateTask != null && !updateTask.isCancelled()) {
                updateTask.cancel();
            }
        } catch (Throwable t){
            t.printStackTrace();
        }
        updateTask = null;
    }

    public void disable() {
        cancelTask();
        HandlerList.unregisterAll(this);
    }
}
