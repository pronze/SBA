package io.github.pronze.sba.visuals;

import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerChatEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;

@Service
public class MainLobbyVisualsManager implements Listener {
    private final static String MAIN_LOBBY_OBJECTIVE = "bwa-mainlobby";

    private static Location location;

    private final Sidebar sidebar;

    public MainLobbyVisualsManager() {
        this.sidebar = Sidebar.of();
    }

    @OnPreDisable
    public void onPreDisable() {
        sidebar.destroy();
    }

    @OnPostEnable
    public void registerListener() {
        if (!SBAConfig.getInstance().getBoolean("main-lobby.enabled", false)) {
            return;
        }

        var title = Message.of(LangKeys.MAIN_LOBBY_SCOREBOARD_TITLE)
                .asComponent();
        sidebar.title(title);

        final var lines = Message.of(LangKeys.MAIN_LOBBY_SCOREBOARD_LINES)
                .placeholder("sba_version", SBA.getInstance().getVersion())
                .placeholder("kills", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getKills()));
                })
                .placeholder("beddestroys", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getDestroyedBeds()));
                })
                .placeholder("deaths", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getDeaths()));
                })
                .placeholder("level", playerWrapper -> {
                    final var sbaWrapper = playerWrapper.as(SBAPlayerWrapper.class);
                    return AdventureHelper.toComponent("§7" + sbaWrapper.getLevel() + "✫");
                })
                .placeholder("progress", playerWrapper -> {
                    final var sbaWrapper = playerWrapper.as(SBAPlayerWrapper.class);
                    return AdventureHelper.toComponent(sbaWrapper.getProgress());
                })
                .placeholder("bar", playerWrapper -> {
                    final var sbaWrapper = playerWrapper.as(SBAPlayerWrapper.class);
                    return AdventureHelper.toComponent(sbaWrapper.getCompletedBoxes());
                })
                .placeholder("wins", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getWins()));
                })
                .placeholder("kdr", playerWrapper -> {
                    final var playerStatistic  = Main
                            .getPlayerStatisticsManager()
                            .getStatistic(playerWrapper.as(Player.class));
                    return AdventureHelper.toComponent(String.valueOf(playerStatistic.getKD()));
                });

        sidebar.bottomLine(lines);
        sidebar.show();

        SBA.getInstance().registerListener(this);
        SBAUtil.readLocationFromConfig("main-lobby").ifPresentOrElse(location -> {
            MainLobbyVisualsManager.location = location;
            Bukkit.getScheduler().runTaskLater(SBA.getPluginInstance(), () -> Bukkit.getOnlinePlayers().forEach(this::create), 3L);
        }, () -> {
            Bukkit.getServer().getLogger().warning("Could not find lobby world!");
            HandlerList.unregisterAll(this);
        });
    }

    public static boolean isInWorld(Location loc) {
        return location != null && loc.getWorld().equals(location.getWorld());
    }

    public static boolean hasMainLobbyObjective(Player player) {
        return player.getScoreboard().getObjective(MAIN_LOBBY_OBJECTIVE) != null;
    }

    @OnEvent
    public void onChat(SPlayerChatEvent event) {
        if (!SBAConfig.getInstance().node("main-lobby","custom-chat").getBoolean(true)) {
            return;
        }

        final var player = event.getPlayer().as(Player.class);
        final var db = SBA
                .getInstance()
                .getPlayerWrapperService()
                .get(player)
                .orElseThrow();

        if (SBAConfig.getInstance().node("main-lobby", "enabled").getBoolean(false)
                && MainLobbyVisualsManager.isInWorld(player.getLocation())) {

            var chatFormat = Message.of(LangKeys.MAIN_LOBBY_CHAT_FORMAT)
                    .placeholder("level", String.valueOf(db.getLevel()))
                    .placeholder("name", player.getName())
                    .placeholder("message", event.getMessage())
                    .placeholder("color", ShopUtil.ChatColorChanger(player))
                    .asComponent();

            event.setFormat(AdventureHelper.toLegacy(chatFormat));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var player = e.getPlayer();

        Bukkit.getServer().getScheduler()
                .runTaskLater(SBA.getPluginInstance(), () -> {
                    if (hasMainLobbyObjective(player)) return;
                    if (isInWorld(player.getLocation()) && !Main.isPlayerInGame(player) && player.isOnline()) {
                        create(player);
                    }
                }, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        final var player = e.getPlayer();
        if (player.isOnline() && isInWorld(player.getLocation())) {
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
        final var wrapper = PlayerMapper.wrapPlayer(player);
        if (SBAConfig.getInstance().node("main-lobby", "tablist-modifications").getBoolean()) {
            var header = Message.of(LangKeys.MAIN_LOBBY_TABLIST_HEADER)
                    .placeholder("sba_version", SBA.getInstance().getVersion())
                    .asComponent();

            var footer = Message.of(LangKeys.MAIN_LOBBY_TABLIST_FOOTER)
                    .placeholder("sba_version", SBA.getInstance().getVersion())
                    .asComponent();

            wrapper.sendPlayerListHeaderAndFooter(header, footer);
        }

        if (!sidebar.getViewers().contains(wrapper)) {
            sidebar.addViewer(wrapper);
        }
    }

    public void remove(Player player) {
        if (player == null) {
            return;
        }

        final var wrapper = PlayerMapper.wrapPlayer(player);
        if (sidebar.getViewers().contains(wrapper)) {
            sidebar.removeViewer(wrapper);
        }

        if (SBAConfig.getInstance().node("main-lobby", "tablist-modifications").getBoolean()) {
            wrapper.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
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
                remove(player);
            }
        }).afterOneTick();
    }
}
