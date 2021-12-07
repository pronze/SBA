package io.github.pronze.sba.game;

import com.google.common.base.Strings;
import io.github.pronze.sba.config.SBAConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.statistics.PlayerStatistic;
import org.screamingsandals.lib.player.ExtendablePlayerWrapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GamePlayerImpl extends ExtendablePlayerWrapper {
    private final PlayerStatistic statistic;

    public GamePlayerImpl(PlayerWrapper wrappedObject) {
        super(wrappedObject);
        var statistic = Main
                .getPlayerStatisticsManager()
                .getStatistic(wrappedObject.as(Player.class));

        if (statistic == null) {
            statistic = Main.getPlayerStatisticsManager().loadStatistic(getUuid());
        }
        this.statistic = statistic;
    }


    public void destroy() {

    }

    public boolean isInGame() {
        return Main.isPlayerInGame(this.as(Player.class));
    }

    public int getTotalScore() {
        return statistic.getScore();
    }

    public int getLevel() {
        var totalScore = getTotalScore();
        if (totalScore < getTotalXPToLevelUp()) {
            return 1;
        }
        return (totalScore / getTotalXPToLevelUp()) + 1;
    }

    @NotNull
    public String getFormattedProgress() {
        var maxLimit  = getTotalXPToLevelUp();

        final var format =  ServiceManager.get(SBAConfig.class)
                .node("main-lobby", "progress-format")
                .getString("§b%progress%§7/§a%total%")
                .replace("%total%", roundAndFormat(maxLimit));

        return format.replace("%progress%", roundAndFormat(getProgress()));
    }

    public int getProgress() {
        int progress = getTotalScore();
        if (getLevel() > 1) {
            progress = progress / ((getTotalXPToLevelUp()) * getLevel());
        }

        if (progress <= 0) {
            progress = 0;
        }

        return progress;
    }

    @NotNull
    public String getCompletedBoxes() {
        int progress = ((getTotalXPToLevelUp() - getProgress()) / getTotalXPToLevelUp()) * 10;
        if (progress < 0)
            progress = 0;

        return "§7[§b" + Strings.repeat("■", progress)
                + "§7" + Strings.repeat("■", 10 - progress) + "]";
    }

    public int getTotalXPToLevelUp() {
        return ServiceManager.get(SBAConfig.class)
                .node("player-statistics", "xp-to-level-up")
                .getInt(5000);
    }

    private static String roundAndFormat(double toRound) {
        if (toRound >= 1000.0D) {
            var bd = new BigDecimal(String.valueOf(toRound / 1000));
            bd = bd.setScale(1, RoundingMode.HALF_DOWN);
            return bd.doubleValue() + "k";
        }
        return String.valueOf(toRound);
    }
}
