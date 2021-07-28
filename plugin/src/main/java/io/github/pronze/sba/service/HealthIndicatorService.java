package io.github.pronze.sba.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.packet.PacketMapper;
import org.screamingsandals.lib.packet.SClientboundSetDisplayObjectivePacket;
import org.screamingsandals.lib.packet.SClientboundSetObjectivePacket;
import org.screamingsandals.lib.packet.SClientboundSetScorePacket;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnDisable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import java.text.DecimalFormat;
import java.util.*;

@Service(dependsOn = {
        PacketMapper.class
})
public class HealthIndicatorService implements Listener {
    private final Map<UUID, Map<UUID, Double>> dataMap = new HashMap<>();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##");

    private static final String TAB_IDENTIFIER = "sba-tab";
    private static final String TAG_IDENTIFIER = "sba-tag";

    private boolean tabEnabled;
    private boolean tagEnabled;

    @OnPostEnable
    public void postEnabled() {
        tabEnabled = SBAConfig
                .getInstance()
                .node("show-health-in-tablist")
                .getBoolean();

        tagEnabled = SBAConfig
                .getInstance()
                .node("show-health-under-player-name")
                .getBoolean();
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onGameStart(BedwarsGameStartedEvent event) {
        final Game game = event.getGame();
        game.getConnectedPlayers().forEach(this::create);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getStatus() == GameStatus.RUNNING) {
                    update(game);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(SBA.getPluginInstance(), 0L, 2L);
    }

    @OnDisable
    public void onDestroy() {
        Set.copyOf(dataMap
                .keySet())
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(this::removePlayer);
    }

    @EventHandler
    public void onPlayerLeave(BedwarsPlayerLeaveEvent event) {
        final var player = event.getPlayer();
        removePlayer(player);
    }

    public void removePlayer(Player player) {
        final var playerData = dataMap.get(player.getUniqueId());
        if (playerData != null) {
            final var playerWrapper = PlayerMapper.wrapPlayer(player);
            if (tabEnabled) {
                getDestroyObjectivePacket(TAB_IDENTIFIER)
                        .sendPacket(playerWrapper);
            }
            if (tagEnabled) {
                getDestroyObjectivePacket(TAG_IDENTIFIER)
                        .sendPacket(playerWrapper);
            }

            dataMap.remove(player.getUniqueId());
        }
    }

    public void update(Game game) {
        game.getConnectedPlayers()
                .forEach(player -> {
                    if (!dataMap.containsKey(player.getUniqueId()))
                        dataMap.put(player.getUniqueId(), new HashMap<>());

                    final var data = dataMap.get(player.getUniqueId());
                    game.getConnectedPlayers().forEach(p -> {
                        if (p.getHealth() != data.getOrDefault(p.getUniqueId(), Double.MAX_VALUE)) {
                            update(player, p.getName(), Integer.parseInt(DECIMAL_FORMAT.format(p.getHealth())));
                            data.put(p.getUniqueId(), p.getHealth());
                        }
                    });
                });
    }

    public void update(Player player, @NotNull String playerName, int health) {
        final var playerWrapper = PlayerMapper.wrapPlayer(player);

        if (tabEnabled) {
            createOrUpdateScorePacket(TAB_IDENTIFIER, health, playerName)
                    .sendPacket(playerWrapper);
        }
        if (tagEnabled) {
            createOrUpdateScorePacket(TAG_IDENTIFIER, health, playerName)
                    .sendPacket(playerWrapper);
        }
    }

    public void create(Player player) {
        dataMap.put(player.getUniqueId(), new HashMap<>());
        final var playerWrapper = PlayerMapper.wrapPlayer(player);

        if (tabEnabled) {
            getScoreboardObjectiveCreatePacket(TAB_IDENTIFIER, Component.text("healthIndicator"))
                    .sendPacket(playerWrapper);
            getScoreboardDisplayObjectivePacket(TAB_IDENTIFIER, SClientboundSetDisplayObjectivePacket.DisplaySlot.PLAYER_LIST)
                    .sendPacket(playerWrapper);
        }

        if (tagEnabled) {
            getScoreboardObjectiveCreatePacket(TAG_IDENTIFIER, Component.text("§c♥"))
                    .sendPacket(playerWrapper);
            getScoreboardDisplayObjectivePacket(TAG_IDENTIFIER, SClientboundSetDisplayObjectivePacket.DisplaySlot.BELOW_NAME)
                    .sendPacket(playerWrapper);
        }
    }

    private SClientboundSetScorePacket createOrUpdateScorePacket(String objectiveKey, int i, String entityName) {
        var packet = new SClientboundSetScorePacket();
        packet.entityName(entityName);
        packet.objectiveKey(objectiveKey);
        packet.action(SClientboundSetScorePacket.ScoreboardAction.CHANGE);
        packet.score(i);
        return packet;
    }

    private SClientboundSetScorePacket destroyScore(String entityName, String objectiveKey) {
        var packet = new SClientboundSetScorePacket();
        packet.objectiveKey(objectiveKey);
        packet.entityName(entityName);
        packet.action(SClientboundSetScorePacket.ScoreboardAction.REMOVE);
        return packet;
    }

    private SClientboundSetObjectivePacket getDestroyObjectivePacket(String objectiveKey) {
        var packet = new SClientboundSetObjectivePacket();
        packet.objectiveKey(objectiveKey);
        packet.mode(SClientboundSetObjectivePacket.Mode.DESTROY);
        return packet;
    }

    private SClientboundSetDisplayObjectivePacket getScoreboardDisplayObjectivePacket(String objectiveKey, SClientboundSetDisplayObjectivePacket.DisplaySlot type) {
        var packet = new SClientboundSetDisplayObjectivePacket();
        packet.objectiveKey(objectiveKey);
        packet.slot(type);
        return packet;
    }

    public SClientboundSetObjectivePacket getScoreboardObjectiveCreatePacket(String objectiveKey, Component title) {
        var packet = new SClientboundSetObjectivePacket();
        packet.objectiveKey(objectiveKey);
        packet.title(title);
        packet.criteriaType(SClientboundSetObjectivePacket.Type.INTEGER);
        packet.mode(SClientboundSetObjectivePacket.Mode.CREATE);
        return packet;
    }
}
