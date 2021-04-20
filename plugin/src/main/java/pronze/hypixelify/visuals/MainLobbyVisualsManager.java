package pronze.hypixelify.visuals;

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
import org.screamingsandals.bedwars.events.PlayerJoinedEventImpl;
import org.screamingsandals.bedwars.events.PlayerLeaveEventImpl;
import org.screamingsandals.bedwars.lib.event.EventManager;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.player.PlayerManager;
import org.screamingsandals.bedwars.statistics.PlayerStatisticManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnDestroy;
import pronze.lib.core.annotations.OnInit;
import pronze.lib.scoreboards.Scoreboard;

import java.util.*;

@AutoInitialize
public class MainLobbyVisualsManager implements Listener {
    private final static String MAIN_LOBBY_OBJECTIVE = "bwa-mainlobby";
    private static Location location;
    private final Map<Player, Scoreboard> scoreboardMap = new HashMap<>();

    @OnInit
    public void registerSLibEvents() {
        EventManager.getDefaultEventManager().register(PlayerJoinedEventImpl.class, this::onBedWarsPlayerJoin, org.screamingsandals.bedwars.lib.event.EventPriority.LOWEST);
        EventManager.getDefaultEventManager().register(PlayerLeaveEventImpl.class, this::onBedWarsPlayerLeaveEvent, org.screamingsandals.bedwars.lib.event.EventPriority.HIGHEST);
    }

    public MainLobbyVisualsManager() {
        if (!SBAConfig.getInstance().getBoolean("main-lobby.enabled", false)) {
           return;
        }
        Core.registerListener(this);

        SBAUtil.readLocationFromConfig("main-lobby").ifPresentOrElse(location -> {
            MainLobbyVisualsManager.location = location;
            Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> Bukkit
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

                if (SBAHypixelify.getInstance().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    format = PlaceholderAPI.setPlaceholders(player, format);
                }
                e.setFormat(format);
            }
        }
    }

    @OnDestroy
    public void disable() {
        scoreboardMap.keySet().forEach(this::remove);
        scoreboardMap.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var player = e.getPlayer();

        Bukkit.getServer().getScheduler()
                .runTaskLater(SBAHypixelify.getInstance(), () -> {
                    if (hasMainLobbyObjective(player)) return;
                    if (isInWorld(player.getLocation()) && !PlayerManager.getInstance().isPlayerInGame(player.getUniqueId())) {
                        create(player);
                    }
                }, 3L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        final var player = e.getPlayer();
        if (isInWorld(player.getLocation()) && !scoreboardMap.containsKey(player)) {
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
        final var playerData = SBAHypixelify.getInstance().getPlayerWrapperService().get(player).orElseThrow();

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
                    final var playerStatistic  = PlayerStatisticManager
                            .getInstance()
                            .getStatistic(PlayerMapper.wrapPlayer(player));

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

    public void onBedWarsPlayerJoin(PlayerJoinedEventImpl e) {
        final var player = e.getPlayer().as(Player.class);
        remove(player);
    }

    public void onBedWarsPlayerLeaveEvent(PlayerLeaveEventImpl e) {
        final var player = e.getPlayer().as(Player.class);
        if (isInWorld(player.getLocation())) {
            Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> create(player), 3L);
        }
    }
}
