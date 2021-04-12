package pronze.hypixelify.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.bukkit.utils.nms.ClassStorage;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.serializer.craftbukkit.MinecraftComponentSerializer;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import org.screamingsandals.bedwars.lib.utils.reflect.Reflect;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.data.HealthIndicatorData;
import pronze.hypixelify.config.SBAConfig;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnDestroy;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

@AutoInitialize(listener = true)
public class HealthIndicatorService implements Listener {
    private final Map<UUID, HealthIndicatorData> dataMap = new HashMap<>();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##");

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
                update(game);
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
                ClassStorage.sendPacket(player, destroyObjective(playerData.getTabData()));
            }
            if (tagEnabled) {
                ClassStorage.sendPacket(player, destroyObjective(playerData.getListData()));
            }
            dataMap.remove(player.getUniqueId());
        }
    }

    public void update(Game game) {
        game.getConnectedPlayers()
                .forEach(player -> {
                    var playerData = dataMap.get(player.getUniqueId());
                    if (playerData != null) {
                        var cachedHealth = playerData.getHealth();
                        var currentHealth = player.getHealth();
                        if (cachedHealth != currentHealth) {
                            playerData.setHealth(player.getHealth());
                            game.getConnectedPlayers()
                                   .forEach(pl -> update(pl, player.getName(), Integer.parseInt(DECIMAL_FORMAT.format(player.getHealth()))));
                       }
                    }
                });
    }

    public void update(Player player, @NotNull String playerName, int health) {
        var data = dataMap.get(player.getUniqueId());
        var tabData = data.getTabData();
        var listData = data.getListData();
        if (tabEnabled) {
            ClassStorage.sendPacket(player, createOrUpdateScorePacket(tabData, health, playerName));
        }
        if (tagEnabled) {
            ClassStorage.sendPacket(player, createOrUpdateScorePacket(listData, health, playerName));
        }
    }

    public void create(Player player) {
        byte[] tabArr = new byte[16];
        new Random().nextBytes(tabArr);
        String gs1 = new String(tabArr, StandardCharsets.UTF_8);

        byte[] listArr = new byte[16];
        new Random().nextBytes(listArr);
        String gs2 = new String(listArr, StandardCharsets.UTF_8);

        var indicatorData = HealthIndicatorData.of(player.getUniqueId(), gs1, gs2);

        dataMap.put(player.getUniqueId(), indicatorData);

        if (tabEnabled) {
            ClassStorage.sendPacket(player, getScoreboardObjectiveCreatePacket(gs1, Component.text("healthIndicator")));
            ClassStorage.sendPacket(player, getScoreboardDisplayObjective(gs1, 0));
        }

        if (tagEnabled) {
            ClassStorage.sendPacket(player, getScoreboardObjectiveCreatePacket(gs2, Component.text("§c♥")));
            ClassStorage.sendPacket(player, getScoreboardDisplayObjective(gs2, 2));
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
