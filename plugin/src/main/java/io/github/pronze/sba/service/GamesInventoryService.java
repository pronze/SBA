package io.github.pronze.sba.service;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.GameMode;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.visuals.MainLobbyVisualsManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerJoinEvent;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.npc.skin.NPCSkin;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service(dependsOn = {
        PlayerMapper.class,
        PlayerWrapperService.class
})
public class GamesInventoryService implements Listener {
    private class NPCConfig {
        public Location location;
        public String skin;
        public String skin_signature;
        public int mode;
        public NPC npc;
    }

    public static enum Action {
        Remove,
        Skin
    };

    private final Map<Integer, Action> entityEditMap = new LinkedHashMap<>();
    private final Map<Integer, Object> entityEditMapArgument = new LinkedHashMap<>();

    private final List<NPCConfig> NPCs = new ArrayList<>();

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
            NPCs.stream().filter(npcc -> npcc.npc != null).forEach(npcc -> {
                npcc.npc.destroy();
                npcc.npc = null;
            });

            NPCs.clear();

            checkAndAdd(config, GameMode.SOLOS, new ArrayList<>());
            checkAndAdd(config, GameMode.DOUBLES, new ArrayList<>());
            checkAndAdd(config, GameMode.TRIPLES, new ArrayList<>());
            checkAndAdd(config, GameMode.SQUADS, new ArrayList<>());
            Logger.trace("Loaded old config files into {}", NPCs);
            config.getList("npcs").forEach(element -> {
                try {
                    LinkedHashMap<String, Object> npc_config = (LinkedHashMap<String, Object>) element;
                    NPCConfig cfg = new NPCConfig();
                    LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) npc_config.get("skin");
                    if (obj != null) {
                        cfg.skin = (String) obj.get("value");
                        cfg.skin_signature = (String) obj.get("signature");
                    }
                    cfg.location = (Location) npc_config.get("location");
                    cfg.mode = (Integer) npc_config.get("mode");
                    NPCs.add(cfg);
                } catch (Exception e) {
                    Logger.error("Could not read {}", e);
                }
            });

            NPCs.forEach(npc -> {
                Logger.trace("NPC at {} for mode {}", npc.location, npc.mode);
                NPC n = createNpc(GameMode.fromInt(npc.mode), npc.location);
                if (npc.skin != null) {
                    n.setSkin(new NPCSkin(
                            npc.skin,
                            npc.skin_signature));
                }
                npc.npc = n;
            });
            update();
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
            locations.forEach(location -> {
                Logger.trace("Adding NPC at {} for mode {}", location, mode);

                NPCConfig cfg = new NPCConfig();
                cfg.location = (Location) location;
                cfg.mode = mode.intVal();
                NPCs.add(cfg);
            });
            locations.clear();
        }
        Logger.trace("Loaded gamemode {} into {}", mode, locations);
    }

    private NPC createNpc(GameMode mode, Location location) {
        return NPC.of(LocationMapper.wrapLocation(location))
                .setShouldLookAtPlayer(true)
                .setDisplayName(LanguageService
                        .getInstance()
                        .get(MessageKeys.GAMES_INV_DISPLAY_NAME)
                        .replace("%mode%", mode.strVal())
                        .toComponentList())
                .show();
    }

    public void addNPC(@NotNull GameMode mode, @NotNull Location location) {

        NPC npc = createNpc(mode, location);

        NPCConfig cfg = new NPCConfig();
        cfg.location = (Location) location;
        cfg.mode = mode.intVal();
        cfg.npc = npc;
        NPCs.add(cfg);

        /*
         * switch (mode) {
         * case SOLOS:
         * soloNPCLocations.add(location);
         * soloNpcs.add((npc));
         * break;
         * case DOUBLES:
         * doubleNPCLocations.add(location);
         * doubleNpcs.add((npc));
         * break;
         * case TRIPLES:
         * tripleNPCLocations.add(location);
         * tripleNpcs.add(npc);
         * break;
         * case SQUADS:
         * squadsNPCLocations.add(location);
         * squadsNpcs.add(npc);
         * break;
         * }
         */
        update();
    }

    public void removeNPC(PlayerWrapper remover, @NotNull NPC npc) {
        NPCs.stream().filter(n -> n.npc == npc).findAny().ifPresent(c -> {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.NPC_REMOVED)
                    .send(remover);
            c.npc.destroy();
            NPCs.remove(c);
            update();
        });
    }

    public void update() {
        try {
            final var file = new File(SBA.getInstance().getDataFolder().resolve("games-inventory").toString(),
                    "npc.yml");
            YamlConfiguration config = new YamlConfiguration();

            if (!file.exists()) {
                file.createNewFile();
            }

            List<YamlConfiguration> npcYaml = new ArrayList<>();
            NPCs.forEach(npc -> {
                YamlConfiguration oneNpc = new YamlConfiguration();
                oneNpc.set("location", npc.location);
                var skin = oneNpc.createSection("skin");
                skin.set("value", npc.skin);
                skin.set("signature", npc.skin_signature);
                oneNpc.set("mode", npc.mode);

                npcYaml.add(oneNpc);
            });

            config.set("npcs", npcYaml);

            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addViewer(@NotNull PlayerWrapper player) {
        NPCs.forEach(npc -> npc.npc.addViewer(player));
    }

    public void removeViewer(@NotNull PlayerWrapper player) {
        NPCs.forEach(npc -> npc.npc.removeViewer(player));
    }

    @OnPreDisable
    public void destroy() {
        Logger.trace("Disabling Games inventory NPCs");
        NPCs.forEach(npc -> {
            npc.npc.destroy();
            npc.npc = null;
        });
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

    @OnPreDisable
    private void onDisable() {
        entityEditMap.clear();
    }

    public void addEditable(PlayerWrapper player, Action mode, Object argument) {
        if (entityEditMap.containsKey(player.as(Player.class).getEntityId())) {
            return;
        }
        entityEditMap.put(player.as(Player.class).getEntityId(), mode);
        entityEditMapArgument.put(player.as(Player.class).getEntityId(), argument);
        Tasker.build(() -> {
            entityEditMap.remove(Integer.valueOf(player.as(Player.class).getEntityId()));
            entityEditMapArgument.remove(Integer.valueOf(player.as(Player.class).getEntityId()));
        })
                .delay(5L, TaskerTime.SECONDS).start();
    }

    private void setNpcSkin(PlayerWrapper player, NPC visual) {
        String argument = (String) entityEditMapArgument.get(player.as(Player.class).getEntityId());
        NPCSkin.retrieveSkin(argument).whenComplete((skin, exp) -> {
            if (skin != null) {
                NPCs.stream().filter(n -> n.npc == visual).findAny().ifPresent(c -> {
                    visual.setSkin(skin);
                    c.skin = skin.getValue();
                    c.skin_signature = skin.getSignature();
                    update();
                });
            } else {
                Logger.error("Unable to retreive skin of {}: {}", argument, exp);
            }
        });
    }

    @OnEvent
    public void onNPCTouch(NPCInteractEvent event) {
        if (event.getInteractType() == org.screamingsandals.lib.utils.InteractType.RIGHT_CLICK) {
            if (entityEditMap.containsKey(event.getPlayer().as(Player.class).getEntityId())) {
                Action a = entityEditMap.get(event.getPlayer().as(Player.class).getEntityId());
                if (a == Action.Remove) {
                    removeNPC(event.getPlayer(), event.getVisual());
                } else if (a == Action.Skin) {
                    setNpcSkin(event.getPlayer(), event.getVisual());
                }
                return;
            }
        }

        NPC clicked = event.getVisual();

        NPCs.stream().filter(n -> n.npc == clicked).findAny().ifPresent(c -> {
            new BukkitRunnable() {
                public void run() {
                    GamesInventory
                            .getInstance()
                            .openForPlayer(event.getPlayer().as(Player.class), c.mode);
                }
            }.runTask(SBA.getPluginInstance());
        });
    }

}
