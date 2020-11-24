package org.pronze.hypixelify.data;

import com.google.common.base.Strings;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Ignore;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.api.party.Party;
import org.pronze.hypixelify.api.party.PartyManager;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.Scheduler;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.statistics.PlayerStatistic;

import java.util.UUID;

public class PlayerWrapper implements org.pronze.hypixelify.api.wrapper.PlayerWrapper {

    private final String name;
    private final Player pInstance;
    private UUID player;
    private BukkitTask inviteTask;
    private Party invitedParty;
    private Player partyLeader;
    private PlayerStatistic statistic;
    private int shout;


    private boolean isInParty = false;
    private boolean isInvited = false;
    private boolean partyChat = false;
    private boolean shouted = false;

    public PlayerWrapper(Player player) {
        this.player = player.getUniqueId();
        name = player.getDisplayName();
        pInstance = player;
        shout = SBAHypixelify.getConfigurator().config.getInt("shout.time-out", 60);
        statistic  = Main.getPlayerStatisticsManager().getStatistic(pInstance);
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
        if (partyLeader == null)
            return false;
        return player.equals(partyLeader.getUniqueId());
    }

    @Override
    public Party getInvitedParty() {
        return invitedParty;
    }

    @Override
    public void setInvitedParty(Party party) {
        invitedParty = party;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player.getUniqueId();
    }

    @Override
    public Player getPartyLeader() {
        return partyLeader;
    }

    @Override
    public void setPartyLeader(Player player) {
        partyLeader = player;
    }



    public void init() {
        Scheduler.runTaskLater(this::updateDatabase, 1L);
    }


    @Override
    public int getKills() {
        return statistic.getCurrentKills() +
               statistic.getKills();
    }

    @Override
    public int getWins() {
        return statistic.getWins() +
                statistic.getCurrentWins();
    }

    @Override
    public int getBedDestroys() {
        return statistic.getCurrentDestroyedBeds() +
               statistic.getDestroyedBeds();
    }

    @Override
    public int getDeaths() {
        return statistic.getDeaths() +
                statistic.getCurrentKills();
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
        if(progress < 1)
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
    public double getKD() {
        return Main.getPlayerStatisticsManager().getStatistic(pInstance).getKD();
    }



    @Override
    public void updateDatabase() {
        if (!isInParty || !isPartyLeader()) return;
        final PartyManager partyManager = SBAHypixelify.getPartyManager();
        final Party party = partyManager.getParty(partyLeader);
        if (party == null) return;
        if (party.shouldDisband()) {
            if (pInstance.isOnline()) {
                Messages.message_party_disband_inactivity
                        .forEach(st->pInstance.sendMessage(ShopUtil.translateColors(st)));
            }
            setIsInParty(false);
            party.disband();
            Scheduler.runTask(()->partyManager.removeParty(partyLeader));
            setPartyLeader(null);
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

        if(bool){
            final PartyManager partyManager = SBAHypixelify.getPartyManager();
            inviteTask = Scheduler.runTaskLater(()->{
                if(isInvited) {
                    final Party party = partyManager.getParty(invitedParty.getLeader());

                    if (invitedParty != null && party != null) {
                        party.removeInvitedMember(pInstance);
                        final Player partyLeader = invitedParty.getLeader();

                        if (partyLeader != null && partyLeader.isOnline())
                            ShopUtil.sendMessage(partyLeader, Messages.message_invite_expired);
                        if (pInstance != null && pInstance.isOnline())
                            ShopUtil.sendMessage(pInstance, Messages.message_invite_expired);
                    }
                    setInvitedParty(null);
                    Scheduler.runTask(() -> {
                        SBAHypixelify.getWrapperService().updateAll();
                        isInvited = false;
                    });
                }
            }, 20L * 60);
        } else{
            cancelInviteTask();
        }

    }

    public void cancelInviteTask(){
        try{
            if(inviteTask != null && !inviteTask.isCancelled()){
                inviteTask.cancel();
                inviteTask = null;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}