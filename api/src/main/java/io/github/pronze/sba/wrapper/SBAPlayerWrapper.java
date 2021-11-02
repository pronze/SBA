package io.github.pronze.sba.wrapper;

import com.google.common.base.Strings;
import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.Permissions;
import io.github.pronze.sba.data.ToggleableSetting;
import io.github.pronze.sba.wrapper.game.GameWrapper;
import io.github.pronze.sba.lang.LangKeys;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.ExtendablePlayerWrapper;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Getter
@Setter
public class SBAPlayerWrapper extends ExtendablePlayerWrapper {
    private int shoutCooldown;
    private final ToggleableSetting<PlayerSetting> settings;

    public static SBAPlayerWrapper of(Player player) {
        return new SBAPlayerWrapper(PlayerMapper.wrapPlayer(player));
    }

    public SBAPlayerWrapper(@NotNull PlayerWrapper playerWrapper) {
        super(playerWrapper);
        // default values
        this.shoutCooldown = 0;
        this.settings = ToggleableSetting.of(PlayerSetting.class);
    }

    public int getEntityId() {
        return asBukkitPlayer().getEntityId();
    }

    public void leaveFromGame() {
        final var game = Main.getInstance().getGameOfPlayer(asBukkitPlayer());
        if (game != null) {
            game.leaveFromGame(asBukkitPlayer());
        }
    }

    public GameWrapper getGame() {
        final var game = Main.getInstance().getGameOfPlayer(asBukkitPlayer());
        if (game == null) {
            return null;
        }
        return GameWrapper.of(game);
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
        if (shoutCooldown == 0) {
            sendMessage(message);
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
            sendMessage(Message.of(LangKeys.MESSAGE_SHOUT_WAIT)
                    .placeholder("seconds", String.valueOf(getShoutCooldown()))
                    .asComponent());
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
        return ((getXP() - ((getLevel() - 1) * getTotalXPToLevelUp())) / getTotalXPToLevelUp()) * 100;
    }

    public String getCompletedBoxes() {
        int progress = getIntegerProgress();
        if (progress < 1)
            progress = 1;

        int numberOfBoxesFilled = progress / 10;
        return "§7[§b" + Strings.repeat("■", numberOfBoxesFilled)
                + "§7" + Strings.repeat("■", 10 - numberOfBoxesFilled) + "]";
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

    public Player asBukkitPlayer() {
        return as(Player.class);
    }

    protected static int getDefaultShoutCoolDownTime() {
        return AddonAPI.getInstance().getConfigurator().getInt("shout.time-out", 60);
    }

    public void sendTitle(@NotNull String title, @NotNull String subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(AdventureHelper.toComponent(title), AdventureHelper.toComponent(subTitle), fadeIn, stay, fadeOut);
    }

    public void sendTitle(@NotNull Component title, @NotNull Component subtitle, int fadeIn, int stay, int fadeOut) {
        var titleComponent = net.kyori.adventure.title.Title.title(
                title,
                subtitle,
                Title.Times.of(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        );
        showTitle(titleComponent);
    }

    public boolean isInGame() {
        return BedWarsAPIWrapper.isPlayerInGame(this);
    }
}