package pronze.hypixelify.party;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.data.InviteData;
import pronze.hypixelify.api.party.Party;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PartyImpl implements Party {
    private final UUID uuid = UUID.randomUUID();
    private @NotNull PlayerWrapper leader;
    private final List<PlayerWrapper> members = Collections.synchronizedList(new LinkedList<>());
    private final List<PlayerWrapper> invitedPlayers = Collections.synchronizedList(new LinkedList<>());
    private final Map<UUID, InviteData> inviteDataMap = new ConcurrentHashMap<>();

    public PartyImpl(@NotNull PlayerWrapper leader) {
        this.leader = leader;
        leader.setInParty(true);
        members.add(leader);
        Logger.trace("Created party with leader: {}, party is: {}", leader.getName(), debugInfo());
    }

    @Override
    public List<PlayerWrapper> getMembers() {
        return List.copyOf(members);
    }

    @Override
    public List<PlayerWrapper> getInvitedPlayers() {
        return List.copyOf(invitedPlayers);
    }

    @Override
    public void sendMessage(@NotNull Component message, @NotNull PlayerWrapper sender) {
        Logger.trace(
                "Sending message: {} to party: {}",
                AdventureHelper.toLegacy(message),
                leader.getName(),
                debugInfo()
        );
        final var formattedMessage = SBAHypixelify
                .getConfigurator()
                .getString("party.chat.format")
                .replace("%name%", sender.getName())
                .replace("%message%", AdventureHelper.toLegacy(message));
        members.forEach(player -> PlayerMapper.wrapPlayer(player.getInstance()).sendMessage(formattedMessage));
    }

    @Override
    public void addPlayer(@NotNull PlayerWrapper player) {
        Logger.trace("Adding player: {} to party: {}", player.getName(), debugInfo());
        invitedPlayers.remove(player);
        members.add(player);
        player.setInParty(true);
        if (inviteDataMap.containsKey(player.getUUID())) {
            final var inviteData = inviteDataMap.get(player.getUUID());
            if (inviteData != null) {
                SBAUtil.cancelTask(inviteData.getInviteTask());
                player.setInvitedToAParty(false);
                inviteDataMap.remove(player.getUUID());
            }
        }
    }

    @Override
    public void removePlayer(@NotNull PlayerWrapper player) {
        Logger.trace("Removing player: {} from party: {}", player.getName(), debugInfo());
        members.remove(player);
        player.setInParty(false);
    }

    @NotNull
    @Override
    public PlayerWrapper getPartyLeader() {
        return leader;
    }

    @Override
    public void setPartyLeader(@NotNull PlayerWrapper player) {
        if (player.equals(leader)) return;
        Logger.trace("Replacing leader: {} with: {} in party of uuid: {}",
                leader.getName(), player.getName(), debugInfo());
        leader = player;
        leader.setInParty(true);
        if (!members.contains(leader))
            members.add(leader);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void invitePlayer(@NotNull PlayerWrapper invitee,
                             @NotNull PlayerWrapper player) {
        if (inviteDataMap.containsKey(invitee.getUUID())) return;
        Logger.trace("Player: {} has invited: {} to party: {}", player.getName(),
                invitee.getName(), debugInfo());
        invitedPlayers.add(invitee);
        invitee.setInvitedToAParty(true);

        final var inviteTask = new BukkitRunnable() {
            @Override
            public void run() {
                Logger.trace("Party invitation expired for: {} of party: {}", invitee.getName(), debugInfo());
                invitee.setInvitedToAParty(false);
                inviteDataMap.remove(invitee.getUUID());
                if (shouldDisband()) {
                    SBAHypixelify
                            .getPartyManager()
                            .disband(uuid);
                    Logger.trace("Disbanding party: {}", uuid);
                }
            }
        }.runTaskLater(SBAHypixelify.getInstance(),
                20L * SBAHypixelify
                        .getConfigurator()
                        .config
                        .getInt("party.invite-expiration-time"));

        final var inviteData = new InviteData(invitee, player, inviteTask);
        inviteDataMap.put(invitee.getUUID(), inviteData);
    }

    @Override
    public boolean isInvited(@NotNull PlayerWrapper player) {
        return inviteDataMap.containsKey(player.getUUID());
    }

    @Override
    public boolean shouldDisband() {
        return getInvitedPlayers().size() > 0 || getMembers().size() > 1;
    }

    @Override
    public void removeInvitedPlayer(@NotNull PlayerWrapper invitee) {
        if (!invitedPlayers.contains(invitee)) {
            return;
        }

        invitedPlayers.remove(invitee);
        invitee.setInvitedToAParty(false);
    }

    @Override
    public List<InviteData> getInviteData() {
        return List.copyOf(inviteDataMap.values());
    }


    @Override
    public String toString() {
        return "PartyImpl{" +
                "uuid=" + uuid +
                ", leader=" + leader.getName() +
                ", members=" + members.stream().map(PlayerWrapper::getName).collect(Collectors.toList()).toString() +
                ", invitedPlayers=" + invitedPlayers.stream().map(PlayerWrapper::getName).collect(Collectors.toList()).toString() +
                '}';
    }

    public String debugInfo() {
        return "[leader=" + leader.getName() + ", uuid=" + uuid.toString() + "]";
    }
}
