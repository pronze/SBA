package pronze.hypixelify.game;

import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.statistics.PlayerStatisticManager;
import pronze.hypixelify.SBAHypixelify;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static pronze.hypixelify.lib.lang.I.i18n;


public class PlayerWrapperImpl extends org.screamingsandals.bedwars.lib.player.PlayerWrapper implements PlayerWrapper {
    private int shout;
    private boolean shoutCooldown;
    private final AtomicBoolean isInParty = new AtomicBoolean(false);
    private final AtomicBoolean isInvitedToParty = new AtomicBoolean(false);
    private final AtomicBoolean isPartyChatEnabled = new AtomicBoolean(false);

    public PlayerWrapperImpl(Player player) {
        super(player.getName(), player.getUniqueId());
        shout = SBAHypixelify.getConfigurator().config.getInt("shout.time-out", 60);
    }

    @Override
    public int getShoutTimeOut() {
        return shout;
    }

    @Override
    public void sendMessage(String message) { getInstance().sendMessage(message); }

    @Override
    public Player getInstance() { return (Player)getWrappedPlayer().get(); }

    @Override
    public boolean isInParty() {
        return isInParty.get();
    }

    @Override
    public void setInParty(boolean isInParty) {
        this.isInParty.set(isInParty);
    }

    @Override
    public boolean isInvitedToAParty() {
        return isInvitedToParty.get();
    }

    @Override
    public void setInvitedToAParty(boolean isInvited) {
        isInvitedToParty.set(isInvited);
    }

    @Override
    public boolean isPartyChatEnabled() {
        return isPartyChatEnabled.get();
    }

    @Override
    public void setPartyChatEnabled(boolean bool) {
        isPartyChatEnabled.set(bool);
    }

    @Override
    public boolean canShout() {
        return !shoutCooldown;
    }

    @Override
    public void shout(String message, Game game) {
        if (!shoutCooldown) {
            shoutCooldown = true;
            game.getConnectedPlayers().forEach(pl -> pl.sendMessage(message));
            if (getInstance().hasPermission("hypixelify.shout.unlimited")
                    || SBAHypixelify.getConfigurator().config.getInt("shout.time-out", 60) == 0)
                shoutCooldown = false;

            new BukkitRunnable() {
                @Override
                public void run() {
                    shout--;
                    if (shout == 0) {
                        shoutCooldown = false;
                        shout = SBAHypixelify.getConfigurator().config.getInt("shout.time-out", 60);
                        this.cancel();
                    }
                }
            }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
        } else {
            final var shout = String.valueOf(getShoutTimeOut());
            sendMessage(i18n("shout_wait", true).replace("{seconds}", shout));
        }
    }

    @Override
    public int getXP() {
        try {
            return PlayerStatisticManager
                    .getInstance()
                    .getStatistic(PlayerMapper.wrapPlayer(getInstance()))
                    .getScore();
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public int getLevel() {
        return (getXP() < 50 ? 1 : getXP() / 500);
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

    @Override
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