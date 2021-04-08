package pronze.hypixelify.api.wrapper;

import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.statistics.PlayerStatisticManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerWrapper extends org.screamingsandals.bedwars.lib.player.PlayerWrapper {
    private int shout;
    private boolean shoutCooldown;
    private final AtomicBoolean isInParty = new AtomicBoolean(false);
    private final AtomicBoolean isInvitedToParty = new AtomicBoolean(false);
    private final AtomicBoolean isPartyChatEnabled = new AtomicBoolean(false);

    public PlayerWrapper(Player player) {
        super(player.getName(), player.getUniqueId());
        shout = SBAHypixelifyAPI.getInstance().getConfigurator().getInt("shout.time-out", 60);
    }

    public int getShoutTimeOut() {
        return shout;
    }

    public void sendMessage(String message) { getInstance().sendMessage(message); }

    public Player getInstance() { return Bukkit.getPlayer(getUuid()); }

    public boolean isInParty() {
        return isInParty.get();
    }

    public void setInParty(boolean isInParty) {
        this.isInParty.set(isInParty);
    }

    public boolean isInvitedToAParty() {
        return isInvitedToParty.get();
    }

    public void setInvitedToAParty(boolean isInvited) {
        isInvitedToParty.set(isInvited);
    }

    public boolean isPartyChatEnabled() {
        return isPartyChatEnabled.get();
    }

    public void setPartyChatEnabled(boolean bool) {
        isPartyChatEnabled.set(bool);
    }

    public boolean canShout() {
        return !shoutCooldown;
    }

    public void shout(Component message, Game game) {
        if (!shoutCooldown) {
            shoutCooldown = true;
            sendMessage(message);
            if (getInstance().hasPermission("hypixelify.shout.unlimited")
                    || SBAHypixelifyAPI.getInstance().getConfigurator().getInt("shout.time-out", 60) == 0)
                shoutCooldown = false;

            new BukkitRunnable() {
                @Override
                public void run() {
                    shout--;
                    if (shout == 0) {
                        shoutCooldown = false;
                        shout = SBAHypixelifyAPI
                                .getInstance()
                                .getConfigurator()
                                .getInt("shout.time-out", 60);
                        this.cancel();
                    }
                }
            }.runTaskTimer((JavaPlugin)SBAHypixelifyAPI.getInstance(), 0L, 20L);
        } else {
            final var shout = String.valueOf(getShoutTimeOut());
            SBAHypixelifyAPI
                    .getInstance()
                    .getLanguageService()
                    .get(MessageKeys.MESSAGE_SHOUT_WAIT)
                    .replace("%seconds%", shout)
                    .send(this);
        }
    }

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

    public int getLevel() {
        return (getXP() < 50 ? 1 : getXP() / 500);
    }

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