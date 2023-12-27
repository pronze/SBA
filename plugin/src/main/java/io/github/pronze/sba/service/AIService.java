package io.github.pronze.sba.service;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.config.SBAConfig.AIConfig;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.citizens.BedwarsBlockPlace;
import io.github.pronze.sba.utils.citizens.BridgePillarTrait;
import io.github.pronze.sba.utils.citizens.FakeDeathTrait;
import io.github.pronze.sba.utils.citizens.Strategy;
import lombok.Getter;
import lombok.SneakyThrows;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.GameModeTrait;
import net.citizensnpcs.trait.SkinTrait;
import org.screamingsandals.lib.spectator.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.npc.NPCManager;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.tasker.DefaultThreads;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.lib.npc.skin.NPCSkin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@ServiceDependencies(dependsOn = {
                NPCManager.class
})
@Getter
public class AIService implements Listener {

        @Getter
        private class NPCRegistryWrapper {
                public NPCRegistryWrapper() {
                        registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
                }

                NPCRegistry registry;
        }

        private static AIService instance;

        NPCRegistryWrapper registry;

        @Getter
        AIConfig settings;

        public static AIService getInstance() {
                return instance;
        }

        public AIService() {
                instance = this;
        }

        @OnPostEnable
        public void onPostEnabled() {
                if (SBA.isBroken())
                        return;

                settings = SBAConfig.getInstance().ai();
                if (SBA.getInstance().citizensFix.canEnable() && SBAConfig.getInstance().ai().enabled()) {
                        if (registry == null) {
                                registry = new NPCRegistryWrapper();
                                SBA.getInstance().registerListener(this);
                        }

                }
        }

        public static void reload() {
                if (AIService.getInstance() != null) {
                        instance.onDisable();
                        instance.onPostEnabled();
                }
        }

        @OnPreDisable
        public void onDisable() {
                if (registry != null) {
                        registry.getRegistry().deregisterAll();
                        registry = null;
                }
        }

        public NPC getNPC(Entity e) {
                if (registry != null) {
                        return registry.getRegistry().getNPC(e);
                }
                return null;
        }

        public CompletableFuture<Player> spawnAI(Location loc) {
                return spawnAI(loc, Strategy.ANY);
        }

        public CompletableFuture<Player> spawnAI(Location loc, Strategy strategy) {
                CompletableFuture<Player> CompletableFuture = new CompletableFuture<Player>();
                if (registry != null) {
                        AtomicInteger count = new AtomicInteger(1);
                        registry.getRegistry().forEach(npc -> count.incrementAndGet());
                        final NPC npc = registry.getRegistry().createNPC(EntityType.PLAYER, "AI_" + count.get());
                        FakeDeathTrait fdt = npc.getOrAddTrait(FakeDeathTrait.class);
                        fdt.setStrategy(strategy);

                        npc.spawn(loc);
                        npc.setProtected(false);

                        npc.data().set(NPC.Metadata.REMOVE_FROM_PLAYERLIST, false);
                        npc.data().set(NPC.Metadata.KEEP_CHUNK_LOADED, true);
                        npc.data().set(NPC.Metadata.SHOULD_SAVE, false);
                        npc.data().set(NPC.Metadata.COLLIDABLE, true);
                        npc.data().set(NPC.Metadata.DISABLE_DEFAULT_STUCK_ACTION, true);

                        npc.getNavigator().getLocalParameters().attackDelayTicks(1).useNewPathfinder(true);
                        npc.getNavigator().getLocalParameters().distanceMargin(1);
                        npc.getNavigator().getLocalParameters().attackRange(1.5f);
                        npc.getNavigator().getLocalParameters().avoidWater(true);
                        npc.addTrait(new BridgePillarTrait());
                        npc.addTrait(new BedwarsBlockPlace());
                        npc.getOrAddTrait(SkinTrait.class).setSkinName(settings.skin());

                        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
                                Player ai = (Player) (npc.getEntity());
                                ai.setCanPickupItems(true);
                                CompletableFuture.complete(ai);
                        }, settings.delay(), TaskerTime.TICKS);

                } else {
                        CompletableFuture.complete(null);
                }

                return CompletableFuture;
        }

        public boolean isNPC(Player player) {
                return registry != null && registry.getRegistry().isNPC(player);
        }

        Method getPlayerHandle = null;
        Field getPlayerKiller = null;

        private Object getHandle(Player player) {
                try {
                        Method getHandle = this.getPlayerHandle == null
                                        ? this.getPlayerHandle = player.getClass().getDeclaredMethod("getHandle")
                                        : this.getPlayerHandle;
                        // I think this is probably the simplest way to do this?
                        return getHandle.invoke(player, getHandle);
                } catch (Throwable e) {
                        e.printStackTrace();
                        return null;
                }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onNPCRespawn(PlayerGameModeChangeEvent event) {
                /*if (!isNPC(event.getPlayer()))
                        return;
                if (event.getNewGameMode() != GameMode.SPECTATOR) {
                        try {
                                Object handle = Reflect.fastInvoke(event.getPlayer(), "getHandle");
                                Reflect.setField(handle, "noPhysics", false);
                                Reflect.setField(handle, "onGround", true);
                        } catch (Throwable t) {
                                t.printStackTrace();
                        }
                }*/
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onPlayerLeave(BedwarsPlayerLeaveEvent event) {
                var game = event.getGame();
                Tasker.run(DefaultThreads.GLOBAL_THREAD, () -> {
                        boolean allAI = true;
                        for (Player p : game.getConnectedPlayers()) {
                                if (!isNPC(p)) {
                                        allAI = false;
                                        break;
                                }
                        }
                        if (allAI) {
                                for (Player p : new ArrayList<>(game.getConnectedPlayers())) {
                                        game.leaveFromGame(p);
                                        getNPC(p).destroy();
                                }
                        }
                });
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        @SuppressWarnings("deprecation")
        public void onDamage(EntityDamageByEntityEvent event) {
                if (event.getEntity().hasMetadata("FakeDeath")) {

                        double damageCount = event.getFinalDamage();
                        Player entity = (Player) event.getEntity();
                        Logger.trace("NPC Damage (By entity)");

                        if (entity.getHealth() < damageCount + 1) {
                                if (event.getDamager() instanceof Player) {
                                        Player killer = (Player) event.getDamager();
                                        if (Reflect.hasMethod(entity.getClass(), "setKiller", Player.class)) {
                                                entity.setKiller(killer);
                                        } else {

                                                try {
                                                        Object playerHandle = getHandle(entity);
                                                        Object killerHandle = killer == null ? null : getHandle(killer);
                                                        Field nmsPlayer = this.getPlayerKiller == null
                                                                        ? this.getPlayerKiller = playerHandle.getClass()
                                                                                        .getDeclaredField("killer")
                                                                        : this.getPlayerKiller;
                                                        nmsPlayer.set(playerHandle, killerHandle);
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }
                        }
                }

                if (event.getDamager().hasMetadata("FakeDeath") && event.getEntity() instanceof LivingEntity) {
                        double damageCount = event.getFinalDamage();
                        LivingEntity entity = (LivingEntity) event.getEntity();
                        if (entity.getHealth() < damageCount) {
                                NPC npc = getNPC(event.getDamager());
                                if (npc != null) {
                                        npc.getNavigator().cancelNavigation();
                                }
                        }
                }
        }

        @EventHandler
        public void npcDespawn(net.citizensnpcs.api.event.NPCDespawnEvent event) {
                var npc = event.getNPC();
                if (npc.getEntity() instanceof Player) {
                        Player player = npc.getOrAddTrait(FakeDeathTrait.class).getPlayerObject();
                        if (Main.getInstance().isPlayerPlayingAnyGame(player)) {
                                if (event.getReason() == DespawnReason.DEATH) {
                                        Logger.trace("NPC HAD DEATH, leaving game to prevent issues");

                                        Game g = Main.getInstance().getGameOfPlayer(player);
                                        g.leaveFromGame(player);
                                        try {
                                                npc.destroy();
                                        } catch (Exception e) {
                                                
                                        }

                                } else if (event.getReason() == DespawnReason.REMOVAL
                                                || event.getReason() == DespawnReason.PLUGIN) {

                                } else {
                                        event.setCancelled(true);
                                }
                        }
                }
        }

        @EventHandler
        public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
                if (isNPC(e.getPlayer()))
                        getNPC(e.getPlayer()).destroy();
        }

        @EventHandler
        public void onBedWarsPlayerJoin(BedwarsPlayerJoinEvent e) {
                if (isNPC(e.getPlayer())) {
                        FakeDeathTrait fdt = getNPC(e.getPlayer()).getTraitNullable(FakeDeathTrait.class);
                        fdt.joinBedwarsGame(e.getGame());
                }

        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        @SuppressWarnings("deprecation")
        public void onDamage(EntityDamageEvent event) {
                if (event.getEntity().hasMetadata("FakeDeath")) {
                        double damageCount = event.getFinalDamage();
                        Player entity = (Player) event.getEntity();
                        Logger.trace("NPC Damage (general)");

                        if (entity.getHealth() < damageCount + 1 || event.getCause() == DamageCause.VOID) {
                                Logger.trace("NPC WOULD HAVE DIED");
                                event.setCancelled(true);
                                die(entity);
                        }
                }
        }

        @SuppressWarnings("deprecation")
        public void die(Player entity) {
                PlayerDeathEvent pde = new PlayerDeathEvent(entity, new ArrayList<>(), 0, "");
                PlayerRespawnEvent pre = new PlayerRespawnEvent(entity, entity.getLocation(), false);
                entity.setHealth(entity.getMaxHealth());
                NPC npc = getNPC(entity);
                if (npc != null) {
                        npc.getNavigator().cancelNavigation();
                }
                manualDispatchEvent(pde, Main.getInstance());
                manualDispatchEvent(pde, SBA.getPluginInstance());

                manualDispatchEvent(pre, Main.getInstance());
                manualDispatchEvent(pre, SBA.getPluginInstance());
        }

        private void manualDispatchEvent(Event evt, Plugin p) {
                ArrayList<RegisteredListener> bedwarsHandler = HandlerList.getRegisteredListeners(p);

                ArrayList<Method> methods = new ArrayList<>();
                for (RegisteredListener handle : bedwarsHandler) {
                        var listener = handle.getListener();
                        if (listener != null) {
                                var clazz = listener.getClass();
                                for (var method : clazz.getMethods()) {
                                        if (!methods.contains(method))
                                                if (method.getParameterTypes().length == 1
                                                                && method.getParameterTypes()[0]
                                                                                .equals(evt.getClass())) {
                                                        try {
                                                                methods.add(method);
                                                                method.invoke(listener, evt);
                                                        } catch (IllegalAccessException | IllegalArgumentException
                                                                        | InvocationTargetException e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                        }
                                                }
                                }
                        }
                }
        }

}
