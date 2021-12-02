package io.github.pronze.sba.service;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.GameMode;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.lib.lang.LanguageService;
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
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.LocationMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service(dependsOn = {
        PlayerMapper.class,
        PlayerWrapperService.class
})
public class GamesInventoryService implements Listener {

    private final List<Location> soloNPCLocations = new ArrayList<>();
    private final List<Location> doubleNPCLocations = new ArrayList<>();
    private final List<Location> tripleNPCLocations = new ArrayList<>();
    private final List<Location> squadsNPCLocations = new ArrayList<>();
    private final List<NPC> npcs = new ArrayList<>();
    private final List<Integer> entityEditMap = new ArrayList<>();

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
            checkAndAdd(config, GameMode.SOLOS, soloNPCLocations);
            checkAndAdd(config, GameMode.DOUBLES, doubleNPCLocations);
            checkAndAdd(config, GameMode.TRIPLES, tripleNPCLocations);
            checkAndAdd(config, GameMode.SQUADS, squadsNPCLocations);
        }
        Tasker.build(() -> Bukkit.getOnlinePlayers().forEach(player -> {
            if (MainLobbyVisualsManager.isInWorld(player.getLocation())) {
                addViewer(PlayerMapper.wrapPlayer(player));
            }
        })).delay(1L, TaskerTime.SECONDS).start();
    }

    @SuppressWarnings("unchecked")
    private void checkAndAdd(YamlConfiguration config, GameMode mode, List<Location> locations) {
        final var node = config.get(mode.name().toLowerCase());
        if (node != null) {
            locations.clear();
            locations.addAll((List<Location>) node);
            locations.forEach(location -> npcs.add(NPC.of(LocationMapper.wrapLocation(location))
                            .setDisplayName(LanguageService
                            .getInstance()
                            .get(MessageKeys.GAMES_INV_DISPLAY_NAME)
                            .replace("%mode%", mode.strVal())
                            .toComponentList())
                            .show()));
        }
    }

    public void addNPC(@NotNull GameMode mode, @NotNull Location location) {
        npcs.add(NPC.of(LocationMapper.wrapLocation(location))
                .setDisplayName(LanguageService
                        .getInstance()
                        .get(MessageKeys.GAMES_INV_DISPLAY_NAME)
                        .replace("%mode%", mode.strVal())
                        .toComponentList())
                .show());

        switch (mode) {
            case SOLOS:
                soloNPCLocations.add(location);
                break;
            case DOUBLES:
                doubleNPCLocations.add(location);
                break;
            case TRIPLES:
                tripleNPCLocations.add(location);
                break;
            case SQUADS:
                squadsNPCLocations.add(location);
                break;
        }
        update();
    }

    public void removeNPC(PlayerWrapper remover, @NotNull NPC npc) {
        final var loc = Objects.requireNonNull(npc.getLocation()).as(Location.class);
        if (soloNPCLocations.contains(loc) || doubleNPCLocations.contains(loc) || tripleNPCLocations.contains(loc) || squadsNPCLocations.contains(loc)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.NPC_REMOVED)
                    .send(remover);
            soloNPCLocations.remove(loc);
            doubleNPCLocations.remove(loc);
            tripleNPCLocations.remove(loc);
            squadsNPCLocations.remove(loc);
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

            config.set("solos", soloNPCLocations);
            config.set("doubles", doubleNPCLocations);
            config.set("triples", tripleNPCLocations);
            config.set("squads", squadsNPCLocations);
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addViewer(@NotNull PlayerWrapper player) {
        npcs.forEach(npc -> npc.addViewer(player));
    }

    public void removeViewer(@NotNull PlayerWrapper player) {
        npcs.forEach(npc -> npc.removeViewer(player));
    }

    @OnPreDisable
    public void destroy() {
        npcs.forEach(NPC::destroy);
        npcs.clear();
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
        return npcs.stream()
                .map(NPC::getLocation)
                .filter(Objects::nonNull)
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
        if (event.getInteractType() == org.screamingsandals.lib.utils.InteractType.RIGHT_CLICK) {
            if (entityEditMap.contains(event.getPlayer().as(Player.class).getEntityId())) {
                removeNPC(event.getPlayer(), event.getVisual());
                return;
            }
        }

        final var npcLocation = Objects.requireNonNull(event.getVisual().getLocation()).as(Location.class);
        final var isSolo = soloNPCLocations.stream().anyMatch(loc -> loc == npcLocation);
        final var isDouble = doubleNPCLocations.stream().anyMatch(loc -> loc == npcLocation);
        final var isTriple = tripleNPCLocations.stream().anyMatch(loc -> loc == npcLocation);
        final var isSquad = squadsNPCLocations.stream().anyMatch(loc -> loc == npcLocation);
        int mode = 1;
        if (isDouble) {
            mode = 2;
        } else if (isTriple) {
            mode = 3;
        } else if (isSquad) {
            mode = 4;
        } else if (!isSolo) {
            return;
        }
        GamesInventory
                .getInstance()
                .openForPlayer(event.getPlayer().as(Player.class), mode);
    }
}
