package io.github.pronze.sba.listener;

import io.github.pronze.sba.wrapper.PlayerSetting;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;

@Service
public class GameChatListener implements Listener {

    @OnPostEnable
    public void registerListener() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final var player = event.getPlayer();
        final var playerWrapper = PlayerMapper.wrapPlayer(player).as(SBAPlayerWrapper.class);

        if (playerWrapper.getSettings().isToggled(PlayerSetting.IN_PARTY)
                && playerWrapper.getSettings().isToggled(PlayerSetting.PARTY_CHAT_ENABLED)) {
            // PartyChatListener will take care of this.
            return;
        }

        if (Main.getInstance().isPlayerPlayingAnyGame(player)) {
            final var game = Main.getInstance().getGameOfPlayer(player);

            if (game.getStatus() == GameStatus.RUNNING) {
                if (SBAConfig.getInstance().node("chat-format", "game-chat", "enabled").getBoolean(true)) {
                    event.setCancelled(true);

                    final var bedwarsPlayer = Main.getPlayerGameProfile(player);

                    String format;
                    final var allChatPrefix = SBAConfig
                            .getInstance()
                            .node("chat-format", "game-chat", "all-chat-prefix").getString();

                    boolean all = false;
                    boolean spectator = false;
                    if (bedwarsPlayer.isSpectator) {
                        format = SBAConfig
                                .getInstance()
                                .node("chat-format", "game-chat", "format-spectator").getString();
                        spectator = true;
                    } else {
                        if (event.getMessage().startsWith(allChatPrefix)) {
                            format = SBAConfig
                                    .getInstance()
                                    .node("chat-format", "game-chat", "all-chat-format").getString();
                            all = true;
                        } else {
                            format = SBAConfig
                                    .getInstance()
                                    .node("chat-format", "game-chat", "format").getString();
                        }

                        var colorName = game.getTeamOfPlayer(player).getColor().name().toUpperCase();
                        var teamName = game.getTeamOfPlayer(player).getName();
                        var color = TeamColor.fromApiColor(game.getTeamOfPlayer(player).getColor()).chatColor
                                .toString();
                        format = format
                                .replace("%color%", color)
                                .replace("%color_name%", colorName)
                                .replace("%team%", teamName);
                    }

                    var message = event.getMessage().replace(allChatPrefix, "");
                    if (message.startsWith(" ")) {
                        message = message.substring(1);
                    }

                    format = format
                            .replace("%player%", player.getDisplayName() + ChatColor.RESET)
                            .replace("%message%", message);
                    if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        format = PlaceholderAPI.setPlaceholders(player, format);
                    }

                    final String finalFormat = format;

                    if (all)
                    {
                        game.getConnectedPlayers().forEach(gamePlayer -> gamePlayer.sendMessage(finalFormat));
                    }
                    else if (spectator)
                    {
                        game.getConnectedPlayers().forEach(gamePlayer -> {
                            if(Main.getPlayerGameProfile(gamePlayer).isSpectator)
                                gamePlayer.sendMessage(finalFormat);
                        });
                    }
                    else // Team
                    {
                        var teamName = game.getTeamOfPlayer(player).getName();
                        game.getConnectedPlayers().forEach(gamePlayer -> {
                            if(teamName.equals(game.getTeamOfPlayer(gamePlayer).getName())||
                                Main.getPlayerGameProfile(gamePlayer).isSpectator)
                                gamePlayer.sendMessage(finalFormat);
                        });
                    }
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
                            .replace("%player%", player.getDisplayName() + ChatColor.RESET);

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
