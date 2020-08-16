package org.pronze.hypixelify.listener;

import com.google.common.base.Strings;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.database.PlayerDatabase;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LobbyBoard extends AbstractListener {

    public static List<Player> players;
    private static Location location;
    private final List<String> lobby_scoreboard_lines;
    private int count = 0;
    private final List<String> board_body;
    private boolean lobbyChatOverride;

    public static boolean isInWorld(Location loc){
        return loc.getWorld().equals(location.getWorld());
    }

    public LobbyBoard() {
        players = new ArrayList<>();
        board_body = Hypixelify.getConfigurator().config.getStringList("main-lobby.lines");
        lobby_scoreboard_lines = Hypixelify.getConfigurator().getStringList("lobby-scoreboard.title");
        lobbyChatOverride = Hypixelify.getConfigurator().config.getBoolean("main-lobby.custom-chat", true);


        try {
            location = new Location(Bukkit.getWorld(Hypixelify.getConfigurator().config.getString("main-lobby.world")),
                    Hypixelify.getConfigurator().config.getDouble("main-lobby.x"),
                    Hypixelify.getConfigurator().config.getDouble("main-lobby.y"),
                    Hypixelify.getConfigurator().config.getDouble("main-lobby.z"),
                    (float) Hypixelify.getConfigurator().config.getDouble("main-lobby.yaw"),
                    (float) Hypixelify.getConfigurator().config.getDouble("main-lobby.pitch")
            );
        } catch (Exception e) {
            e.printStackTrace();
            if(Hypixelify.getConfigurator().config.getBoolean("main-lobby.enabled", false)){
                Hypixelify.getConfigurator().config.set("main-lobby.enabled", false);
                Hypixelify.getConfigurator().saveConfig();
                onDisable();
                return;
            }
        }

        if(!Bukkit.getOnlinePlayers().isEmpty()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player == null || !player.isOnline()) continue;

                        if(player.getWorld().getName().equalsIgnoreCase(location.getWorld().getName())){
                            createBoard(player);
                        }
                    }
                }
            }.runTaskLater(Hypixelify.getInstance(), 40L);
        }

         new BukkitRunnable() {
            public void run() {
                if(players != null) {
                    if (!players.isEmpty()) {
                        updateScoreboard();
                    }
                } else
                {
                    this.cancel();
                }
            }
        }.runTaskTimer(Hypixelify.getInstance(), 0L, 2L);

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if(!lobbyChatOverride) return;
        Player player = e.getPlayer();
        PlayerDatabase db = Hypixelify.getDatabaseManager().getDatabase(player);

        if(Hypixelify.LobbyBoardEnabled() && LobbyBoard.isInWorld(e.getPlayer().getLocation())) {
            e.setFormat(Messages.lobby_chat_format
                    .replace("{level}", String.valueOf(db.getLevel()))
                    .replace("{name}", e.getPlayer().getName())
                    .replace("{message}", e.getMessage())
                    .replace("{color}", ShopUtil.ChatColorChanger(e.getPlayer())));
        }
    }
    @Override
    public void onDisable() {
        for(Player player : players){
            if(player == null || !player.isOnline()) continue;
            if (player.getScoreboard().getObjective("bwa-mainlobby") != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
        players.clear();
        players = null;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

      new BukkitRunnable(){
            @Override
            public void run() {
                if(player.getScoreboard().getObjective("bwa-mainlobby") != null) return;

                if(player.getWorld().getName().equalsIgnoreCase(location.getWorld().getName())){
                    createBoard(player);
                }
            }
        }.runTaskLater(Hypixelify.getInstance(), 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        if (!player.getWorld().getName().equals(location.getWorld().getName())) {
            players.remove(player);
            if (player.getScoreboard() == null) return;

            if (player.getScoreboard().getObjective("bwa-mainlobby") != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                return;
            }
            return;
        }

        createBoard(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        players.remove(e.getPlayer());
    }


    public void createBoard(Player player) {
        if(players.contains(player)) return;

        if(Hypixelify.getDatabaseManager().getDatabase(player) == null) return;

        final PlayerDatabase  playerData = Hypixelify.getDatabaseManager().getDatabase(player);
        if(playerData == null) return;

        Scoreboard scoreboard = player.getScoreboard();

        String bar = " §7[" + playerData.getCompletedBoxes() + "]";

        if (scoreboard == null || scoreboard.getObjective("bwa-mainlobby") == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            scoreboard.registerNewObjective("bwa-mainlobby", "dummy");


            Objects.requireNonNull(scoreboard.getObjective("bwa-mainlobby")).setDisplaySlot(DisplaySlot.SIDEBAR);
            int i = 15;
            for (String s : board_body) {
                if(i == 0) continue;
                try {
                    if (Hypixelify.isPapiEnabled())
                        s = PlaceholderAPI.setPlaceholders(player, s);
                } catch (Exception ignored) {

                }
                if (s == "" || s == " " || s.isEmpty())
                    s = Strings.repeat(" ", i);


                s = s
                        .replace("{kills}", String.valueOf(playerData.getKills()))
                        .replace("{beddestroys}", String.valueOf(playerData.getBedDestroys()))
                        .replace("{deaths}", String.valueOf(playerData.getDeaths()))
                        .replace("{level}", "§7" + playerData.getLevel()+ "✫")
                        .replace("{progress}", playerData.getProgress())
                        .replace("{bar}", bar)
                        .replace("{wins}", String.valueOf(playerData.getWins()))
                        .replace("{k/d}", String.valueOf(playerData.getKD()));

                Objects.requireNonNull(scoreboard.getObjective("bwa-mainlobby")).getScore(s).setScore(i);
                i--;
            }
        }

        player.setScoreboard(scoreboard);
        players.add(player);
    }

    @EventHandler
    public void onBedwarsPlayerJoin(BedwarsPlayerJoinedEvent e){
        Player player = e.getPlayer();
        if(players != null && player != null)
            players.remove(player);
    }

    public void updateScoreboard(){
        count++;
        if (count >= lobby_scoreboard_lines.size()) {
            count = 0;
        }

        for (Player player : players) {
            if (player.getScoreboard().getObjective("bwa-mainlobby") == null) continue;
            Objects.requireNonNull(player.getScoreboard().getObjective("bwa-mainlobby")).setDisplayName(lobby_scoreboard_lines.get(count));
        }



    }




}
