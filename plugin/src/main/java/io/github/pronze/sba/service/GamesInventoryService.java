package io.github.pronze.sba.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.GameMode;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.visuals.MainLobbyVisualsManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerJoinEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.InteractType;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.LocationMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service(dependsOn = {
        PlayerMapper.class,
        PlayerWrapperService.class
})
public class GamesInventoryService implements Listener {

    private final Multimap<GameMode, NPC> npcMap;
    private final List<Integer> entityEditMap;

    public GamesInventoryService() {
        npcMap = ArrayListMultimap.create();
        entityEditMap = new ArrayList<>();
    }

    public static GamesInventoryService getInstance() {
        return ServiceManager.get(GamesInventoryService.class);
    }

    @SneakyThrows
    @OnPostEnable
    public void loadGamesInv() {
        SBA.getInstance().registerListener(this);
        final var file = new File(SBA.getInstance().getDataFolder().resolve("games-inventory").toString(), "npc.yml");
        if (file.exists()) {
            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            for (var gameMode : GameMode.values()) {
                checkAndAdd(config, gameMode);
            }
        }

        Tasker.build(() -> Bukkit.getOnlinePlayers().forEach(player -> {
            if (MainLobbyVisualsManager.isInWorld(player.getLocation())) {
                addViewer(PlayerMapper.wrapPlayer(player));
            }
        })).delay(1L, TaskerTime.SECONDS).start();
    }

    @SuppressWarnings("unchecked")
    private void checkAndAdd(YamlConfiguration config, GameMode mode) {
        final var node = config.get(mode.name().toLowerCase());
        if (node instanceof List) {
            final var locations = (List<Location>) node;
            locations.forEach(location -> addNPC(mode, location));
        }
    }

    public void addNPC(@NotNull GameMode mode, @NotNull Location location) {
        if (mode == GameMode.UNKNOWN) {
            throw new UnsupportedOperationException("Registration with mode unknown??");
        }

        final var npc = NPC.of(LocationMapper.wrapLocation(location))
                .setDisplayName(Message.of(LangKeys.GAMES_INV_DISPLAY_NAME)
                        .placeholder("mode", mode.strVal())
                        .getForAnyone());
        npcMap.put(mode, npc);
        update();
    }

    public void removeNPC(PlayerWrapper remover, @NotNull NPC npc) {
        if (npcMap.containsValue(npc)) {
            Message.of(LangKeys.NPC_REMOVED).send(remover);
            npcMap.values().removeIf(npc::equals);
            npc.destroy();
            update();
        }
    }

    public void update() {
        try {
            final var file = new File(SBA.getInstance().getDataFolder().resolve("games-inventory").toString(), "npc.yml");
            YamlConfiguration config = new YamlConfiguration();

            if (!file.exists()) {
                file.createNewFile();
            }

            for (var key : npcMap.keySet()) {
                final var locations = npcMap.get(key)
                        .stream()
                        .map(NPC::getLocation)
                        .collect(Collectors.toList());
                config.set(key.name().toLowerCase(), locations);
            }
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addViewer(@NotNull PlayerWrapper player) {
        npcMap.values().forEach(npc -> npc.addViewer(player));
    }

    public void removeViewer(@NotNull PlayerWrapper player) {
        npcMap.values().forEach(npc -> npc.removeViewer(player));
    }

    @OnPreDisable
    public void destroy() {
        npcMap.values().forEach(NPC::destroy);
        npcMap.clear();
        update();
    }

    @OnEvent
    public void onPlayerJoin(SPlayerJoinEvent e) {
        final var player = e.getPlayer();
        Tasker.build(() -> {
            if (MainLobbyVisualsManager.isInWorld(player.getLocation().as(Location.class)) && player.isOnline()) {
                addViewer(player);
            }
        }).delay(1L, TaskerTime.SECONDS).start();
    }

    public boolean isNPCAtLocation(@NotNull LocationHolder location) {
        return npcMap.values()
                .stream()
                .map(NPC::getLocation)
                .anyMatch(npcLoc -> npcLoc.equals(location));
    }

    @OnPreDisable
    private void onDisable() {
        entityEditMap.clear();
    }

    public void addEditable(PlayerWrapper player) {
        if (entityEditMap.contains(player.as(Player.class).getEntityId())) {
            return;
        }
        entityEditMap.add(player.as(Player.class).getEntityId());
        Tasker.build(() -> entityEditMap.remove(Integer.valueOf(player.as(Player.class).getEntityId()))).delay(5L, TaskerTime.SECONDS).start();
    }

    @OnEvent
    public void onNPCTouch(NPCInteractEvent event) {
        if (event.getInteractType() == InteractType.RIGHT_CLICK) {
            if (entityEditMap.contains(event.getPlayer().as(Player.class).getEntityId())) {
                removeNPC(event.getPlayer(), event.getVisual());
                return;
            }
        }

        for (var key : npcMap.keySet()) {
            final var npcs = npcMap.get(key);
            final var maybeNPC = npcs.stream()
                    .filter(npc -> event.getVisual().equals(npc))
                    .findAny();

            if (maybeNPC.isPresent()) {
                GamesInventory
                        .getInstance()
                        .openForPlayer(event.getPlayer(), key.ordinal());
                break;
            }
        }
    }
}
