package pronze.hypixelify.utils;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.packets.WrapperPlayServerScoreboardScore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ScoreboardUtil {
    public static final String GAME_OBJECTIVE_NAME = "bwa-game";
    public static final String LOBBY_OBJECTIVE_NAME = "bwa-lobby";
    public static final String TAG_OBJECTIVE_NAME = "bwa-tag";
    public static final String TAB_OBJECTIVE_NAME = "bwa-tab";
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##");

    private static final Map<Player, Map<Player, Integer>> player_health = new HashMap<>();


    public static void removePlayer(Player player) {
        player_health.remove(player);
    }

    public static void updateCustomObjective(Player p, Game game) {
        if (!SBAHypixelify.getInstance().getServer().getPluginManager().isPluginEnabled("ProtocolLib") || Main.isLegacy()) return;

        if (!player_health.containsKey(p))
            player_health.put(p, new HashMap<>());

        final var map = player_health.get(p);

        game.getConnectedPlayers()
                .forEach(pl -> {
                    var playerHealth = Integer.parseInt(DECIMAL_FORMAT.format(pl.getHealth()));
                    if (map.getOrDefault(pl, 0) != playerHealth) {
                        if (SBAHypixelify.getConfigurator().config.getBoolean("game.tag-health", true)) {

                            try {
                                final var packet = new WrapperPlayServerScoreboardScore();
                                packet.setValue(playerHealth);
                                packet.setScoreName(pl.getName());
                                packet.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);
                                packet.setObjectiveName(TAG_OBJECTIVE_NAME);
                                packet.sendPacket(p);
                            } catch (Exception e) {
                                SBAHypixelify.getExceptionManager().handleException(e);
                            }
                        }

                        if (SBAHypixelify.getConfigurator().config.getBoolean("game.tab-health", true)) {
                            try {
                                final var packet = new WrapperPlayServerScoreboardScore();
                                packet.setValue(playerHealth);
                                packet.setScoreName(pl.getName());
                                packet.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);
                                packet.setObjectiveName(TAB_OBJECTIVE_NAME);
                                packet.sendPacket(p);
                            } catch (Exception e) {
                                SBAHypixelify.getExceptionManager().handleException(e);
                            }
                        }
                        map.put(pl, playerHealth);
                    }
                });
    }

}
