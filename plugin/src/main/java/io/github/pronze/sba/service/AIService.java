package io.github.pronze.sba.service;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.citizens.AIPlayer;
import io.github.pronze.sba.utils.citizens.BedwarsBlockPlace;
import io.github.pronze.sba.utils.citizens.BridgePillarTrait;
import io.github.pronze.sba.utils.citizens.FakeDeathTrait;
import lombok.Getter;
import lombok.SneakyThrows;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.npc.NPCManager;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
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

@Service(dependsOn = {
                NPCManager.class
})
@Getter
public class AIService implements Listener {

        NPCRegistry registry;

        public static AIService getInstance() {
                return ServiceManager.get(AIService.class);

        }

        @SneakyThrows
        public AIService() {

        }

        @OnPostEnable
        public void onPostEnabled() {
                SBA.getInstance().registerListener(this);
                if (SBA.getPluginInstance().getServer().getPluginManager().getPlugin("Citizens") != null
                                && SBA.getPluginInstance().getServer().getPluginManager().getPlugin("Citizens")
                                                .isEnabled()) {
                        if (registry == null)
                                registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
                }
        }

        @OnPreDisable
        public void onDisable() {
                if (registry != null) {
                        registry.deregisterAll();
                }
        }

        public NPC getNPC(Entity e) {
                if (e instanceof AIPlayer)
                {
                        return ((AIPlayer) e).getNpc();
                }
                if (registry != null) {
                        return registry.getNPC(e);
                }
                return null;
        }

        public CompletableFuture<Player> spawnAI(Location loc) {
                CompletableFuture<Player> CompletableFuture = new CompletableFuture<Player>();  
                if (registry != null) {
                        AtomicInteger count = new AtomicInteger(1);
                        registry.forEach(npc -> count.incrementAndGet());
                        final NPC npc = registry.createNPC(EntityType.PLAYER, "AI_" + count.get());
                        npc.data().set("removefromtablist", false);
                        npc.spawn(loc);
                        npc.setProtected(false);
                        FakeDeathTrait fdt = npc.getOrAddTrait(FakeDeathTrait.class);

                        npc.getNavigator().getLocalParameters().attackDelayTicks(1).useNewPathfinder(true);
                        //npc.getNavigator().getLocalParameters().distanceMargin(0);
                        npc.getNavigator().getLocalParameters().stuckAction(new StuckAction() {
                                @Override
                                public boolean run(NPC arg0, Navigator arg1) {
                                        Logger.trace("NPC IS STUCK {}", arg0.getName());
                                        return false;
                                }
                        });
                        npc.getNavigator().getLocalParameters().attackRange(1.5f);
                        npc.addTrait(new BridgePillarTrait());
                        npc.addTrait(new BedwarsBlockPlace());
                        npc.getOrAddTrait(SkinTrait.class).setSkinName("robot");
                        Tasker.build(() -> {
                                Player ai = (Player) (npc.getEntity());
                                CompletableFuture.complete(ai);
                        }).delay(4, TaskerTime.SECONDS).start();
                       

                }
                else
                {
                        CompletableFuture.complete(null);
                }

                return CompletableFuture;
        }

        public boolean isNPC(Player player) {
                if (player instanceof AIPlayer)
                {
                        return true;
                }
                return registry != null && registry.isNPC(player);
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
            
        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        @SuppressWarnings("deprecation")
        public void onDamage(EntityDamageByEntityEvent event) {
                if (event.getEntity().hasMetadata("FakeDeath")) {

                        double damageCount = event.getFinalDamage();
                        Player entity = (Player) event.getEntity();
                        Logger.trace("NPC Damage (By entity)");

                        if (entity.getHealth() < damageCount + 1) {
                                if (event.getDamager() instanceof Player) {
                                        try {
                                                entity.setKiller((Player) event.getDamager());
                                        } catch (Throwable nsm) {
                                                Player killer = (Player) event.getDamager();
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

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onGameModeChange(PlayerGameModeChangeEvent event)
        {
                if (event.getPlayer().hasMetadata("FakeDeath")) {
                        Bukkit.getServer().getOnlinePlayers().forEach(pl -> pl.showPlayer(event.getPlayer()));
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

                                PlayerDeathEvent pde = new PlayerDeathEvent(entity, new ArrayList<>(), 0, "");
                                PlayerRespawnEvent pre = new PlayerRespawnEvent(entity, entity.getLocation(), false);
                                entity.setHealth(entity.getMaxHealth());
                                NPC npc = getNPC(event.getEntity());
                                if (npc != null) {
                                        npc.getNavigator().cancelNavigation();
                                }
                                manualDispatchEvent(pde, Main.getInstance());
                                manualDispatchEvent(pde, SBA.getPluginInstance());

                                manualDispatchEvent(pre, Main.getInstance());
                                manualDispatchEvent(pre, SBA.getPluginInstance());
                        }
                }
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
                                                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                        } 
                                                }
                                }
                        }
                }
        }

}
