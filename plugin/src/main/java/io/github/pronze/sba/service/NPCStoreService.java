package io.github.pronze.sba.service;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.utils.Logger;
import lombok.Getter;
import lombok.SneakyThrows;
import org.screamingsandals.lib.spectator.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.npc.NPCManager;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.npc.skin.NPCSkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@ServiceDependencies(dependsOn = {
                NPCManager.class
})
@Getter
public class NPCStoreService implements Listener {

        public static NPCStoreService getInstance() {
                return ServiceManager.get(NPCStoreService.class);
        }

        private NPCSkin shopSkin;
        private NPCSkin upgradeShopSkin;

        private final List<Component> shopText = new ArrayList<>();
        private final List<Component> upgradeShopText = new ArrayList<>();

        @SneakyThrows
        public NPCStoreService() {
                shopText.clear();
                shopText.addAll(Objects
                                .requireNonNull(SBAConfig.getInstance().node("shop", "normal-shop", "entity-name")
                                                .getList(String.class))
                                .stream()
                                .map(Component::fromLegacy)
                                .collect(Collectors.toList()));

                upgradeShopText.clear();
                upgradeShopText.addAll(Objects
                                .requireNonNull(SBAConfig.getInstance().node("shop", "upgrade-shop", "entity-name")
                                                .getList(String.class))
                                .stream()
                                .map(Component::fromLegacy)
                                .collect(Collectors.toList()));
        }

        @OnPostEnable
        public void onPostEnabled() {
                if(SBA.isBroken())return;
                SBA.getInstance().registerListener(this);
                EventManager.getDefaultEventManager().register(NPCInteractEvent.class, this::onNPCTouched);

                if (SBA.getInstance().citizensFix.canEnable()) {
                        SBA.getInstance().registerListener(new CitizensListeners());
                }

                shopSkin = new NPCSkin(
                                SBAConfig.getInstance().node("shop", "normal-shop", "skin", "value").getString(),
                                SBAConfig.getInstance().node("shop", "normal-shop", "skin", "signature").getString());

                upgradeShopSkin = new NPCSkin(
                                SBAConfig.getInstance().node("shop", "upgrade-shop", "skin", "value").getString(),
                                SBAConfig.getInstance().node("shop", "upgrade-shop", "skin", "signature").getString());
        }

        private class CitizensListeners implements Listener {
                @EventHandler
                public void rclick(net.citizensnpcs.api.event.NPCRightClickEvent event) {
                        var npc = event.getNPC();
                        // Handle a click on a NPC. The event has a getNPC() method.
                        // Be sure to check event.getNPC() == this.getNPC() so you only handle clicks on
                        // this NPC!
                        final var player = event.getClicker();
                        npcClick(npc, player);
                }

                private void npcClick(net.citizensnpcs.api.npc.NPC npc, final Player player) {
                        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
                                return;
                        }
                        if (!SBAConfig.getInstance().replaceStoreWithCitizen()) {
                                return;
                        }
                        final var game = Main.getInstance().getGameOfPlayer(player);
                        ArenaManager
                                        .getInstance()
                                        .getArenaMap()
                                        .values()
                                        .stream()
                                        .filter(iArena -> iArena.getCitizensStores().values().contains(npc))
                                        .forEach(arena -> {

                                                GameStore store = null;
                                                for (var i : arena.getCitizensStores().entrySet()) {
                                                        if (i.getValue().equals(npc))
                                                                store = (GameStore) i.getKey();
                                                }

                                                BedwarsOpenShopEvent openShopEvent = new BedwarsOpenShopEvent(game,
                                                                player, store, null);

                                                new BukkitRunnable() {
                                                        public void run() {
                                                                Bukkit.getServer().getPluginManager()
                                                                                .callEvent(openShopEvent);
                                                        }
                                                }.runTask(SBA.getPluginInstance());
                                        });
                }

                @EventHandler
                public void lclick(net.citizensnpcs.api.event.NPCLeftClickEvent event) {
                        var npc = event.getNPC();
                        // Handle a click on a NPC. The event has a getNPC() method.
                        // Be sure to check event.getNPC() == this.getNPC() so you only handle clicks on
                        // this NPC!
                        final var player = event.getClicker();
                        npcClick(npc, player);
                }

        }

        public void onNPCTouched(NPCInteractEvent event) {
                Logger.trace("Clicked NPC with click type: {}", event.interactType().name());
                // if (event.getInteractType() ==
                // org.screamingsandals.lib.utils.InteractType.LEFT_CLICK) {
                // return;
                // }
                final var player = event.player().as(Player.class);
                if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
                        return;
                }
                Logger.trace("Clicked NPC with click type: {}", event.interactType().name());

                final var game = Main.getInstance().getGameOfPlayer(player);
                final var npc = event.visual();

                ArenaManager
                                .getInstance()
                                .getArenaMap()
                                .values()
                                .stream()
                                .filter(iArena -> iArena.getStores().values().contains(npc))
                                .forEach(arena -> {

                                        GameStore store = null;
                                        for (var i : arena.getStores().entrySet()) {
                                                if (i.getValue().equals(npc))
                                                        store = (GameStore) i.getKey();
                                        }

                                        BedwarsOpenShopEvent openShopEvent = new BedwarsOpenShopEvent(game,
                                                        player, store, null);

                                        new BukkitRunnable() {
                                                public void run() {
                                                        Bukkit.getServer().getPluginManager().callEvent(openShopEvent);
                                                }
                                        }.runTask(SBA.getPluginInstance());

                                });
        }

}
