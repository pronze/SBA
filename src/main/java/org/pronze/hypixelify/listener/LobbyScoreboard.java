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
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.utils.ScoreboardUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

public class LobbyScoreboard implements Listener {
    private String title = "";
    private final String countdown_message;
    private final boolean isEnabled;
    private final List<String> lobby_scoreboard_lines;
    private final String date;

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
        List<String> lobby_scoreboard = Hypixelify.getConfigurator().getStringList("lobby-scoreboard.title");
        lobby_scoreboard_lines = Hypixelify.getConfigurator().getStringList("lobby_scoreboard.lines");

        countdown_message = format(Hypixelify.getConfigurator().config.getString("lobby-scoreboard.state.countdown", "&fStarting in &a{countdown}s"));
        isEnabled = Hypixelify.getConfigurator().config.getBoolean("lobby-scoreboard.enabled", true);
        new BukkitRunnable() {
            int tc = 0;

            public void run() {
                    title = lobby_scoreboard.get(tc);
                    tc++;
                    if (tc >= lobby_scoreboard.size())
                        tc = 0;
            }
        }.runTaskTimer(Hypixelify.getInstance(), 0L, 2L);
    }

    @EventHandler
    public void onPlayerJoin(BedwarsPlayerJoinedEvent e) {
        if (!isEnabled)
            return;
        final Game game = e.getGame();
        final Player player = e.getPlayer();

        new BukkitRunnable() {

            public void run() {
                if (player.isOnline()  && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player) &&
                        e.getGame().getStatus() == GameStatus.WAITING) {
                        updateScoreboard(player, game);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Hypixelify.getInstance(), 0L, 2L);
    }

    private void updateScoreboard(Player player, Game game) {
        List<String> ncelements = new ArrayList<>();
        ncelements.add(title.replace("{game}", game.getName()));
        ncelements.addAll(getLine(player, game));
        ncelements = elementsPro(ncelements);
        if (ncelements.size() < 16) {
            int es = ncelements.size();
            for (int i = 0; i < 16 - es; i++)
                ncelements.add(1, null);
        }
        String[] scoreboardElements = ncelements.toArray(new String[0]);
        ScoreboardUtil.setLobbyScoreboard(player, scoreboardElements, game);
    }

    private List<String> getLine(Player player, Game game) {
        List<String> line = new ArrayList<>();
        String state = "§fWaiting...";
        String countdown = "null";
        int needplayers = game.getMinPlayers() - game.getConnectedPlayers().size();
        needplayers = Math.max(needplayers, 0);
        int s = Configurator.game_size.getOrDefault(game.getName(), 4);
        String mode;
        switch(s){
            case 1:
                mode = "Solo";
                break;
            case 2:
                mode = "Double";
                break;
            case 3:
                mode = "Triples";
                break;
            case 4:
                mode = "Squads";
                break;
            default:
                mode = s +"v" +s +"v" + s + "v" +s;
        }

        if (game.countConnectedPlayers() >= game.getMinPlayers() && game.getStatus().equals(GameStatus.WAITING)) {
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
