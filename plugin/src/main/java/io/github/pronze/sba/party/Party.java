package io.github.pronze.sba.party;
import io.github.pronze.sba.wrapper.PlayerSetting;
import net.kyori.adventure.text.Component;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.data.PartyInviteData;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import java.util.*;
import java.util.stream.Collectors;

public class Party implements IParty {
    @NotNull
    private SBAPlayerWrapper leader;
    private final UUID uuid;
    private final List<SBAPlayerWrapper> members;
    private final List<SBAPlayerWrapper> invitedPlayers;
    private final Map<UUID, PartyInviteData> inviteDataMap;
    private final PartySetting settings;

    public Party(@NotNull SBAPlayerWrapper leader) {
        this.leader = leader;
        this.uuid = UUID.randomUUID();
        this.members = new ArrayList<>();
        this.invitedPlayers = new ArrayList<>();
        this.inviteDataMap = new HashMap<>();
        this.settings = new PartySetting();

        leader.getSettings().enable(PlayerSetting.IN_PARTY);
        members.add(leader);
    }

    @Override
    public List<SBAPlayerWrapper> getMembers() {
        return List.copyOf(members);
    }

    @Override
    public List<SBAPlayerWrapper> getInvitedPlayers() {
        return List.copyOf(invitedPlayers);
    }

    @Override
    public void sendMessage(@NotNull Component message, @NotNull SBAPlayerWrapper sender) {
        Logger.trace(
                "Sending message: {} to party: {}",
                AdventureHelper.toLegacy(message),
                debugInfo()
        );
        final var formattedMessage = LanguageService
                .getInstance()
                .get(MessageKeys.PARTY_CHAT_FORMAT)
                .replace("%name%", sender.as(Player.class).getDisplayName() + ChatColor.RESET)
                .replace("%message%", AdventureHelper.toLegacy(message))
                .toComponent();
        members.forEach(player -> PlayerMapper.wrapPlayer(player.getInstance()).sendMessage(formattedMessage));
    }

    @Override
    public void addPlayer(@NotNull SBAPlayerWrapper player) {
        invitedPlayers.remove(player);
        members.add(player);
        //leader.getSettings().disable(PlayerSetting.IN_PARTY);
        if (inviteDataMap.containsKey(player.getInstance().getUniqueId())) {
            final var inviteData = inviteDataMap.get(player.getInstance().getUniqueId());
            if (inviteData != null) {
                SBAUtil.cancelTask(inviteData.getInviteTask());
                player.getSettings().disable(PlayerSetting.INVITED_TO_PARTY);
                inviteDataMap.remove(player.getInstance().getUniqueId());
            }
        }
    }

    @Override
    public void removePlayer(@NotNull SBAPlayerWrapper player) {
        members.remove(player);
        player.getSettings().disable(PlayerSetting.IN_PARTY);
    }

    @NotNull
    @Override
    public synchronized SBAPlayerWrapper getPartyLeader() {
        return leader;
    }

    @Override
    public synchronized void setPartyLeader(@NotNull SBAPlayerWrapper player) {
        if (player.equals(leader)) {
            return;
        }
        leader = player;
        //leader.getSettings().disable(PlayerSetting.IN_PARTY);
        if (!members.contains(leader)) {
            members.add(leader);
        }
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void invitePlayer(@NotNull SBAPlayerWrapper invitee,
                             @NotNull SBAPlayerWrapper player) {
        if (inviteDataMap.containsKey(invitee.getInstance().getUniqueId())) return;
        invitedPlayers.add(invitee);
        invitee.getSettings().enable(PlayerSetting.INVITED_TO_PARTY);

        final var inviteTask = new BukkitRunnable() {
            @Override
            public void run() {
                invitee.getSettings().disable(PlayerSetting.INVITED_TO_PARTY);
                inviteDataMap.remove(invitee.getInstance().getUniqueId());
                if (shouldDisband()) {
                    SBA.getInstance()
                            .getPartyManager()
                            .disband(uuid);
                    Logger.trace("Disbanding party: {}", uuid);
                }
                if (getPartyLeader().isOnline()) {
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_INVITE_EXPIRED)
                            .send(getPartyLeader());
                }
            }
        }.runTaskLater(SBA.getPluginInstance(),
                20L * SBA
                        .getInstance()
                        .getConfigurator()
                        .getInt("party.invite-expiration-time", 60));

        final var inviteData = PartyInviteData.of(invitee, player, inviteTask);
        inviteDataMap.put(invitee.getInstance().getUniqueId(), inviteData);
    }

    @Override
    public boolean isInvited(@NotNull SBAPlayerWrapper player) {
        return inviteDataMap.containsKey(player.getInstance().getUniqueId());
    }

    @Override
    public boolean shouldDisband() {
        return getInvitedPlayers().size() == 0 && getMembers().size() <= 1;
    }

    @Override
    public void removeInvitedPlayer(@NotNull SBAPlayerWrapper invitee) {
        if (!invitedPlayers.contains(invitee)) {
            return;
        }

        invitedPlayers.remove(invitee);
        invitee.getSettings().disable(PlayerSetting.INVITED_TO_PARTY);
    }

    @Override
    public List<PartyInviteData> getInviteData() {
        return List.copyOf(inviteDataMap.values());
    }

    @Override
    public String toString() {
        return "Party{" +
                "uuid=" + uuid +
                ", leader=" + leader.as(Player.class).getDisplayName() +
                ", members=" + members.stream().map(SBAPlayerWrapper::getName).collect(Collectors.toList()).toString() +
                ", invitedPlayers=" + invitedPlayers.stream().map(SBAPlayerWrapper::getName).collect(Collectors.toList()).toString() +
                '}';
    }

    public String debugInfo() {
        return "[leader=" + leader.as(Player.class).getDisplayName() + ", uuid=" + uuid.toString() + "]";
    }

    @Override
    public synchronized PartySetting getSettings() {
        return settings;
    }
}
