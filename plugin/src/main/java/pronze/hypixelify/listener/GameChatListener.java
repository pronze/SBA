package pronze.hypixelify.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.player.PlayerManager;
import pronze.hypixelify.config.SBAConfig;
import pronze.lib.core.annotations.AutoInitialize;

@AutoInitialize(listener = true)
public class GameChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final var player = event.getPlayer();
        if (PlayerManager.getInstance().isPlayerInGame(player.getUniqueId())) {
            final var game = PlayerManager
                    .getInstance()
                    .getGameOfPlayer(player.getUniqueId())
                    .orElseThrow();

            if (game.getStatus() == GameStatus.RUNNING) {
                if (SBAConfig.getInstance().node("chat-format", "game-chat", "enabled").getBoolean(true)) {
                    event.setCancelled(true);

                    final var bedwarsPlayer = PlayerManager
                            .getInstance()
                            .getPlayer(player.getUniqueId())
                            .orElseThrow();

                    String format;
                    final var allChatPrefix = SBAConfig
                            .getInstance()
                            .node("chat-format", "game-chat", "all-chat-prefix").getString();

                    if (bedwarsPlayer.isSpectator) {
                        format = SBAConfig
                                .getInstance()
                                .node("chat-format", "game-chat", "format-spectator").getString();
                    } else {


                        if (event.getMessage().startsWith(allChatPrefix)) {
                            format = SBAConfig
                                    .getInstance()
                                    .node("chat-format", "game-chat", "all-chat-format").getString();
                        } else {
                            format = SBAConfig
                                    .getInstance()
                                    .node("chat-format", "game-chat", "format").getString();
                        }

                        var colorName = game.getTeamOfPlayer(player).getColor().name().toUpperCase();
                        var color = TeamColor.fromApiColor(game.getTeamOfPlayer(player).getColor()).chatColor.toString();
                        format = format
                                .replace("%color%", color)
                                .replace("%team%", colorName);
                    }

                    var message = event.getMessage().replace(allChatPrefix, "");
                    if (message.startsWith(" ")) {
                        message = message.substring(1);
                    }

                    format = format
                            .replace("%player%", player.getName())
                            .replace("%message%", message);
                    if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        format = PlaceholderAPI.setPlaceholders(player, format);
                    }

                    String finalFormat = format;
                    game.getConnectedPlayers().forEach(gamePlayer -> gamePlayer.sendMessage(finalFormat));
                }
            } else if (game.getStatus() == GameStatus.WAITING) {
                if (SBAConfig.getInstance().node("chat-format", "lobby-chat", "enabled").getBoolean()) {
                    event.setCancelled(true);
                    var lobbyChatFormat = SBAConfig
                            .getInstance()
                            .node("chat-format", "lobby-chat", "format")
                            .getString();

                    var team = game.getTeamOfPlayer(player);
                    var teamColor = team != null ? TeamColor.fromApiColor(team.getColor()).chatColor.toString() : null;

                    lobbyChatFormat = lobbyChatFormat
                            .replace("%color%", teamColor == null ? "" : teamColor)
                            .replace("%message%", event.getMessage())
                            .replace("%player%", player.getName());

                    if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        lobbyChatFormat = PlaceholderAPI
                                .setPlaceholders(player, lobbyChatFormat);
                    }

                    String finalLobbyChatFormat = lobbyChatFormat;
                    game.getConnectedPlayers().forEach(gamePlayer -> gamePlayer.sendMessage(finalLobbyChatFormat));
                }
            }
        }
    }
}
