package org.pronze.hypixelify.listener;
import java.text.SimpleDateFormat;
import java.util.*;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.utils.ScoreboardUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

public class LobbyScoreboard implements Listener {
    private String title = "";
    private String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
        return format.format(date);
    }

    public static String format(String format){
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    public static List<String> listcolor(List<String> list) {
        List<String> clist = new ArrayList<>();
        for (String l : list)
            clist.add(ChatColor.translateAlternateColorCodes('&', l));
        return clist;
    }

    public LobbyScoreboard() {
        (new BukkitRunnable() {
            int i = 0;

            int tc = 0;

            public void run() {
               i--;
                if (i <= 0) {
                    i = Hypixelify.getConfigurator().getInt("lobby-scoreboard.interval", 2);
                    title = format(Hypixelify.getConfigurator().getStringList("lobby-scoreboard.title").get(tc));
                    tc++;
                    if (tc >= Hypixelify.getConfigurator().getStringList("lobby-scoreboard.title").size())
                        tc = 0;
                }
            }
        }).runTaskTimer(Hypixelify.getInstance(), 0L, 1L);
    }

    @EventHandler
    public void onPlayerJoin(BedwarsPlayerJoinedEvent e) {
        if (!Hypixelify.getConfigurator().getBoolean("lobby-scoreboard.enabled", true))
            return;
        final Game game = e.getGame();
        final Player player = e.getPlayer();
        Hypixelify.getInstance().getConfig().set("lobby-scoreboard.content", getLine(player, game));
        final int tc = 0;

        (new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (player.isOnline()  && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player) &&
                        e.getGame().getStatus() == GameStatus.WAITING) {
                    i--;
                    if (i <= 0) {
                        i = Hypixelify.getConfigurator().getInt("lobby-scoreboard.interval", 2);
                        updateScoreboard(player, game, tc);
                    }
                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(Hypixelify.getInstance(), 0L, 1L);
    }

    private void updateScoreboard(Player player, Game game, int tc) {
        List<String> ncelements = new ArrayList<>();
        ncelements.add(title.replace("{game}", game.getName()));
        Hypixelify.getInstance().getConfig().set("lobby-scoreboard.title", title);
        ncelements.addAll(getLine(player, game));
        ncelements = elementsPro(ncelements);
        if (ncelements.size() < 16) {
            int es = ncelements.size();
            for (int i = 0; i < 16 - es; i++)
                ncelements.add(1, null);
        }
        String[] scoreboardelements = ncelements.toArray(new String[0]);
        ScoreboardUtil.setLobbyScoreboard(player, scoreboardelements, game);
    }

    private List<String> getLine(Player player, Game game) {
        List<String> line = new ArrayList<>();
        String state = Hypixelify.getConfigurator().getString("lobby-scoreboard.state.waiting", "§fWaiting...");
        String countdown = "null";
        int needplayers = game.getMinPlayers() - game.getConnectedPlayers().size();
        needplayers = (needplayers < 0) ? 0 : needplayers;
        int s = game.getAvailableTeams().get(1).getMaxPlayers();
        String mode = s +"v" +s +"v" + s + "v" +s;

        if (game.getLobbyCountdown() != 0 && game.countConnectedPlayers() >= game.getMinPlayers()) {
            String time = Main.getGame(game.getName()).getFormattedTimeLeft();
            String[] units = time.split(":");
            int seconds = Integer.parseInt(units[1]) + 1;
            state = Hypixelify.getConfigurator().getString("lobby-scoreboard.state.countdown", "&fStarting in &a{countdown}s").replace("{countdown}",String.valueOf(seconds));
        }
        for (String li : Hypixelify.getConfigurator().getStringList("lobby_scoreboard.lines")) {
            String l = li.replace("{date}", getDate()).replace("{state}", state).replace("{game}", game.getName())
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

    private List<String> elementsPro(List<String> lines) {
        ArrayList<String> nclines = new ArrayList<>();
        for (String ls : lines) {
            String l = ls;
            if (l != null) {
                if (nclines.contains(l)) {
                    for (int i = 0; i == 0; ) {
                        l = l + "§r";
                        if (!nclines.contains(l)) {
                            nclines.add(l);
                            break;
                        }
                    }
                    continue;
                }
                nclines.add(l);
                continue;
            }
            nclines.add(l);
        }
        return nclines;
    }
}
