package io.github.pronze.sba.service;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.npc.skin.NPCSkin;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.tasker.DefaultThreads;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@ServiceDependencies(dependsOn = {
        PlayerWrapperService.class
})
public class GamesInventoryService implements Listener {
    private class NPCConfig {
        public Location location;
        public String skin;
        public String skin_signature;
        public String mode;
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
                if(SBA.isBroken())return;
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

            checkAndAdd(config, "SOLO", new ArrayList<>());
            checkAndAdd(config, "DOUBLES", new ArrayList<>());
            checkAndAdd(config, "TRIPLES", new ArrayList<>());
            checkAndAdd(config, "SQUADS", new ArrayList<>());
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
                    if (npc_config.get("mode") instanceof Integer)
                        cfg.mode = String.valueOf(((Integer) npc_config.get("mode")).intValue());
                    else
                        cfg.mode = (String) npc_config.get("mode");
                    NPCs.add(cfg);
                } catch (Exception e) {
                    Logger.error("Could not read {}", e);
                }
            });

            NPCs.forEach(npc -> {
                try {
                    Logger.trace("NPC at {} for mode {}", npc.location, npc.mode);
                    NPC n = createNpc(npc.mode, npc.location);
                    if (npc.skin != null) {
                        n.skin(new NPCSkin(
                                npc.skin,
                                npc.skin_signature));
                    }
                    npc.npc = n;
                } catch (Throwable t) {
                    Logger.error("{}", t);
                }
            });
            update();
        }
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> Bukkit.getOnlinePlayers().forEach(player -> {
            addViewer(player);
        }), 1L, TaskerTime.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void checkAndAdd(YamlConfiguration config, String mode, List<Location> locations) {
        final var node = config.get(mode.toLowerCase());
        if (node != null) {
            locations.clear();
            locations.addAll((List<Location>) node);
            locations.forEach(location -> {
                Logger.trace("Adding NPC at {} for mode {}", location, mode);

                NPCConfig cfg = new NPCConfig();
                cfg.location = (Location) location;
                cfg.mode = mode;
                NPCs.add(cfg);
            });
            locations.clear();
        }
        Logger.trace("Loaded gamemode {} into {}", mode, locations);
    }

    private NPC createNpc(String mode, Location location) {
        return NPC.of(Objects.requireNonNull(org.screamingsandals.lib.world.Location.fromPlatform(location)))
                .lookAtPlayer(true)
                .displayName(LanguageService
                        .getInstance()
                        .get(MessageKeys.GAMES_INV_DISPLAY_NAME)
                        .replace("%mode%", mode)
                        .toComponentList())
                .show();
    }

    public void addNPC(@NotNull String mode, @NotNull Location location) {

        NPC npc = createNpc(mode, location);

        NPCConfig cfg = new NPCConfig();
        cfg.location = (Location) location;
        cfg.mode = mode;
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

    public void removeNPC(org.screamingsandals.lib.player.Player remover, @NotNull NPC npc) {
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

    public void addViewer(@NotNull Player player) {
        Logger.trace("addViewer", player.getName());

        NPCs.forEach(npc -> {
            Logger.trace("npc::addViewer", player.getName());
            if (npc.location.getWorld().equals(player.getWorld()))
                if (npc.npc != null)
                    npc.npc.addViewer(Players.wrapPlayer(player));
        });
    }

    public void removeViewer(@NotNull Player player) {
        NPCs.forEach(npc -> {
            if (npc.npc != null)
                npc.npc.removeViewer(Players.wrapPlayer(player));
        });
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var player = e.getPlayer();
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
            if (player.isOnline()) {
                addViewer(player);
            }
        }, 1L, TaskerTime.TICKS);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        final var player = e.getPlayer();
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
            if (player.isOnline()) {
                addViewer(player);
            }
        }, 1L, TaskerTime.TICKS);
    }

    @OnPreDisable
    private void onDisable() {
        entityEditMap.clear();
    }

    public void addEditable(org.screamingsandals.lib.player.Player player, Action mode, Object argument) {
        if (entityEditMap.containsKey(player.as(Player.class).getEntityId())) {
            return;
        }
        entityEditMap.put(player.as(Player.class).getEntityId(), mode);
        entityEditMapArgument.put(player.as(Player.class).getEntityId(), argument);
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
            entityEditMap.remove(Integer.valueOf(player.as(Player.class).getEntityId()));
            entityEditMapArgument.remove(Integer.valueOf(player.as(Player.class).getEntityId()));
        }, 5L, TaskerTime.SECONDS);
    }

    private void setNpcSkin(org.screamingsandals.lib.player.Player player, NPC visual) {
        String argument = (String) entityEditMapArgument.get(player.as(Player.class).getEntityId());
        NPCSkin.retrieveSkin(argument).whenComplete((skin, exp) -> {
            if (skin != null) {
                NPCs.stream().filter(n -> n.npc == visual).findAny().ifPresent(c -> {
                    visual.skin(skin);
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
        if (event.interactType() == org.screamingsandals.lib.utils.InteractType.RIGHT_CLICK) {
            if (entityEditMap.containsKey(event.player().as(Player.class).getEntityId())) {
                Action a = entityEditMap.get(event.player().as(Player.class).getEntityId());
                if (a == Action.Remove) {
                    removeNPC(event.player(), event.visual());
                } else if (a == Action.Skin) {
                    setNpcSkin(event.player(), event.visual());
                }
                return;
            }
        }

        NPC clicked = event.visual();

        NPCs.stream().filter(n -> n.npc == clicked).findAny().ifPresent(c -> {
            new BukkitRunnable() {
                public void run() {
                    GamesInventory
                            .getInstance()
                            .openForPlayer(event.player().as(Player.class), c.mode);
                }
            }.runTask(SBA.getPluginInstance());
        });
    }

}
