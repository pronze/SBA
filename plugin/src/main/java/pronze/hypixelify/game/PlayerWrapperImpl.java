package pronze.hypixelify.game;

import com.google.common.base.Strings;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.audience.Audience;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.statistics.PlayerStatisticManager;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.statistics.PlayerStatistic;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.UUID;


public class PlayerWrapperImpl extends org.screamingsandals.bedwars.lib.player.PlayerWrapper implements PlayerWrapper {
    private final Player instance;
    private final String name;
    private final PlayerStatistic statistic;
    private int shout;
    private boolean shouted;
    private boolean isInParty;
    private boolean isInvitedToParty;

    public PlayerWrapperImpl(Player player) {
        super(player.getName(), player.getUniqueId());
        name = player.getName();
        instance = player;
        shout = SBAHypixelify.getConfigurator().config.getInt("shout.time-out", 60);
        statistic = PlayerStatisticManager.getInstance().getStatistic(PlayerMapper.wrapPlayer(player));
    }

    @Override
    public int getShoutTimeOut() {
        return shout;
    }

    @Override
    public void sendMessage(String message) { instance.sendMessage(message); }

    @Override
    public Player getInstance() { return instance; }

    @Override
    public boolean isInParty() {
        return isInParty;
    }

    @Override
    public void setInParty(boolean isInParty) {
        this.isInParty = isInParty;
    }

    @Override
    public boolean isInvitedToAParty() {
        return isInvitedToParty;
    }

    @Override
    public void setInvitedToAParty(boolean isInvited) {
        isInvitedToParty = isInvited;
    }

    @Override
    public double getKD() { return statistic.getKD(); }

    @Override
    public boolean canShout() {
        return !shouted;
    }

    @Override
    public UUID getUUID() { return instance.getUniqueId(); }

    public String getName() {
        return name;
    }

    @Override
    public int getKills() {
        return statistic.getKills();
    }

    @Override
    public int getWins() {
        return statistic.getWins();
    }

    @Override
    public int getBedDestroys() {
        return statistic.getDestroyedBeds();
    }

    @Override
    public int getDeaths() {
        return statistic.getDeaths();
    }

    @Override
    public void shout() {
        if (!shouted) {
            shouted = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    shout--;
                    if (shout == 0) {
                        shouted = false;
                        shout = SBAHypixelify
                                .getConfigurator()
                                .config
                                .getInt("shout.time-out", 60);
                        this.cancel();
                    }
                }
            }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
        }
    }

    @Override
    public int getXP() {
        try {
            return statistic.getScore();
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public int getLevel() {
        int xp = getXP();

        if (xp < 50)
            return 1;

        return getXP() / 500;
    }

    @Override
    public String getProgress() {
        String p = "§b{p}§7/§a500";
        int progress;
        try {
            progress = getXP() - (getLevel() * 500);
        } catch (Exception e) {
            progress = 1;
        }
        return p
                .replace("{p}", String.valueOf(progress));
    }

    @Override
    public int getIntegerProgress() {
        int progress;
        try {
            progress = getXP() - (getLevel() * 500);
        } catch (Exception e) {
            progress = 1;
        }
        return progress;
    }

    public String getStringProgress() {
        String progress = null;
        try {
            int p = getIntegerProgress();
            if (p < 0)
                progress = "§b0§7/§a500";
            else
                progress = getProgress();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (progress == null) {
            return "§b0§7/§a500";
        }
        return progress;
    }

    @Override
    public String getCompletedBoxes() {
        int progress;
        try {
            progress = (getXP() - (getLevel() * 500)) / 5;
        } catch (Exception e) {
            progress = 1;
        }
        if (progress < 1)
            progress = 1;
        char i  =String.valueOf(Math.abs((long) progress)).charAt(0);
        if (progress < 10) {
            i = '1';
        }
        return "§7[§b" + Strings.repeat("■", Integer.parseInt(String.valueOf(i)))
                + "§7" + Strings.repeat("■", 10 - Integer.parseInt(String.valueOf(i))) + "]";
    }
}