package io.github.pronze.sba.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.Pair;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.lib.world.LocationMapper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.game.GameCreator;
import org.screamingsandals.bedwars.commands.AdminCommand;
import org.screamingsandals.lib.hologram.Hologram;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.packet.SClientboundSetPlayerTeamPacket.CollisionRule;

@Service
public class GameModeListener implements Listener {

    HashMap<String, GameCreator> creators;
    HashMap<String, VisualRunnable> bkRun = new HashMap<>();

    @OnPostEnable
    public void onPostEnable() {
        SBA.getInstance().registerListener(this);

        for (var cmd : Main.getCommands().values()) {
            if (cmd instanceof AdminCommand) {
                creators = ((AdminCommand) cmd).gc;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        for (VisualRunnable runn : bkRun.values()) {
            runn.playerMoved(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (VisualRunnable runn : bkRun.values()) {
            runn.playerMoved(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeave(PlayerTeleportEvent event) {
        for (VisualRunnable runn : bkRun.values()) {
            runn.playerMoved(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        for (VisualRunnable runn : bkRun.values()) {
            runn.playerDisconnect(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreviewCommand(PlayerCommandPreprocessEvent event) {

        String message = event.getMessage();
        String[] arguments = message.split(" ");
        if (arguments.length >= 4 && isBedwarsAdminCommand(arguments)) {
            String arenaName = arguments[2];
            String action = arguments[3];
            var editingHoloEnabled = SBAConfig.getInstance().getBoolean("editing-hologram-enabled", false);
            Logger.trace("editingHoloEnabled:{}", editingHoloEnabled);
            if (editingHoloEnabled) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (action.equals("save")) {
                            if (!creators.containsKey(arenaName) && bkRun.containsKey(arenaName)) {
                                Logger.trace("Arena " + arenaName + " saved");
                                Optional.of(bkRun.remove(arenaName)).ifPresent(r -> r.cancel());
                            }
                        }
                        if (action.equals("edit") || action.equals("add")) {
                            if (creators.containsKey(arenaName) && !bkRun.containsKey(arenaName)) {
                                Logger.trace("Arena " + arenaName + " is now in edit mode ");
                                startHolograms(arenaName);
                            }
                        }
                    }
                }.runTaskLater(SBA.getPluginInstance(), 5);
            }
        }
    }

    private void startHolograms(String arenaName) {
        var runnable = new VisualRunnable();
        runnable.setArena(creators.get(arenaName));
        runnable.runTaskTimer(SBA.getPluginInstance(), 20, 20);
        bkRun.put(arenaName, runnable);
    }

    private boolean isBedwarsCommand(@NotNull String[] arguments) {
        String shortened = arguments[0].toLowerCase();
        return List.of("/bw", "/bedwars", "/bedwars:bw", "/bedwars:bedwars").stream().anyMatch(shortened::equals);
    }

    private boolean isBedwarsAdminCommand(@NotNull String[] arguments) {
        if (!isBedwarsCommand(arguments))
            return false;
        if (arguments.length < 2)
            return false;
        String shortened = arguments[1].toLowerCase();
        return shortened.equals("admin");
    }

    private class VisualRunnable extends BukkitRunnable {

        Map<ItemSpawner, Hologram> holograms = new HashMap<>();
        Map<GameStore, NPC> npcs = new HashMap<>();
        Map<String, GameStore> villagerStores;
        // GameCreator gameCreator;
        Game game;

        public void setArena(GameCreator gameCreator) {
            // this.gameCreator = gameCreator;

            game = gameCreator.getGame();
            Logger.trace("Starting editing holograms for game {}", game.getName());
            villagerStores = (Map<String, GameStore>) Reflect.getField(gameCreator, "villagerstores");
        }

        public void playerMoved(@NotNull Player player) {
            npcs.values().forEach(n -> n.removeViewer(PlayerMapper.wrapPlayer(player)));
            holograms.values().forEach(h -> h.removeViewer(PlayerMapper.wrapPlayer(player)));

            Tasker.build(() -> {
                for (var npc : npcs.values()) {
                    if (npc.location().getWorld().getName() == player.getWorld().getName()) {
                        npc.addViewer(PlayerMapper.wrapPlayer(player));
                    }
                }
                for (var holo : holograms.values()) {
                    if (holo.location().getWorld().getName() == player.getWorld().getName()) {
                        holo.addViewer(PlayerMapper.wrapPlayer(player));
                    }
                }
            }).afterOneTick().start();
        }

        public void playerDisconnect(@NotNull Player player) {
            holograms.values().forEach(h -> h.removeViewer(PlayerMapper.wrapPlayer(player)));
            npcs.values().forEach(n -> n.removeViewer(PlayerMapper.wrapPlayer(player)));
        }

        private NPC addNpcAt(Location l, String name) {
            if (name == null)
                name = "shop.yml";

            NPC npc = NPC.of(LocationMapper.wrapLocation(l))
                    .lookAtPlayer(true)
                    .displayName(List.of(Component.text(name)
                            .color(TextColor.color(139, 69, 19))))
                    .collisionRule(CollisionRule.NEVER)
                    .show();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (l.getWorld() == p.getWorld()) {
                    npc.addViewer(PlayerMapper.wrapPlayer(p));
                }
            }
            return npc;
        }

        @Override
        public void run() {
            // holograms.values().forEach(Hologram::destroy);
            // holograms.clear();
            for (var store : villagerStores.values()) {
                if (!npcs.containsKey(store)) {
                    npcs.put(store, addNpcAt(store.getStoreLocation(),
                            store.getShopCustomName() != null ? store.getShopCustomName()
                                    : store.getShopFile()));
                }
            }
            for (var spawners : game.getSpawners()) {
                if (!holograms.containsKey(spawners)) {
                    var hologram = HologramManager
                            .hologram(LocationMapper.wrapLocation(spawners.getLocation().clone()));
                    hologram.show();

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (spawners.getLocation().getWorld() == p.getWorld()) {
                            hologram.addViewer(PlayerMapper.wrapPlayer(p));
                        }
                    }

                    hologram.item(ItemFactory.build(spawners.getItemSpawnerType().getStack()).orElseThrow())
                            .itemPosition(Hologram.ItemPosition.BELOW)
                            .rotationMode(Hologram.RotationMode.Y)
                            .rotationTime(Pair.of(1, TaskerTime.TICKS))
                            .rotationIncrement(18);

                    holograms.put(spawners, hologram);
                }
            }
            var keys = holograms.keySet().stream().collect(Collectors.toList());
            for (ItemSpawner itemSpawner : keys) {
                if (!game.getSpawners().contains(itemSpawner)) {
                    holograms.get(itemSpawner).destroy();
                    holograms.remove(itemSpawner);
                }
            }
            var keysNpc = npcs.keySet().stream().collect(Collectors.toList());
            for (GameStore store : keysNpc) {
                if (!villagerStores.values().contains(store)) {
                    npcs.get(store).destroy();
                    npcs.remove(store);
                }
            }

        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            holograms.values().forEach(Hologram::destroy);
            holograms.clear();
            npcs.values().forEach(NPC::destroy);
            npcs.clear();

            super.cancel();
        }

    }
}