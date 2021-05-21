package pronze.hypixelify.visuals;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
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
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.statistics.PlayerStatisticManager;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.scoreboards.Scoreboard;

import java.util.*;

@Service
public class MainLobbyVisualsManager implements Listener {
    private final static String MAIN_LOBBY_OBJECTIVE = "bwa-mainlobby";
    private static Location location;
    private final Map<Player, Scoreboard> scoreboardMap = new HashMap<>();


    public MainLobbyVisualsManager() {
        if (!SBAConfig.getInstance().getBoolean("main-lobby.enabled", false)) {
           return;
        }
        SBAHypixelify.getInstance().registerListener(this);
        SBAUtil.readLocationFromConfig("main-lobby").ifPresentOrElse(location -> {
            MainLobbyVisualsManager.location = location;
            Bukkit.getScheduler().runTaskLater(SBAHypixelify.getPluginInstance(), () -> Bukkit
                    .getOnlinePlayers().forEach(this::create), 3L);
        }, () -> {
            disable();
            Bukkit.getServer().getLogger().warning("Could not find lobby world!");
        });
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
        if (!SBAConfig.getInstance().node("main-lobby","custom-chat").getBoolean(true)) return;
        final var player = e.getPlayer();
        final var db = SBAHypixelify.getInstance().getPlayerWrapperService().get(player).orElseThrow();

        if (SBAConfig.getInstance().node("main-lobby", "enabled").getBoolean(false)
                && MainLobbyVisualsManager.isInWorld(e.getPlayer().getLocation())) {

            var chatFormat = LanguageService
                    .getInstance()
                    .get(MessageKeys.MAIN_LOBBY_CHAT_FORMAT)
                    .toString();

            if (chatFormat != null) {
                var format = chatFormat
                        .replace("%level%", String.valueOf(db.getLevel()))
                        .replace("%name%", e.getPlayer().getName())
                        .replace("%message%", e.getMessage())
                        .replace("%color%", ShopUtil.ChatColorChanger(e.getPlayer()));

                if (SBAHypixelify.getPluginInstance().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    format = PlaceholderAPI.setPlaceholders(player, format);
                }
                e.setFormat(format);
            }
        }
    }

    @OnPreDisable
    public void disable() {
        Set.copyOf(scoreboardMap.keySet()).forEach(this::remove);
        scoreboardMap.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var player = e.getPlayer();

        Bukkit.getServer().getScheduler()
                .runTaskLater(SBAHypixelify.getPluginInstance(), () -> {
                    if (hasMainLobbyObjective(player)) return;
                    if (isInWorld(player.getLocation()) && !Main.isPlayerInGame(player)) {
                        create(player);
                    }
                }, 3L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        final var player = e.getPlayer();
        if (player.isOnline() && isInWorld(player.getLocation()) && !scoreboardMap.containsKey(player)) {
            create(player);
        } else {
            remove(player);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    public void create(Player player) {
        final var playerData = SBAHypixelify
                .getInstance()
                .getPlayerWrapperService()
                .get(player)
                .orElseThrow();

        if (SBAConfig.getInstance().node("main-lobby", "tablist-modifications").getBoolean()) {
            var header = LanguageService
                    .getInstance()
                    .get(MessageKeys.MAIN_LOBBY_TABLIST_HEADER)
                    .toComponent();

            var footer = LanguageService
                    .getInstance()
                    .get(MessageKeys.MAIN_LOBBY_TABLIST_FOOTER)
                    .toComponent();

            playerData.sendPlayerListHeaderAndFooter(header, footer);
        }

        var title = LanguageService
                .getInstance()
                .get(MessageKeys.MAIN_LOBBY_SCOREBOARD_TITLE)
                .toString();

        var lines = LanguageService
                .getInstance()
                .get(MessageKeys.MAIN_LOBBY_SCOREBOARD_LINES)
                .toStringList();

        final var scoreboard = Scoreboard.builder()
                .animate(false)
                .player(player)
                .title(title)
                .displayObjective(MAIN_LOBBY_OBJECTIVE)
                .updateInterval(20L)
                .lines(lines)
                .placeholderHook(hook -> {
                    final var bar = playerData.getCompletedBoxes();
                    final var progress = playerData.getStringProgress();
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(player);

                    return hook
                            .getLine()
                            .replace("%kills%", String.valueOf(playerStatistic.getKills()))
                            .replace("%beddestroys%", String.valueOf(playerStatistic.getDestroyedBeds()))
                            .replace("%deaths%", String.valueOf(playerStatistic.getDeaths()))
                            .replace("%level%", "§7" + playerData.getLevel() + "✫")
                            .replace("%progress%", progress)
                            .replace("%bar%", bar)
                            .replace("%wins%", String.valueOf(playerStatistic.getWins()))
                            .replace("%kdr%", String.valueOf(playerStatistic.getKD()));
                }).build();

        scoreboardMap.put(player, scoreboard);
    }

    public void remove(Player player) {
        if (player == null) return;
        final var scoreboard = scoreboardMap.get(player);
        if (scoreboard != null) {
            scoreboard.destroy();
            scoreboardMap.remove(player);
        }
        if (hasMainLobbyObjective(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        if (SBAConfig.getInstance().node("main-lobby", "tablist-modifications").getBoolean()) {
            PlayerMapper.wrapPlayer(player).sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        }
    }

    @EventHandler
    public void onBedWarsPlayerJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        remove(player);
    }

    @EventHandler
    public void onBedWarsPlayerLeaveEvent(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        Tasker.build(() -> {
            if (isInWorld(player.getLocation()) && player.isOnline()) {
                create(player);
            }
        }).delay(1, TaskerTime.SECONDS).start();
    }
}
