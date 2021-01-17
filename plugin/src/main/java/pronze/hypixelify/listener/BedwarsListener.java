package pronze.hypixelify.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.GameStatus;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.game.Arena;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ScoreboardUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.scoreboards.Scoreboard;
import pronze.lib.scoreboards.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;
import static pronze.hypixelify.lib.lang.I.i18n;

public class BedwarsListener implements Listener {

    private final Map<UUID, BukkitTask> runnableCache = new HashMap<>();

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        final var game = e.getGame();
        final var arena = new Arena(game);
        SBAHypixelify.addArena(arena);
        arena.onGameStarted(e);
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final var plugin = event.getPlugin().getName();
        final var pluginManager = Bukkit.getServer().getPluginManager();

        if (plugin.equalsIgnoreCase(Main.getInstance().getName())) {
            pluginManager.disablePlugin(SBAHypixelify.getInstance());
            pluginManager.enablePlugin(SBAHypixelify.getInstance());
        }
    }


    @EventHandler
    public void onPreRebuild(BedwarsPreRebuildingEvent e) {
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            arena.onPreRebuildingEvent();
        }
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            arena.onTargetBlockDestroyed(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostRebuildingEvent(BedwarsPostRebuildingEvent e) {
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            SBAHypixelify.removeArena(game.getName());
        }
    }


    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if (arena != null)
            arena.onOver(e);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        final var game = Main.getGame(e.getGame().getName());
        final var task = runnableCache.get(player.getUniqueId());
        if (task != null) {
            SBAUtil.cancelTask(task);
        }

        if (game.getStatus() == GameStatus.WAITING) {
            runnableCache.put(player.getUniqueId(), new BukkitRunnable() {
                int buffer = 1; //fixes the bug where it constantly shows will start in 1 second

                public void run() {
                    if (player.isOnline() &&
                            game.getConnectedPlayers().contains(player) &&
                            game.getStatus() == GameStatus.WAITING) {

                        if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                            String time = game.getFormattedTimeLeft();

                            if (!time.contains("0-1")) {
                                String[] units = time.split(":");
                                int seconds = Integer.parseInt(units[1]) + 1;
                                if (buffer == seconds) return;
                                buffer = seconds;
                                if (seconds <= 10) {
                                    String message = i18n("game-starts-in")
                                            .replace("{seconds}", String.valueOf(seconds));

                                    message = seconds == 1 ? message
                                            .replace("seconds", "second") : message;
                                    player.sendMessage(message);
                                    sendTitle(player, ShopUtil
                                            .translateColors("&c" + seconds), "", 0, 20, 0);
                                }
                            }
                        }
                    } else {
                        this.cancel();
                        runnableCache.remove(player.getUniqueId());
                    }
                }
            }.runTaskTimer(SBAHypixelify.getInstance(), 3L, 20L));
        }

        /* Joined as spectator, let's give him a scoreboard*/
        else if (game.getStatus() == GameStatus.RUNNING) {
            final var arena = SBAHypixelify.getArena(game.getName());
            if (arena != null) {
                final var scoreboard = arena.getScoreboard();
                if (scoreboard != null) {
                    scoreboard.createBoard(player);
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());

        if (arena != null) {
            final var scoreboard = arena.getScoreboard();
            if (scoreboard != null) {
                scoreboard.remove(player);
            }
        }
        if (task != null) {
            SBAUtil.cancelTask(task);
        }
        runnableCache.remove(player.getUniqueId());

        ScoreboardUtil.removePlayer(player);
        SBAUtil.removeScoreboardObjective(player);
        ScoreboardManager.getInstance().fromCache(player.getUniqueId()).ifPresent(Scoreboard::destroy);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @EventHandler
    public void onBedWarsPlayerKilledEvent(BedwarsPlayerKilledEvent e) {
        final var game = e.getGame();
        final var arena = SBAHypixelify.getArena(game.getName());
        if (arena != null) {
            arena.onBedWarsPlayerKilled(e);
        }
    }

}
