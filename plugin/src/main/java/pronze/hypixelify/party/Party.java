package pronze.hypixelify.party;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.data.PartyInviteData;
import pronze.hypixelify.api.party.IParty;
import pronze.hypixelify.api.party.PartySetting;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Party implements IParty {
    private final UUID uuid = UUID.randomUUID();
    private @NotNull volatile PlayerWrapper leader;
    private final List<PlayerWrapper> members = Collections.synchronizedList(new LinkedList<>());
    private final List<PlayerWrapper> invitedPlayers = Collections.synchronizedList(new LinkedList<>());
    private final Map<UUID, PartyInviteData> inviteDataMap = new ConcurrentHashMap<>();
    private final PartySetting settings = new PartySetting();

    public Party(@NotNull PlayerWrapper leader) {
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
                debugInfo()
        );
        final var formattedMessage = LanguageService
                .getInstance()
                .get(MessageKeys.PARTY_CHAT_FORMAT)
                .replace("%player%", sender.getName())
                .replace("%message%", AdventureHelper.toLegacy(message))
                .toComponent();
        members.forEach(player -> PlayerMapper.wrapPlayer(player.getInstance()).sendMessage(formattedMessage));
    }

    @Override
    public void addPlayer(@NotNull PlayerWrapper player) {
        Logger.trace("Adding player: {} to party: {}", player.getName(), debugInfo());
        invitedPlayers.remove(player);
        members.add(player);
        player.setInParty(true);
        if (inviteDataMap.containsKey(player.getInstance().getUniqueId())) {
            final var inviteData = inviteDataMap.get(player.getInstance().getUniqueId());
            if (inviteData != null) {
                SBAUtil.cancelTask(inviteData.getInviteTask());
                player.setInvitedToAParty(false);
                inviteDataMap.remove(player.getInstance().getUniqueId());
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
    public synchronized PlayerWrapper getPartyLeader() {
        return leader;
    }

    @Override
    public synchronized void setPartyLeader(@NotNull PlayerWrapper player) {
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
        if (inviteDataMap.containsKey(invitee.getInstance().getUniqueId())) return;
        Logger.trace("Player: {} has invited: {} to party: {}", player.getName(),
                invitee.getName(), debugInfo());
        invitedPlayers.add(invitee);
        invitee.setInvitedToAParty(true);

        final var inviteTask = new BukkitRunnable() {
            @Override
            public void run() {
                Logger.trace("IParty invitation expired for: {} of party: {}", invitee.getName(), debugInfo());
                invitee.setInvitedToAParty(false);
                inviteDataMap.remove(invitee.getInstance().getUniqueId());
                if (shouldDisband()) {
                    SBAHypixelify
                            .getInstance()
                            .getPartyManager()
                            .disband(uuid);
                    Logger.trace("Disbanding party: {}", uuid);
                }
                LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_INVITE_EXPIRED)
                        .send(getPartyLeader());
            }
        }.runTaskLater(SBAHypixelify.getPluginInstance(),
                20L * SBAHypixelify
                        .getInstance()
                        .getConfigurator()
                        .getInt("party.invite-expiration-time", 60));

        final var inviteData = new PartyInviteData(invitee, player, inviteTask);
        inviteDataMap.put(invitee.getInstance().getUniqueId(), inviteData);
    }

    @Override
    public boolean isInvited(@NotNull PlayerWrapper player) {
        return inviteDataMap.containsKey(player.getInstance().getUniqueId());
    }

    @Override
    public boolean shouldDisband() {
        return getInvitedPlayers().size() == 0 && getMembers().size() <= 1;
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
    public List<PartyInviteData> getInviteData() {
        return List.copyOf(inviteDataMap.values());
    }

    @Override
    public String toString() {
        return "Party{" +
                "uuid=" + uuid +
                ", leader=" + leader.getName() +
                ", members=" + members.stream().map(PlayerWrapper::getName).collect(Collectors.toList()).toString() +
                ", invitedPlayers=" + invitedPlayers.stream().map(PlayerWrapper::getName).collect(Collectors.toList()).toString() +
                '}';
    }

    public String debugInfo() {
        return "[leader=" + leader.getName() + ", uuid=" + uuid.toString() + "]";
    }

    @Override
    public synchronized PartySetting getSettings() {
        return settings;
    }
}
