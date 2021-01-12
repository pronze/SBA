package pronze.hypixelify.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.scoreboards.Scoreboard;

import java.util.HashMap;
import java.util.Map;


public class MainLobbyBoard implements Listener {
    private final static String MAIN_LOBBY_OBJECTIVE = "bwa-mainlobby";
    private static Location location;
    private final Map<Player, Scoreboard> scoreboardMap = new HashMap<>();

    public MainLobbyBoard() {
        final var optionalLocation = SBAUtil.readLocationFromConfig("main-lobby");
        if (optionalLocation.isPresent()) {
            location = optionalLocation.get();
        } else {
            disable();
            Bukkit.getServer().getLogger().warning("Could not find lobby world!");
        }
        Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> Bukkit
                .getOnlinePlayers().forEach(this::createBoard), 3L);
    }

    public static boolean isInWorld(Location loc) {
        try {
            return loc.getWorld().equals(location.getWorld());
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasMainLobbyObjective(Player player) {
        return player.getScoreboard().getObjective(MAIN_LOBBY_OBJECTIVE) != null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!SBAHypixelify.getConfigurator().config
                .getBoolean("main-lobby.custom-chat", true)) return;
        final var player = e.getPlayer();
        final var db = SBAHypixelify.getWrapperService().getWrapper(player);

        if (SBAHypixelify.getConfigurator().config.getBoolean("main-lobby.enabled", false)
                && MainLobbyBoard.isInWorld(e.getPlayer().getLocation())) {

            if (SBAHypixelify.getConfigurator().getString("main-lobby.chat-format") != null) {
                String format = SBAHypixelify.getConfigurator()
                        .getString("main-lobby.chat-format")
                        .replace("{level}", String.valueOf(db.getLevel()))
                        .replace("{name}", e.getPlayer().getName())
                        .replace("{message}", e.getMessage())
                        .replace("{color}", ShopUtil.ChatColorChanger(e.getPlayer()));

                if (SBAHypixelify.getInstance().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    format = PlaceholderAPI.setPlaceholders(player, format);
                }
                e.setFormat(format);
            }
        }
    }

    protected void disable() {
        scoreboardMap.keySet().forEach(pl -> {
            if (hasMainLobbyObjective(pl)) {
                pl.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        });
        scoreboardMap.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var player = e.getPlayer();

        Bukkit.getServer().getScheduler()
                .runTaskLater(SBAHypixelify.getInstance(), () -> {
                    if (hasMainLobbyObjective(player)) return;
                    if (isInWorld(player.getLocation()) && !BedwarsAPI.getInstance()
                            .isPlayerPlayingAnyGame(player)) {
                        createBoard(player);
                    }
                }, 3L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        final var player = e.getPlayer();
        if (isInWorld(player.getLocation())) {
            createBoard(player);
        } else {
            final var scoreboard = scoreboardMap.get(player);
            if (scoreboard != null) {
                scoreboard.destroy();
                scoreboardMap.remove(player);
            }
            if (hasMainLobbyObjective(player)) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        final var player = e.getPlayer();
        final var scoreboard = scoreboardMap.get(player);
        if (scoreboard != null) {
            scoreboard.destroy();
            scoreboardMap.remove(player);
        }
    }

    public void createBoard(Player player) {
        final var playerData = SBAHypixelify.getWrapperService().getWrapper(player);
        final var scoreboard = Scoreboard.builder()
                .animate(false)
                .player(player)
                .async(true)
                .title(SBAHypixelify.getConfigurator().getString("main-lobby.title", "&e&lBED WARS"))
                .displayObjective(MAIN_LOBBY_OBJECTIVE)
                .updateInterval(20L)
                .lines(SBAHypixelify.getConfigurator().getStringList("main-lobby.lines"))
                .placeholderHook(hook -> {
                    final var bar = playerData.getCompletedBoxes();
                    final var progress = playerData.getStringProgress();
                    return hook.getLine()
                            .replace("{kills}", String.valueOf(playerData.getKills()))
                            .replace("{beddestroys}", String.valueOf(playerData.getBedDestroys()))
                            .replace("{deaths}", String.valueOf(playerData.getDeaths()))
                            .replace("{level}", "§7" + playerData.getLevel() + "✫")
                            .replace("{progress}", progress)
                            .replace("{bar}", bar)
                            .replace("{wins}", String.valueOf(playerData.getWins()))
                            .replace("{k/d}", String.valueOf(playerData.getKD()));
                }).build();

        scoreboardMap.put(player, scoreboard);
    }

    @EventHandler
    public void onBedWarsPlayerJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        final var scoreboard = scoreboardMap.get(player);
        if (scoreboard != null) {
            scoreboard.destroy();
            scoreboardMap.remove(player);
        }
        if (hasMainLobbyObjective(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedWarsPlayerLeaveEvent(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        if (isInWorld(player.getLocation())) {
            Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> createBoard(player), 3L);
        }
    }
}
