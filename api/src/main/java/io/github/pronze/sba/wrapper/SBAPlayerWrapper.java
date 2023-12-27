package io.github.pronze.sba.wrapper;

import com.google.common.base.Strings;
import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.Permissions;
import io.github.pronze.sba.data.ToggleableSetting;
import lombok.Getter;
import lombok.Setter;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.spectator.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@Getter
@Setter
public class SBAPlayerWrapper extends org.screamingsandals.lib.player.ExtendablePlayer {
    private int shoutCooldown;
    private final ToggleableSetting<PlayerSetting> settings;

    public SBAPlayerWrapper(Player player) {
        super(Players.wrapPlayer(player));

        this.shoutCooldown = 0;
        this.settings = ToggleableSetting.of(PlayerSetting.class);
    }
   

    public void sendMessage(String message) {
        getInstance().sendMessage(message);
    }

    public Player getInstance() {
        return as(Player.class);
    }

    public boolean canShout() {
        return shoutCooldown == 0;
    }

    public void shout(Component message) {
        System.out.println("Player shouting with cooldown "+shoutCooldown);
        if (shoutCooldown == 0) {
            //sendMessage(message);
            Collection<? extends Player> receivers = Bukkit.getOnlinePlayers();
            if(Main.isPlayerInGame(as(Player.class)))
                receivers=Main.getInstance().getGameOfPlayer(as(Player.class)).getConnectedPlayers();
            receivers.forEach(receiver->Players.wrapPlayer(receiver).sendMessage(message));
            if (getInstance().hasPermission(Permissions.SHOUT_BYPASS.getKey()) || getDefaultShoutCoolDownTime() == 0) {
                return;
            }

            shoutCooldown = getDefaultShoutCoolDownTime();

            new BukkitRunnable() {
                @Override
                public void run() {
                    shoutCooldown = shoutCooldown - 1;
                    if (shoutCooldown == 0) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(AddonAPI.getInstance().getJavaPlugin(), 0L, 20L);
        } else {
            AddonAPI
                    .getInstance()
                    .getLanguageService()
                    .get(MessageKeys.MESSAGE_SHOUT_WAIT)
                    .replace("%seconds%", String.valueOf(getShoutCooldown()))
                    .send(this);
        }
    }

    public int getXP() {
        var statistic = Main
                .getPlayerStatisticsManager()
                .getStatistic(this.getInstance());

        if (statistic == null) {
            statistic = Main.getPlayerStatisticsManager().loadStatistic(getUuid());
            if (statistic != null) {
                return statistic.getScore();
            }
            return 1;
        }
        return statistic.getScore();
    }

    public int getLevel() {
        var xp = getXP();
        if (xp < getTotalXPToLevelUp()) {
            return 1;
        }
        return 1 + (xp / getTotalXPToLevelUp());
    }

    public String getProgress() {
        var maxLimit  = getTotalXPToLevelUp();

        final var format = AddonAPI
                .getInstance()
                .getConfigurator()
                .getString("main-lobby.progress-format", "§b%progress%§7/§a%total%")
                .replace("%total%", round(maxLimit));

        int progress = getXP() - ((getLevel() - 1) * maxLimit);
        if (progress <= 0) {
            progress = 0;
        }
        return format.replace("%progress%", round(progress));
    }

    public int getIntegerProgress() {
        return (int)(((getXP() - ((getLevel() - 1) * getTotalXPToLevelUp())) / (double)getTotalXPToLevelUp()) * 100);
    }

    public String getCompletedBoxes() {
        int progress = getIntegerProgress();
        if (progress < 1)
            progress = 1;

        int numberOfBoxesFilled = progress / 10;
        return "&7[&b" + Strings.repeat("■", numberOfBoxesFilled)
                + "§7" + Strings.repeat("■", 10 - numberOfBoxesFilled) + "&7]";
    }

    protected static String round(double toRound) {
        if (toRound >= 1000.0D) {
            var bd = new BigDecimal(String.valueOf(toRound / 1000));
            bd = bd.setScale(1, RoundingMode.HALF_DOWN);
            return bd.doubleValue() + "k";
        }
        return String.valueOf(toRound);
    }

    protected static String round(int toRound) {
        return round((double) toRound);
    }

    public static int getTotalXPToLevelUp() {
        return AddonAPI
                .getInstance()
                .getConfigurator()
                .getInt("player-statistics.xp-to-level-up", 5000);
    }

    protected static int getDefaultShoutCoolDownTime() {
        return AddonAPI.getInstance().getConfigurator().getInt("shout.time-out", 60);
    }
}