package pronze.hypixelify.service;

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
import org.screamingsandals.bedwars.lib.bukkit.utils.nms.ClassStorage;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.serializer.craftbukkit.MinecraftComponentSerializer;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import org.screamingsandals.bedwars.lib.utils.reflect.Reflect;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.config.SBAConfig;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnDestroy;
import java.text.DecimalFormat;
import java.util.*;

@AutoInitialize(listener = true)
public class HealthIndicatorService implements Listener {
    private final Map<UUID, Map<UUID, Double>> dataMap = new HashMap<>();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##");

    private static final String TAB_IDENTIFIER = "sba-tab";
    private static final String TAG_IDENTIFIER = "sba-tag";

    private final boolean tabEnabled;
    private final boolean tagEnabled;

    public HealthIndicatorService () {
        tabEnabled = SBAConfig
                .getInstance()
                .node("show-health-in-tablist")
                .getBoolean();

        tagEnabled = SBAConfig
                .getInstance()
                .node("show-health-under-player-name")
                .getBoolean();
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
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 2L);
    }

    @OnDestroy
    public void onDestroy() {
        dataMap
                .keySet()
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
            if (tabEnabled) {
                ClassStorage.sendPacket(player, destroyObjective(TAB_IDENTIFIER));
            }
            if (tagEnabled) {
                ClassStorage.sendPacket(player, destroyObjective(TAG_IDENTIFIER));
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
                            update(player, p.getName(), Integer.parseInt(DECIMAL_FORMAT.format(player.getHealth())));
                            data.put(p.getUniqueId(), player.getHealth());
                        }
                    });
                });
    }

    public void update(Player player, @NotNull String playerName, int health) {
        if (tabEnabled) {
            ClassStorage.sendPacket(player, createOrUpdateScorePacket(TAB_IDENTIFIER, health, playerName));
        }
        if (tagEnabled) {
            ClassStorage.sendPacket(player, createOrUpdateScorePacket(TAG_IDENTIFIER, health, playerName));
        }
    }

    public void create(Player player) {
        dataMap.put(player.getUniqueId(), new HashMap<>());

        if (tabEnabled) {
            ClassStorage.sendPacket(player, getScoreboardObjectiveCreatePacket(TAB_IDENTIFIER, Component.text("healthIndicator")));
            ClassStorage.sendPacket(player, getScoreboardDisplayObjective(TAB_IDENTIFIER, 0));
        }

        if (tagEnabled) {
            ClassStorage.sendPacket(player, getScoreboardObjectiveCreatePacket(TAG_IDENTIFIER, Component.text("§c♥")));
            ClassStorage.sendPacket(player, getScoreboardDisplayObjective(TAG_IDENTIFIER, 2));
        }
    }

    private Object createOrUpdateScorePacket(String objectiveKey, int i, String value) {
        var packet = Reflect.constructResulted(ClassStorage.NMS.PacketPlayOutScoreboardScore);
        packet.setField("a", value);
        packet.setField("b", objectiveKey);
        packet.setField("c", i);
        packet.setField("d", Reflect.findEnumConstant(ClassStorage.NMS.EnumScoreboardAction, "CHANGE"));
        return packet.raw();
    }

    private Object destroyScore(String value, String objectiveKey) {
        var packet = Reflect.constructResulted(ClassStorage.NMS.PacketPlayOutScoreboardScore);
        packet.setField("a", value);
        packet.setField("b", objectiveKey);
        packet.setField("d", Reflect.findEnumConstant(ClassStorage.NMS.EnumScoreboardAction, "REMOVE"));
        return packet.raw();
    }

    private Object destroyObjective(String objectiveKey) {
        var packet = Reflect.constructResulted(ClassStorage.NMS.PacketPlayOutScoreboardObjective);
        packet.setField("a", objectiveKey);
        packet.setField("d", 1);
        return packet.raw();
    }

    private Object getScoreboardDisplayObjective(String objectiveKey, int type) {
        var packet = Reflect.constructResulted(ClassStorage.NMS.PacketPlayOutScoreboardDisplayObjective);
        packet.setField("a", type);
        packet.setField("b", objectiveKey);
        return packet.raw();
    }

    public Object getScoreboardObjectiveCreatePacket(String objectiveKey, Component title) {
        var packet = Reflect.constructResulted(ClassStorage.NMS.PacketPlayOutScoreboardObjective);
        packet.setField("a", objectiveKey);
        if (packet.setField("b", asMinecraftComponent(title)) == null) {
            packet.setField("b", AdventureHelper.toLegacy(title));
        }
        packet.setField("c", Reflect.findEnumConstant(ClassStorage.NMS.EnumScoreboardHealthDisplay, "INTEGER"));
        packet.setField("d", 0);
        return packet.raw();
    }

    public static Object asMinecraftComponent(Component component) {
        try {
            return MinecraftComponentSerializer.get().serialize(component);
        } catch (Exception ignored) { // current Adventure is facing some weird bug on non-adventure native server software, let's do temporary workaround
            return Reflect.getMethod(ClassStorage.NMS.ChatSerializer, "a,field_150700_a", String.class)
                    .invokeStatic(GsonComponentSerializer.gson().serialize(component));
        }
    }
}
