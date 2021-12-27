package io.github.pronze.sba.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.item.meta.PotionEffectHolder;
import org.screamingsandals.lib.packet.SClientboundSetEquipmentPacket;
import org.screamingsandals.lib.packet.SClientboundSetPlayerTeamPacket;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.sidebar.Sidebar;
import org.screamingsandals.lib.sidebar.team.ScoreboardTeam;
import org.screamingsandals.lib.slot.EquipmentSlotHolder;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskBase;
import org.screamingsandals.lib.tasker.task.TaskState;
import org.screamingsandals.lib.tasker.task.TaskerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class InvisiblePlayerImpl extends GamePlayer implements InvisiblePlayer {
    private final List<GamePlayer> hiddenFrom = new ArrayList<>();

    private final GameWrapper gameWrapper;
    private final Sidebar sidebar;
    private final ScoreboardTeam invisibleSidebarTeam;
    private final RunningTeam team;

    private TaskerTask showTask;
    @Getter @Setter
    private boolean hidden;

    /**
     * Boolean used to indicate if the Invisible Player was just hidden recently [10 Ticks before], so PacketListeners can cancel any further Equipment packets for  viewers.
     */
    @Getter @Setter
    private boolean justHidden;

    public InvisiblePlayerImpl(@NotNull PlayerWrapper wrappedObject,
                               @NotNull GameWrapper gameWrapper,
                               @NotNull Sidebar sidebar) {
        super(wrappedObject);
        this.gameWrapper = gameWrapper;
        this.sidebar = sidebar;
        this.team = gameWrapper.getGame().getTeamOfPlayer(as(Player.class));
        String invisibleTeamName = "i-" + team.getName();

        final var chatColor = TeamColor.fromApiColor(team.getColor()).chatColor;
        invisibleSidebarTeam = sidebar
                .team(invisibleTeamName)
                .nameTagVisibility(SClientboundSetPlayerTeamPacket.TagVisibility.NEVER)
                .friendlyFire(false)
                .color(NamedTextColor.NAMES.value(chatColor.name().toLowerCase()));
    }

    @Override
    public void show() {
        hiddenFrom.forEach(getResetPacket()::sendPacket);
        hiddenFrom.clear();

        gameWrapper.getGame()
                .getConnectedPlayers()
                .stream()
                .map(PlayerMapper::wrapPlayer)
                .forEach(player -> {
                    if (invisibleSidebarTeam.players().contains(player)) {
                        invisibleSidebarTeam.removePlayer(player);
                    }

                    var sidebarTeam = sidebar.getTeam(team.getName()).orElseThrow();
                    if (!sidebarTeam.players().contains(player)) {
                        sidebarTeam.player(player);
                    }
                });

        hidden = false;
        gameWrapper.removeInvisiblePlayer(this);
    }

    @Override
    public void hide() {
        final var game = gameWrapper.getGame();

        final var toHide = game.getConnectedPlayers()
                .stream()
                .map(PlayerMapper::wrapPlayer)
                .map(playerWrapper -> playerWrapper.as(InvisiblePlayerImpl.class))
                .collect(Collectors.toList());

        toHide.removeIf(hiddenFrom::contains);
        toHide.forEach(gamePlayer -> {
            getInvisiblePacket().sendPacket(gamePlayer);
            var sidebarTeam = sidebar.getTeam(team.getName()).orElseThrow();
            if (sidebarTeam.players().contains(gamePlayer)) {
                sidebarTeam.removePlayer(gamePlayer);
            }

            if (!invisibleSidebarTeam.players().contains(gamePlayer)) {
                invisibleSidebarTeam.player(gamePlayer);
            }
        });

        if (showTask == null
                || showTask.getState() == TaskState.CANCELLED) {
            showTask = Tasker.build(taskBase -> new ShowTask(this, taskBase, game))
                    .repeat(1L, TaskerTime.SECONDS)
                    .async()
                    .start();
        }

        hidden = true;
        justHidden = true;

        Tasker.build(() -> setJustHidden(false))
                .delay(10L, TaskerTime.TICKS)
                .start();
    }

    @Override
    public void removeHidden(@NotNull GamePlayer gamePlayer) {
        if (!hiddenFrom.contains(gamePlayer)) {
            return;
        }
        invisibleSidebarTeam.removePlayer(gamePlayer);
        hiddenFrom.remove(gamePlayer);
    }

    @NotNull
    @Override
    public List<GamePlayer> getHiddenFrom() {
        return List.copyOf(hiddenFrom);
    }

    private SClientboundSetEquipmentPacket getInvisiblePacket() {
        return getEquipmentPacket(null, null, null, null);
    }

    private SClientboundSetEquipmentPacket getResetPacket() {
        return getEquipmentPacket(
                getHelmet(),
                getChestplate(),
                getLeggings(),
                getBoots()
        );
    }

    private SClientboundSetEquipmentPacket getEquipmentPacket(Item helmet,
                                                              Item chestplate,
                                                              Item leggings,
                                                              Item boots) {
        final var packet = new SClientboundSetEquipmentPacket();
        packet.entityId(getEntityId());

        final var slots = packet.slots();
        slots.put(EquipmentSlotHolder.of("HEAD"), airIfNull(helmet));
        slots.put(EquipmentSlotHolder.of("CHEST"), airIfNull(chestplate));
        slots.put(EquipmentSlotHolder.of("LEGS"), airIfNull(leggings));
        slots.put(EquipmentSlotHolder.of("FEET"), airIfNull(boots));

        return packet;
    }

    private Item airIfNull(@Nullable Item item) {
        return Objects.requireNonNullElse(item, ItemFactory.getAir());
    }

    @RequiredArgsConstructor
    private static final class ShowTask implements Runnable {
        private final InvisiblePlayerImpl invisiblePlayer;
        private final TaskBase taskBase;
        private final Game game;

        @Override
        public void run() {
            if (!invisiblePlayer.isOnline()
                    || game.getStatus() != GameStatus.RUNNING
                    || !invisiblePlayer.getGameMode().is("SURVIVAL")
                    || !invisiblePlayer.hasPotionEffect(PotionEffectHolder.of("INVISIBILITY"))
                    || !game.getConnectedPlayers().contains(invisiblePlayer.as(Player.class))) {
                invisiblePlayer.show();
                taskBase.cancel();
            }
        }
    }
}
