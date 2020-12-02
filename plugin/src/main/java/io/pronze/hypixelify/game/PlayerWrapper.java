package io.pronze.hypixelify.game;

import com.google.common.base.Strings;
import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.party.Party;
import io.pronze.hypixelify.api.party.PartyManager;
import io.pronze.hypixelify.message.Messages;
import io.pronze.hypixelify.utils.Scheduler;
import io.pronze.hypixelify.utils.ShopUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.statistics.PlayerStatistic;

import javax.annotation.Nullable;

public class PlayerWrapper implements io.pronze.hypixelify.api.wrapper.PlayerWrapper {

    private final String name;
    private final Player instance;
    private final PlayerStatistic statistic;
    private BukkitTask inviteTask;
    private Party invitedParty;
    private Party party;
    private int shout;


    private boolean isInParty = false;
    private boolean isInvited = false;
    private boolean partyChat = false;
    private boolean shouted = false;

    public PlayerWrapper(Player player) {
        name = player.getDisplayName();
        instance = player;
        shout = SBAHypixelify.getConfigurator().config.getInt("shout.time-out", 60);
        statistic = Main.getPlayerStatisticsManager().getStatistic(instance);
        init();
    }

    @Override
    public int getShoutTimeOut() {
        return shout;
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
                        shout = SBAHypixelify.getConfigurator().config.getInt("shout.time-out", 60);
                        this.cancel();
                    }
                }
            }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
        }
    }

    @Override
    public boolean canShout() {
        return !shouted;
    }

    @Override
    public boolean getPartyChatEnabled() {
        return partyChat;
    }

    @Override
    public void setPartyChatEnabled(boolean b) {
        partyChat = b;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setIsInParty(boolean bool) {
        isInParty = bool;
    }

    @Override
    public boolean isPartyLeader() {
        if (party == null || party.getLeader() == null)
            return false;
        return instance.getUniqueId().equals(party.getLeader().getUniqueId());
    }

    @Override
    public Party getInvitedParty() {
        return invitedParty;
    }

    @Override
    public void setInvitedParty(Party party) {
        invitedParty = party;
    }


    @Nullable
    @Override
    public Player getPartyLeader() {
        if (party == null) {
            return null;
        }
        return party.getLeader();
    }

    public void init() {
        Scheduler.runTaskLater(this::updateDatabase, 1L);
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
    public int getXP() {
        try {
            return statistic.getScore() +
                    statistic.getCurrentScore();
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
        char i;
        i = String.valueOf(Math.abs((long) progress)).charAt(0);
        if (progress < 10) {
            i = '1';
        }
        return "§b" + Strings.repeat("■", Integer.parseInt(String.valueOf(i)))
                + "§7" + Strings.repeat("■", 10 - Integer.parseInt(String.valueOf(i)));
    }

    @Override
    public void sendMessage(String message) {
        if (!instance.isOnline()) {
            return;
        }

        instance.sendMessage(message);
    }

    @Override
    public Party getParty() {
        return party;
    }

    @Override
    public void setParty(Player leader) {
        party = SBAHypixelify.getPartyManager().getParty(leader);
    }

    @Override
    public double getKD() {
        return Main.getPlayerStatisticsManager().getStatistic(instance).getKD();
    }


    @Override
    public void updateDatabase() {
        if (!isInParty || !isPartyLeader()) return;
        final PartyManager partyManager = SBAHypixelify.getPartyManager();
        if (party == null) return;
        if (party.shouldDisband()) {
            if (instance.isOnline()) {
                Messages.message_party_disband_inactivity
                        .forEach(st -> instance.sendMessage(ShopUtil.translateColors(st)));
            }
            setIsInParty(false);
            party.disband();
            Scheduler.runTask(() -> partyManager.removeParty(party));
            setParty(null);
        }
    }

    @Override
    public boolean isInParty() {
        return isInParty;
    }

    @Override
    public boolean isInvited() {
        return isInvited;
    }

    @Override
    public void setInvited(boolean bool) {
        isInvited = bool;

        if (bool) {
            final PartyManager partyManager = SBAHypixelify.getPartyManager();
            cancelInviteTask();
            inviteTask = Scheduler.runTaskLater(() -> {
                if (isInvited) {
                    final Party party = partyManager.getParty(invitedParty.getLeader());

                    if (invitedParty != null && party != null) {
                        party.removeInvitedMember(instance);
                        final Player partyLeader = invitedParty.getLeader();

                        if (partyLeader != null && partyLeader.isOnline())
                            ShopUtil.sendMessage(partyLeader, Messages.message_invite_expired);
                        if (instance != null && instance.isOnline())
                            ShopUtil.sendMessage(instance, Messages.message_invite_expired);
                    }
                    setInvitedParty(null);
                    SBAHypixelify.getWrapperService().updateAll();
                    isInvited = false;
                }
            }, 20L * 60);
        } else {
            cancelInviteTask();
        }

    }

    public void cancelInviteTask() {
        try {
            if (inviteTask != null && !inviteTask.isCancelled()) {
                inviteTask.cancel();
                inviteTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}