package io.pronze.hypixelify.listener;

import com.google.common.base.Strings;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.wrapper.PlayerWrapper;
import io.pronze.hypixelify.message.Messages;
import io.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LobbyBoard extends AbstractListener {

    private static Location location;
    private final List<Player> players = new ArrayList<>();
    private final List<String> board_body;
    private final boolean lobbyChatOverride;

    public LobbyBoard() {
        board_body = SBAHypixelify.getConfigurator().config.getStringList("main-lobby.lines");
        lobbyChatOverride = SBAHypixelify.getConfigurator().config.getBoolean("main-lobby.custom-chat", true);


        try {
            location = new Location(Bukkit.getWorld(Objects.requireNonNull(SBAHypixelify.getConfigurator().config.getString("main-lobby.world"))),
                    SBAHypixelify.getConfigurator().config.getDouble("main-lobby.x"),
                    SBAHypixelify.getConfigurator().config.getDouble("main-lobby.y"),
                    SBAHypixelify.getConfigurator().config.getDouble("main-lobby.z"),
                    (float) SBAHypixelify.getConfigurator().config.getDouble("main-lobby.yaw"),
                    (float) SBAHypixelify.getConfigurator().config.getDouble("main-lobby.pitch")
            );
        } catch (Exception e) {
            e.printStackTrace();
            if (SBAHypixelify.getConfigurator().config.getBoolean("main-lobby.enabled", false)) {
                SBAHypixelify.getConfigurator().config.set("main-lobby.enabled", false);
                SBAHypixelify.getConfigurator().saveConfig();
                onDisable();
                Bukkit.getServer().getLogger().warning("Could not find lobby world!");
                return;
            }
        }


        Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(),
                () -> Bukkit.getOnlinePlayers().forEach(this::createBoard), 3L);

    }

    public static boolean isInWorld(Location loc) {
        try {
            return loc.getWorld().equals(location.getWorld());
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean hasMainLobbyObjective(Player player) {
        return player.getScoreboard().getObjective("bwa-mainlobby") != null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!lobbyChatOverride) return;
        final Player player = e.getPlayer();
        final PlayerWrapper db = SBAHypixelify.getWrapperService().getWrapper(player);

        if (SBAHypixelify.LobbyBoardEnabled() && LobbyBoard.isInWorld(e.getPlayer().getLocation())) {
            if (Messages.lobby_chat_format != null) {
                String format = Messages.lobby_chat_format
                        .replace("{level}", String.valueOf(db.getLevel()))
                        .replace("{name}", e.getPlayer().getName())
                        .replace("{message}", e.getMessage())
                        .replace("{color}", ShopUtil.ChatColorChanger(e.getPlayer()));

                if(SBAHypixelify.isPapiEnabled()){
                    format = PlaceholderAPI.setPlaceholders(player, format);
                }

                e.setFormat(format);
            }
        }
    }

    @Override
    public void onDisable() {
        players.forEach(pl -> {
            try {
                if (pl.getScoreboard().getObjective("bwa-mainlobby") != null) {
                    pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        players.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();

        Bukkit.getServer().getScheduler().runTaskLater(SBAHypixelify.getInstance(),
                () -> {
                    if (hasMainLobbyObjective(player)) return;

                    if (isInWorld(player.getLocation()) && !isInGame(player)) {
                        createBoard(player);
                    }
                }, 3L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();

        if (!isInWorld(player.getLocation())) {
            players.remove(player);
            if (player.getScoreboard() == null) return;

            if (hasMainLobbyObjective(player)) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                return;
            }
            return;
        }

        createBoard(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        players.remove(e.getPlayer());
    }


    public void createBoard(Player player) {
        if (players.contains(player)) return;


        final PlayerWrapper playerData = SBAHypixelify.getWrapperService().getWrapper(player);
        if (playerData == null) {
            SBAHypixelify.debug("Player data of player: " + player.getDisplayName() + " is null," +
                    "skipping scoreboard creation");
            return;
        }


        Scoreboard scoreboard = player.getScoreboard();

        String bar = " §7[" + playerData.getCompletedBoxes() + "]";


        String progress = null;
        try {
            int p = playerData.getIntegerProgress();
            if (p < 0)
                progress = "§b0§7/§a500";
            else
                progress = playerData.getProgress();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (progress == null)
            progress = "§b0§7/§a500";

        if (scoreboard == null || scoreboard.getObjective("bwa-mainlobby") == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj = scoreboard.registerNewObjective("bwa-mainlobby", "dummy");
            obj.setDisplayName("§e§lBED WARS");

            Objects.requireNonNull(scoreboard.getObjective("bwa-mainlobby")).setDisplaySlot(DisplaySlot.SIDEBAR);
            int i = 15;
            for (String s : board_body) {
                if (i == 0) break;
                try {
                    if (SBAHypixelify.isPapiEnabled())
                        s = PlaceholderAPI.setPlaceholders(player, s);
                } catch (Exception ignored) {

                }
                if (StringUtils.isEmpty(s))
                    s = Strings.repeat(" ", i);


                s = s
                        .replace("{kills}", String.valueOf(playerData.getKills()))
                        .replace("{beddestroys}", String.valueOf(playerData.getBedDestroys()))
                        .replace("{deaths}", String.valueOf(playerData.getDeaths()))
                        .replace("{level}", "§7" + playerData.getLevel() + "✫")
                        .replace("{progress}", progress)
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
    public void onBedWarsPlayerJoin(BedwarsPlayerJoinedEvent e) {
        players.remove(e.getPlayer());
    }


}
